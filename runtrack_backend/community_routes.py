import json
from typing import List

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy import func
from sqlalchemy.orm import Session

from auth import get_current_user
from database import get_db
from models import (
    ActivityDB,
    ClubDB,
    ClubEventDB,
    ClubEventRegistrationDB,
    ClubMemberDB,
    GpsArtRouteDB,
    UserDB,
    UserProfileDB,
)
from schemas import (
    ClubDetailResponse,
    ClubEventItem,
    ClubMemberItem,
    ClubResponse,
    GpsArtRouteItem,
    RoutePoint,
)
from services import (
    build_club_response,
    ensure_default_clubs,
    get_current_week_bounds,
    parse_route_points,
)

router = APIRouter(tags=["Community & GPS Art"])

DEFAULT_GPS_ART_ROUTES = (
    (
        "Trái Tim Hồ Gươm",
        5.2,
        "Hà Nội",
        "heart",
        [
            {"lat": 21.0285, "lng": 105.8522},
            {"lat": 21.0310, "lng": 105.8500},
            {"lat": 21.0322, "lng": 105.8520},
            {"lat": 21.0308, "lng": 105.8542},
            {"lat": 21.0322, "lng": 105.8564},
            {"lat": 21.0308, "lng": 105.8582},
            {"lat": 21.0276, "lng": 105.8562},
            {"lat": 21.0252, "lng": 105.8542},
            {"lat": 21.0276, "lng": 105.8522},
            {"lat": 21.0285, "lng": 105.8522},
        ],
    ),
    (
        "Khủng Long Hồ Tây",
        8.6,
        "Hà Nội",
        "dino",
        [
            {"lat": 21.0450, "lng": 105.8180},
            {"lat": 21.0485, "lng": 105.8205},
            {"lat": 21.0520, "lng": 105.8220},
            {"lat": 21.0510, "lng": 105.8260},
            {"lat": 21.0475, "lng": 105.8285},
            {"lat": 21.0440, "lng": 105.8265},
            {"lat": 21.0425, "lng": 105.8230},
            {"lat": 21.0450, "lng": 105.8180},
        ],
    ),
    (
        "Sét Đánh Thủ Thiêm",
        6.4,
        "TP.HCM",
        "bolt",
        [
            {"lat": 10.7785, "lng": 106.7090},
            {"lat": 10.7745, "lng": 106.7055},
            {"lat": 10.7740, "lng": 106.7100},
            {"lat": 10.7690, "lng": 106.7065},
            {"lat": 10.7725, "lng": 106.7130},
            {"lat": 10.7735, "lng": 106.7095},
            {"lat": 10.7785, "lng": 106.7090},
        ],
    ),
    (
        "Ngôi Sao Công Viên Gia Định",
        4.8,
        "TP.HCM",
        "star",
        [
            {"lat": 10.8135, "lng": 106.6765},
            {"lat": 10.8108, "lng": 106.6780},
            {"lat": 10.8105, "lng": 106.6812},
            {"lat": 10.8085, "lng": 106.6788},
            {"lat": 10.8058, "lng": 106.6798},
            {"lat": 10.8075, "lng": 106.6765},
            {"lat": 10.8058, "lng": 106.6732},
            {"lat": 10.8085, "lng": 106.6742},
            {"lat": 10.8105, "lng": 106.6718},
            {"lat": 10.8135, "lng": 106.6765},
        ],
    ),
)


def ensure_default_gps_art_routes(db: Session) -> list[GpsArtRouteDB]:
    """Khởi tạo 4 tuyến GPS Art mẫu nếu chưa tồn tại trong cơ sở dữ liệu."""
    created = False
    for name, distance_km, city, icon_key, points in DEFAULT_GPS_ART_ROUTES:
        if not db.query(GpsArtRouteDB).filter(GpsArtRouteDB.name == name).first():
            db.add(
                GpsArtRouteDB(
                    name=name,
                    distance_km=distance_km,
                    city=city,
                    icon_key=icon_key,
                    points_json=json.dumps(points),
                )
            )
            created = True
    if created:
        db.commit()
    return db.query(GpsArtRouteDB).order_by(GpsArtRouteDB.id.asc()).all()


def _build_club_event_item(event: ClubEventDB, user_id: int, db: Session) -> ClubEventItem:
    registered_count = (
        db.query(ClubEventRegistrationDB)
        .filter(ClubEventRegistrationDB.event_id == event.id)
        .count()
    )
    is_registered = (
        db.query(ClubEventRegistrationDB)
        .filter(
            ClubEventRegistrationDB.event_id == event.id,
            ClubEventRegistrationDB.user_id == user_id,
        )
        .first()
        is not None
    )
    return ClubEventItem(
        event_id=event.id,
        club_id=event.club_id,
        title=event.title,
        schedule_text=event.schedule_text or "",
        location=event.location or "",
        registered_count=registered_count,
        is_registered=is_registered,
    )


