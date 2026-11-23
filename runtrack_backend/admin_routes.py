from datetime import datetime, timedelta, timezone
from typing import List, Optional

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy import func, or_
from sqlalchemy.orm import Session

from auth import get_password_hash, require_admin
from database import get_db
from models import ActivityDB, ClubDB, ClubMemberDB, UserDB, UserProfileDB
from schemas import (
    AdminActivityCreate,
    AdminActivityItem,
    AdminActivityUpdate,
    AdminClubCreate,
    AdminClubUpdate,
    AdminDailyStat,
    AdminStatsResponse,
    AdminTopRunner,
    AdminUserCreate,
    AdminUserItem,
    AdminUserUpdate,
    ClubResponse,
)
from services import VN_TZ, build_club_response, ensure_utc

router = APIRouter(prefix="/admin", tags=["Admin"], dependencies=[Depends(require_admin)])


def _to_admin_user_item(user: UserDB, profile: Optional[UserProfileDB], db: Session) -> AdminUserItem:
    agg = (
        db.query(func.count(ActivityDB.id), func.coalesce(func.sum(ActivityDB.distance_km), 0.0))
        .filter(ActivityDB.user_id == user.id)
        .first()
    )
    return AdminUserItem(
        id=user.id,
        email=user.email,
        full_name=(profile.full_name if profile and profile.full_name else user.email.split("@")[0]),
        role=user.role or "user",
        height_cm=float(profile.height_cm) if profile and profile.height_cm is not None else None,
        weight_kg=float(profile.weight_kg) if profile and profile.weight_kg is not None else None,
        weekly_goal_km=float(profile.weekly_goal_km) if profile and profile.weekly_goal_km is not None else 40.0,
        activities_count=int(agg[0] or 0),
        total_distance_km=round(float(agg[1] or 0.0), 2),
        created_at=user.created_at.isoformat() if user.created_at else None,
    )


def _to_admin_activity_item(act: ActivityDB, user: UserDB, profile: Optional[UserProfileDB]) -> AdminActivityItem:
    start_dt = ensure_utc(act.start_time).astimezone(VN_TZ) if act.start_time else datetime.now(VN_TZ)
    return AdminActivityItem(
        id=str(act.id),
        user_id=act.user_id,
        user_email=user.email,
        user_full_name=(profile.full_name if profile and profile.full_name else user.email.split("@")[0]),
        title=act.title or "Buổi chạy bộ",
        distance_km=round(float(act.distance_km), 2),
        duration_seconds=int(act.duration_seconds),
        avg_pace_seconds=int(act.avg_pace_seconds or 0),
        calories_burned=int(act.calories_burned or 0),
        start_time=start_dt.isoformat(),
    )


def _build_daily_stats(db: Session) -> List[AdminDailyStat]:
    today_vn = datetime.now(VN_TZ).replace(hour=0, minute=0, second=0, microsecond=0)
    start_vn = today_vn - timedelta(days=6)
    start_utc = start_vn.astimezone(timezone.utc)
    buckets = {
        (start_vn + timedelta(days=i)).date().isoformat(): {"count": 0, "distance": 0.0}
        for i in range(7)
    }
    activities = db.query(ActivityDB).filter(ActivityDB.start_time >= start_utc).all()
    for act in activities:
        if not act.start_time:
            continue
        day_key = ensure_utc(act.start_time).astimezone(VN_TZ).date().isoformat()
        if day_key in buckets:
            buckets[day_key]["count"] += 1
            buckets[day_key]["distance"] += float(act.distance_km)
    return [
        AdminDailyStat(date=day, activities_count=vals["count"], distance_km=round(vals["distance"], 2))
        for day, vals in buckets.items()
    ]


def _get_top_runners(db: Session, limit: int = 5) -> List[AdminTopRunner]:
    total_km_col = func.coalesce(func.sum(ActivityDB.distance_km), 0.0).label("total_km")
    rows = (
        db.query(UserDB, UserProfileDB, total_km_col, func.count(ActivityDB.id))
        .outerjoin(UserProfileDB, UserDB.id == UserProfileDB.user_id)
        .outerjoin(ActivityDB, UserDB.id == ActivityDB.user_id)
        .group_by(UserDB.id, UserProfileDB.id)
        .order_by(total_km_col.desc(), UserDB.id.asc())
        .limit(limit)
        .all()
    )
    return [
        AdminTopRunner(
            user_id=user.id,
            full_name=(profile.full_name if profile and profile.full_name else user.email.split("@")[0]),
            email=user.email,
            total_distance_km=round(float(total_km or 0.0), 2),
            activities_count=int(act_count or 0),
        )
        for user, profile, total_km, act_count in rows
    ]


