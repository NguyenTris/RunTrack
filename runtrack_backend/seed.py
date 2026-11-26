from datetime import datetime, timedelta, timezone

from sqlalchemy.orm import Session

from auth import get_password_hash
from community_routes import ensure_default_gps_art_routes
from database import Base, SessionLocal, engine
from models import (
    ActivityDB,
    ClubDB,
    ClubEventDB,
    ClubMemberDB,
    GpsArtRouteDB,
    UserDB,
    UserProfileDB,
)
from services import (
    ensure_activities_route_points_column,
    ensure_default_clubs,
    ensure_users_role_column,
)

SEED_USERS = (
    ("admin@runtrack.app", "Admin@123", "admin", "RunTrack Admin", 175.0, 70.0, 50.0),
    ("runner1@runtrack.app", "Runner@123", "user", "Nguyễn Minh Tuấn", 174.0, 68.0, 45.0),
    ("runner2@runtrack.app", "Runner@123", "user", "Trần Ngọc Mai", 163.0, 52.0, 35.0),
    ("runner3@runtrack.app", "Runner@123", "user", "Lê Hoàng Nam", 178.0, 73.0, 50.0),
)

SEED_MEMBERSHIPS = (
    ("runner1@runtrack.app", "RunTrack Ho Chi Minh"),
    ("runner1@runtrack.app", "UIT Runners"),
    ("runner2@runtrack.app", "RunTrack Ho Chi Minh"),
    ("runner2@runtrack.app", "Sunrise Runners"),
    ("runner3@runtrack.app", "Sunrise Runners"),
    ("runner3@runtrack.app", "UIT Runners"),
)

SEED_ACTIVITIES = (
    ("runner1@runtrack.app", "Chạy sáng Công viên Gia Định", 6.2, 1980, 6),
    ("runner2@runtrack.app", "Chạy nhẹ quanh hồ Đá", 4.5, 1620, 5),
    ("runner3@runtrack.app", "Tempo Run Thủ Thiêm", 8.4, 2400, 4),
    ("runner1@runtrack.app", "Chạy phục hồi buổi chiều", 5.0, 1740, 3),
    ("runner2@runtrack.app", "Interval 5K sáng sớm", 5.2, 1680, 2),
    ("runner3@runtrack.app", "Long Run cuối tuần", 12.5, 4200, 1),
    ("runner1@runtrack.app", "Khởi động đầu ngày", 4.8, 1560, 0),
)

SEED_CLUB_EVENTS = (
    ("RunTrack Ho Chi Minh", "Long Run Công Viên Gia Định 10K", "Chủ Nhật 05:30", "Công viên Gia Định, Phú Nhuận"),
    ("Sunrise Runners", "Sunrise Tempo Cầu Ba Son", "Thứ Bảy 05:15", "Công viên Bến Bạch Đằng, Quận 1"),
    ("UIT Runners", "Chạy Giao Lưu Hồ Đá 5K", "Chủ Nhật 06:00", "Cổng chính ĐH CNTT (UIT), Thủ Đức"),
)


def _upsert_user(db: Session, row: tuple) -> UserDB:
    email, password, role, full_name, height_cm, weight_kg, goal_km = row
    user = db.query(UserDB).filter(UserDB.email == email).first()
    if not user:
        user = UserDB(email=email, password_hash=get_password_hash(password), role=role)
        db.add(user)
        db.commit()
        db.refresh(user)
    elif user.role != role:
        user.role = role
        db.commit()

    profile = db.query(UserProfileDB).filter(UserProfileDB.user_id == user.id).first()
    if not profile:
        db.add(
            UserProfileDB(
                user_id=user.id,
                full_name=full_name,
                height_cm=height_cm,
                weight_kg=weight_kg,
                weekly_goal_km=goal_km,
            )
        )
        db.commit()
    return user


def _seed_memberships(db: Session, users_by_email: dict[str, UserDB], clubs_by_name: dict[str, ClubDB]) -> None:
    created = False
    for email, club_name in SEED_MEMBERSHIPS:
        user = users_by_email.get(email)
        club = clubs_by_name.get(club_name)
        if not user or not club:
            continue
        exists = (
            db.query(ClubMemberDB)
            .filter(ClubMemberDB.club_id == club.id, ClubMemberDB.user_id == user.id)
            .first()
        )
        if not exists:
            db.add(ClubMemberDB(club_id=club.id, user_id=user.id))
            created = True
    if created:
        db.commit()


def _seed_activities(db: Session, users_by_email: dict[str, UserDB]) -> None:
    now_utc = datetime.now(timezone.utc)
    created = False
    for email, title, distance_km, duration_sec, days_ago in SEED_ACTIVITIES:
        user = users_by_email.get(email)
        if not user:
            continue
        exists = (
            db.query(ActivityDB)
            .filter(ActivityDB.user_id == user.id, ActivityDB.title == title)
            .first()
        )
        if not exists:
            db.add(
                ActivityDB(
                    user_id=user.id,
                    title=title,
                    distance_km=distance_km,
                    duration_seconds=duration_sec,
                    avg_pace_seconds=int(duration_sec / distance_km),
                    calories_burned=int(distance_km * 65),
                    start_time=now_utc - timedelta(days=days_ago, hours=2),
                )
            )
            created = True
    if created:
        db.commit()


def _seed_club_events(db: Session, clubs_by_name: dict[str, ClubDB]) -> None:
    created = False
    for club_name, title, schedule_text, location in SEED_CLUB_EVENTS:
        club = clubs_by_name.get(club_name)
        if not club:
            continue
        exists = (
            db.query(ClubEventDB)
            .filter(ClubEventDB.club_id == club.id, ClubEventDB.title == title)
            .first()
        )
        if not exists:
            db.add(
                ClubEventDB(
                    club_id=club.id,
                    title=title,
                    schedule_text=schedule_text,
                    location=location,
                )
            )
            created = True
    if created:
        db.commit()


def seed_database(db: Session) -> dict:
    """Khởi tạo dữ liệu mẫu RunTrack (idempotent, chạy nhiều lần không trùng lặp)."""
    users_by_email = {row[0]: _upsert_user(db, row) for row in SEED_USERS}
    clubs = ensure_default_clubs(db)
    clubs_by_name = {club.name: club for club in clubs}
    _seed_memberships(db, users_by_email, clubs_by_name)
    _seed_activities(db, users_by_email)
    _seed_club_events(db, clubs_by_name)
    ensure_default_gps_art_routes(db)
    return {
        "users": db.query(UserDB).count(),
        "clubs": db.query(ClubDB).count(),
        "club_members": db.query(ClubMemberDB).count(),
        "club_events": db.query(ClubEventDB).count(),
        "gps_art_routes": db.query(GpsArtRouteDB).count(),
        "activities": db.query(ActivityDB).count(),
    }


def main() -> None:
    """Điểm chạy trực tiếp: python seed.py."""
    Base.metadata.create_all(bind=engine)
    ensure_users_role_column(engine)
    ensure_activities_route_points_column(engine)
    db = SessionLocal()
    try:
        summary = seed_database(db)
        print(f"Seed RunTrack hoàn tất: {summary}")
    finally:
        db.close()


if __name__ == "__main__":
    main()