@router.get("/search/clubs", response_model=List[ClubResponse])
async def search_clubs(
    q: str = "",
    page: int = Query(default=1, ge=1),
    limit: int = Query(default=10, ge=1, le=50),
    current_user: UserDB = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    if db.query(ClubDB).count() == 0:
        ensure_default_clubs(db)
    query = db.query(ClubDB)
    keyword = q.strip()
    if keyword:
        query = query.filter(ClubDB.name.ilike(f"%{keyword}%"))
    clubs = query.order_by(ClubDB.id.asc()).offset((page - 1) * limit).limit(limit).all()
    return [build_club_response(club, db, current_user.id) for club in clubs]


@router.post("/clubs/{club_id}/toggle-join", response_model=ClubResponse)
async def toggle_join_club(
    club_id: int,
    current_user: UserDB = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    club = db.query(ClubDB).filter(ClubDB.id == club_id).first()
    if not club:
        raise HTTPException(status_code=404, detail="Không tìm thấy câu lạc bộ")
    membership = (
        db.query(ClubMemberDB)
        .filter(ClubMemberDB.club_id == club_id, ClubMemberDB.user_id == current_user.id)
        .first()
    )
    if membership:
        db.delete(membership)
    else:
        db.add(ClubMemberDB(club_id=club_id, user_id=current_user.id))
    db.commit()
    return build_club_response(club, db, current_user.id)


@router.get("/clubs/{club_id}/detail", response_model=ClubDetailResponse)
async def get_club_detail(
    club_id: int,
    current_user: UserDB = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    club = db.query(ClubDB).filter(ClubDB.id == club_id).first()
    if not club:
        raise HTTPException(status_code=404, detail="Không tìm thấy câu lạc bộ")

    _, week_start_utc, week_end_utc = get_current_week_bounds()
    member_rows = (
        db.query(ClubMemberDB, UserDB, UserProfileDB)
        .join(UserDB, ClubMemberDB.user_id == UserDB.id)
        .outerjoin(UserProfileDB, UserDB.id == UserProfileDB.user_id)
        .filter(ClubMemberDB.club_id == club_id)
        .all()
    )
    weekly_km_map: dict[int, float] = {}
    if member_rows:
        member_ids = [u.id for _, u, _ in member_rows]
        agg_rows = (
            db.query(ActivityDB.user_id, func.coalesce(func.sum(ActivityDB.distance_km), 0.0))
            .filter(
                ActivityDB.user_id.in_(member_ids),
                ActivityDB.start_time >= week_start_utc,
                ActivityDB.start_time < week_end_utc,
            )
            .group_by(ActivityDB.user_id)
            .all()
        )
        weekly_km_map = {uid: round(float(km or 0.0), 2) for uid, km in agg_rows}

    members = [
        ClubMemberItem(
            user_id=user.id,
            full_name=(profile.full_name if profile and profile.full_name else user.email.split("@")[0]),
            weekly_km=weekly_km_map.get(user.id, 0.0),
            joined_at=membership.joined_at.isoformat() if membership.joined_at else None,
        )
        for membership, user, profile in member_rows
    ]
    members.sort(key=lambda m: (-m.weekly_km, m.user_id))

    events = db.query(ClubEventDB).filter(ClubEventDB.club_id == club_id).order_by(ClubEventDB.id.asc()).all()
    summary = build_club_response(club, db, current_user.id)
    return ClubDetailResponse(
        club_id=summary.club_id,
        name=summary.name,
        description=summary.description,
        member_count=summary.member_count,
        is_joined=summary.is_joined,
        members=members,
        events=[_build_club_event_item(ev, current_user.id, db) for ev in events],
    )


@router.post("/clubs/events/{event_id}/toggle-register", response_model=ClubEventItem)
async def toggle_register_club_event(
    event_id: int,
    current_user: UserDB = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    event = db.query(ClubEventDB).filter(ClubEventDB.id == event_id).first()
    if not event:
        raise HTTPException(status_code=404, detail="Không tìm thấy sự kiện câu lạc bộ")

    registration = (
        db.query(ClubEventRegistrationDB)
        .filter(
            ClubEventRegistrationDB.event_id == event_id,
            ClubEventRegistrationDB.user_id == current_user.id,
        )
        .first()
    )
    if registration:
        db.delete(registration)
    else:
        db.add(ClubEventRegistrationDB(event_id=event_id, user_id=current_user.id))
    db.commit()
    return _build_club_event_item(event, current_user.id, db)


@router.get("/gps-art/routes", response_model=List[GpsArtRouteItem])
async def get_gps_art_routes(db: Session = Depends(get_db)):
    if db.query(GpsArtRouteDB).count() == 0:
        ensure_default_gps_art_routes(db)
    routes = db.query(GpsArtRouteDB).order_by(GpsArtRouteDB.id.asc()).all()
    return [
        GpsArtRouteItem(
            id=route.id,
            name=route.name,
            distance_km=round(float(route.distance_km), 2),
            city=route.city or "Hà Nội",
            icon_key=route.icon_key or "star",
            points=[RoutePoint(**pt) for pt in parse_route_points(route.points_json)],
        )
        for route in routes
    ]