@router.get("/stats", response_model=AdminStatsResponse)
async def get_admin_stats(db: Session = Depends(get_db)):
    totals = db.query(
        func.count(ActivityDB.id),
        func.coalesce(func.sum(ActivityDB.distance_km), 0.0),
        func.coalesce(func.sum(ActivityDB.calories_burned), 0),
    ).first()
    return AdminStatsResponse(
        total_users=db.query(UserDB).count(),
        total_activities=int(totals[0] or 0),
        total_distance_km=round(float(totals[1] or 0.0), 2),
        total_calories=int(totals[2] or 0),
        total_clubs=db.query(ClubDB).count(),
        daily_stats=_build_daily_stats(db),
        top_runners=_get_top_runners(db),
    )


@router.get("/users", response_model=List[AdminUserItem])
async def list_admin_users(
    q: str = "",
    page: int = Query(default=1, ge=1),
    limit: int = Query(default=20, ge=1, le=100),
    db: Session = Depends(get_db),
):
    query = db.query(UserDB, UserProfileDB).outerjoin(UserProfileDB, UserDB.id == UserProfileDB.user_id)
    keyword = q.strip()
    if keyword:
        pattern = f"%{keyword}%"
        query = query.filter(or_(UserDB.email.ilike(pattern), UserProfileDB.full_name.ilike(pattern)))
    rows = query.order_by(UserDB.id.asc()).offset((page - 1) * limit).limit(limit).all()
    return [_to_admin_user_item(user, profile, db) for user, profile in rows]


@router.post("/users", response_model=AdminUserItem, status_code=status.HTTP_201_CREATED)
async def create_admin_user(data: AdminUserCreate, db: Session = Depends(get_db)):
    if db.query(UserDB).filter(UserDB.email == data.email).first():
        raise HTTPException(status_code=400, detail="Email đã tồn tại trong hệ thống")
    user = UserDB(email=data.email, password_hash=get_password_hash(data.password), role=data.role)
    db.add(user)
    db.commit()
    db.refresh(user)
    profile = UserProfileDB(user_id=user.id, full_name=data.full_name, weekly_goal_km=data.weekly_goal_km)
    db.add(profile)
    db.commit()
    db.refresh(profile)
    return _to_admin_user_item(user, profile, db)


@router.put("/users/{user_id}", response_model=AdminUserItem)
async def update_admin_user(user_id: int, data: AdminUserUpdate, db: Session = Depends(get_db)):
    user = db.query(UserDB).filter(UserDB.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="Không tìm thấy người dùng")
    profile = db.query(UserProfileDB).filter(UserProfileDB.user_id == user.id).first()
    if not profile:
        profile = UserProfileDB(user_id=user.id)
        db.add(profile)
    if data.role is not None:
        user.role = data.role
    if data.full_name is not None:
        profile.full_name = data.full_name
    if data.height_cm is not None:
        profile.height_cm = data.height_cm
    if data.weight_kg is not None:
        profile.weight_kg = data.weight_kg
    if data.weekly_goal_km is not None:
        profile.weekly_goal_km = data.weekly_goal_km
    db.commit()
    db.refresh(user)
    db.refresh(profile)
    return _to_admin_user_item(user, profile, db)


@router.delete("/users/{user_id}")
async def delete_admin_user(
    user_id: int,
    current_admin: UserDB = Depends(require_admin),
    db: Session = Depends(get_db),
):
    if user_id == current_admin.id:
        raise HTTPException(status_code=400, detail="Không thể tự xóa tài khoản quản trị đang đăng nhập")
    user = db.query(UserDB).filter(UserDB.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="Không tìm thấy người dùng")
    db.query(ClubMemberDB).filter(ClubMemberDB.user_id == user_id).delete()
    db.query(ActivityDB).filter(ActivityDB.user_id == user_id).delete()
    db.query(UserProfileDB).filter(UserProfileDB.user_id == user_id).delete()
    db.delete(user)
    db.commit()
    return {"message": "Đã xóa người dùng thành công"}


@router.get("/activities", response_model=List[AdminActivityItem])
async def list_admin_activities(
    user_id: Optional[int] = Query(default=None, ge=1),
    page: int = Query(default=1, ge=1),
    limit: int = Query(default=20, ge=1, le=100),
    db: Session = Depends(get_db),
):
    query = (
        db.query(ActivityDB, UserDB, UserProfileDB)
        .join(UserDB, ActivityDB.user_id == UserDB.id)
        .outerjoin(UserProfileDB, UserDB.id == UserProfileDB.user_id)
    )
    if user_id is not None:
        query = query.filter(ActivityDB.user_id == user_id)
    rows = query.order_by(ActivityDB.start_time.desc()).offset((page - 1) * limit).limit(limit).all()
    return [_to_admin_activity_item(act, user, profile) for act, user, profile in rows]


@router.post("/activities", response_model=AdminActivityItem, status_code=status.HTTP_201_CREATED)
async def create_admin_activity(data: AdminActivityCreate, db: Session = Depends(get_db)):
    user = db.query(UserDB).filter(UserDB.id == data.user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="Không tìm thấy người dùng")
    avg_pace = (
        data.avg_pace_seconds
        if data.avg_pace_seconds is not None
        else int(data.duration_seconds / data.distance_km)
    )
    calories = data.calories_burned if data.calories_burned is not None else int(data.distance_km * 65)
    start_time_utc = ensure_utc(data.start_time) if data.start_time else datetime.now(timezone.utc)
    activity = ActivityDB(
        user_id=user.id,
        title=data.title,
        distance_km=data.distance_km,
        duration_seconds=data.duration_seconds,
        avg_pace_seconds=avg_pace,
        calories_burned=calories,
        start_time=start_time_utc,
    )
    db.add(activity)
    db.commit()
    db.refresh(activity)
    profile = db.query(UserProfileDB).filter(UserProfileDB.user_id == user.id).first()
    return _to_admin_activity_item(activity, user, profile)


@router.put("/activities/{activity_id}", response_model=AdminActivityItem)
async def update_admin_activity(activity_id: str, data: AdminActivityUpdate, db: Session = Depends(get_db)):
    activity = db.query(ActivityDB).filter(ActivityDB.id == activity_id).first()
    if not activity:
        raise HTTPException(status_code=404, detail="Không tìm thấy buổi chạy")
    if data.title is not None:
        activity.title = data.title
    if data.distance_km is not None:
        activity.distance_km = data.distance_km
    if data.duration_seconds is not None:
        activity.duration_seconds = data.duration_seconds
    if data.avg_pace_seconds is not None:
        activity.avg_pace_seconds = data.avg_pace_seconds
    if data.calories_burned is not None:
        activity.calories_burned = data.calories_burned
    db.commit()
    db.refresh(activity)
    user = db.query(UserDB).filter(UserDB.id == activity.user_id).first()
    profile = db.query(UserProfileDB).filter(UserProfileDB.user_id == activity.user_id).first()
    return _to_admin_activity_item(activity, user, profile)


@router.delete("/activities/{activity_id}")
async def delete_admin_activity(activity_id: str, db: Session = Depends(get_db)):
    activity = db.query(ActivityDB).filter(ActivityDB.id == activity_id).first()
    if not activity:
        raise HTTPException(status_code=404, detail="Không tìm thấy buổi chạy")
    db.delete(activity)
    db.commit()
    return {"message": "Đã xóa buổi chạy thành công"}


@router.get("/clubs", response_model=List[ClubResponse])
async def list_admin_clubs(
    q: str = "",
    page: int = Query(default=1, ge=1),
    limit: int = Query(default=20, ge=1, le=100),
    db: Session = Depends(get_db),
):
    query = db.query(ClubDB)
    keyword = q.strip()
    if keyword:
        query = query.filter(ClubDB.name.ilike(f"%{keyword}%"))
    clubs = query.order_by(ClubDB.id.asc()).offset((page - 1) * limit).limit(limit).all()
    return [build_club_response(club, db) for club in clubs]


@router.post("/clubs", response_model=ClubResponse, status_code=status.HTTP_201_CREATED)
async def create_admin_club(data: AdminClubCreate, db: Session = Depends(get_db)):
    clean_name = data.name.strip()
    if not clean_name:
        raise HTTPException(status_code=422, detail="Tên câu lạc bộ không được để trống")
    if db.query(ClubDB).filter(ClubDB.name == clean_name).first():
        raise HTTPException(status_code=400, detail="Tên câu lạc bộ đã tồn tại")
    club = ClubDB(name=clean_name, description=data.description.strip())
    db.add(club)
    db.commit()
    db.refresh(club)
    return build_club_response(club, db)


@router.put("/clubs/{club_id}", response_model=ClubResponse)
async def update_admin_club(club_id: int, data: AdminClubUpdate, db: Session = Depends(get_db)):
    club = db.query(ClubDB).filter(ClubDB.id == club_id).first()
    if not club:
        raise HTTPException(status_code=404, detail="Không tìm thấy câu lạc bộ")
    if data.name is not None:
        clean_name = data.name.strip()
        if not clean_name:
            raise HTTPException(status_code=422, detail="Tên câu lạc bộ không được để trống")
        dup = db.query(ClubDB).filter(ClubDB.name == clean_name, ClubDB.id != club_id).first()
        if dup:
            raise HTTPException(status_code=400, detail="Tên câu lạc bộ đã tồn tại")
        club.name = clean_name
    if data.description is not None:
        club.description = data.description.strip()
    db.commit()
    db.refresh(club)
    return build_club_response(club, db)


@router.delete("/clubs/{club_id}")
async def delete_admin_club(club_id: int, db: Session = Depends(get_db)):
    club = db.query(ClubDB).filter(ClubDB.id == club_id).first()
    if not club:
        raise HTTPException(status_code=404, detail="Không tìm thấy câu lạc bộ")
    db.query(ClubMemberDB).filter(ClubMemberDB.club_id == club_id).delete()
    db.delete(club)
    db.commit()
    return {"message": "Đã xóa câu lạc bộ thành công"}
