from contextlib import asynccontextmanager
from datetime import datetime, timezone
from typing import List, Optional

from fastapi import Depends, FastAPI, Header, HTTPException, Query, status
from sqlalchemy import or_
from sqlalchemy.orm import Session

from auth import (
    create_access_token,
    decode_token_payload,
    extract_bearer_token,
    get_current_user,
    get_password_hash,
    revoke_token,
    verify_password,
)
from database import Base, engine, get_db
from models import ActivityDB, UserDB, UserProfileDB
from schemas import (
    ActivityCreate,
    ActivityItem,
    ClubResponse,
    ForgotPassword,
    FriendResponse,
    HomeStatsResponse,
    PasswordChange,
    UserLogin,
    UserProfileResponse,
    UserProfileUpdate,
    UserRegister,
    WeatherResponse,
    WeeklyActivitiesResponse,
)
from services import (
    build_user_profile_response,
    build_weekly_activities_response,
    ensure_utc,
    fetch_weather_and_aqi,
    get_week_activities,
    to_activity_item,
)


@asynccontextmanager
async def lifespan(_app: FastAPI):
    """Đảm bảo các bảng ORM (bao gồm token_blacklist) sẵn sàng khi khởi động."""
    Base.metadata.create_all(bind=engine)
    yield


app = FastAPI(title="RunTrack Backend API (SQL Schema)", lifespan=lifespan)


# ==========================================
# 1. AUTHENTICATION APIs
# ==========================================
@app.post("/auth/register")
async def register(data: UserRegister, db: Session = Depends(get_db)):
    existing_user = db.query(UserDB).filter(UserDB.email == data.email).first()
    if existing_user:
        raise HTTPException(status_code=400, detail="Email đã được đăng ký hệ thống")

    new_user = UserDB(email=data.email, password_hash=get_password_hash(data.password))
    db.add(new_user)
    db.commit()
    db.refresh(new_user)

    new_profile = UserProfileDB(
        user_id=new_user.id,
        full_name=data.full_name,
        height_cm=175.0,
        weight_kg=70.0,
        weekly_goal_km=40.0,
    )
    db.add(new_profile)
    db.commit()

    return {"message": "Đăng ký thành công", "user": data.email}


@app.post("/auth/login")
async def login(data: UserLogin, db: Session = Depends(get_db)):
    user = db.query(UserDB).filter(UserDB.email == data.email).first()
    if not user or not verify_password(data.password, user.password_hash):
        raise HTTPException(status_code=401, detail="Email hoặc mật khẩu không chính xác")

    access_token = create_access_token(data={"sub": user.email})
    return {"access_token": access_token, "token_type": "bearer"}


@app.post("/auth/logout")
async def logout(
    authorization: Optional[str] = Header(None),
    db: Session = Depends(get_db),
):
    token = extract_bearer_token(authorization)
    decode_token_payload(token)
    revoke_token(token, db)
    return {"message": "Đăng xuất thành công"}


@app.put("/auth/change-password")
async def change_password(
    data: PasswordChange,
    current_user: UserDB = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    if not verify_password(data.old_password, current_user.password_hash):
        raise HTTPException(status_code=400, detail="Mật khẩu hiện tại không chính xác")

    current_user.password_hash = get_password_hash(data.new_password)
    db.commit()
    return {"message": "Đổi mật khẩu thành công"}


@app.post("/auth/forgot-password")
async def forgot_password(data: ForgotPassword):
    return {"message": f"Hướng dẫn đặt lại mật khẩu đã được gửi tới {data.email}"}


# ==========================================
# 2. USER & TRACKING APIs (Sẽ hoàn thiện ở các bước tiếp theo)
# ==========================================
@app.get("/user/profile", response_model=UserProfileResponse)
async def get_user_profile(
    current_user: UserDB = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    profile = db.query(UserProfileDB).filter(UserProfileDB.user_id == current_user.id).first()
    return build_user_profile_response(current_user, profile, db)


@app.put("/user/profile", response_model=UserProfileResponse)
async def update_user_profile(
    data: UserProfileUpdate,
    current_user: UserDB = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    profile = db.query(UserProfileDB).filter(UserProfileDB.user_id == current_user.id).first()
    if not profile:
        profile = UserProfileDB(user_id=current_user.id)
        db.add(profile)

    if data.full_name is not None:
        profile.full_name = data.full_name
    if data.height is not None:
        profile.height_cm = data.height
    if data.weight is not None:
        profile.weight_kg = data.weight
    if data.weekly_goal_km is not None:
        profile.weekly_goal_km = data.weekly_goal_km
    db.commit()
    db.refresh(profile)

    return build_user_profile_response(current_user, profile, db)


@app.get("/dashboard/home-stats", response_model=HomeStatsResponse)
async def get_home_stats(
    current_user: UserDB = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    profile = db.query(UserProfileDB).filter(UserProfileDB.user_id == current_user.id).first()
    goal_km = float(profile.weekly_goal_km) if profile and profile.weekly_goal_km else 40.0

    _, weekly_activities = get_week_activities(current_user.id, db)
    total_km = round(sum(float(act.distance_km) for act in weekly_activities), 2)
    percentage = int((total_km / goal_km) * 100) if goal_km > 0 else 0

    active_days = []
    for act in weekly_activities:
        if act.start_time:
            run_dt = ensure_utc(act.start_time).astimezone(VN_TZ)
            iso_wd = run_dt.isoweekday()
            mapped_day = iso_wd + 1 if iso_wd <= 7 else 2
            if mapped_day not in active_days:
                active_days.append(mapped_day)

    return HomeStatsResponse(
        total_distance_km=total_km,
        weekly_goal_km=goal_km,
        completion_percentage=percentage,
        active_days=active_days,
    )


@app.get("/dashboard/recent-activities", response_model=List[ActivityItem])
async def get_recent_activities(
    limit: int = Query(default=5, ge=1, le=50),
    current_user: UserDB = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    activities = (
        db.query(ActivityDB)
        .filter(ActivityDB.user_id == current_user.id)
        .order_by(ActivityDB.start_time.desc())
        .limit(limit)
        .all()
    )
    return [to_activity_item(act) for act in activities]


@app.post("/activities", response_model=ActivityItem, status_code=status.HTTP_201_CREATED)
async def create_activity(
    data: ActivityCreate,
    current_user: UserDB = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    avg_pace = (
        data.avg_pace_seconds
        if data.avg_pace_seconds is not None
        else int(data.duration_seconds / data.distance_km)
    )
    calories = (
        data.calories_burned
        if data.calories_burned is not None
        else int(data.distance_km * 65)
    )
    start_time_utc = ensure_utc(data.start_time) if data.start_time else datetime.now(timezone.utc)

    activity = ActivityDB(
        user_id=current_user.id,
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
    return to_activity_item(activity)


@app.get("/activities/weekly", response_model=WeeklyActivitiesResponse)
async def get_weekly_activities(
    current_user: UserDB = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    return build_weekly_activities_response(current_user.id, db)


@app.get("/weather", response_model=WeatherResponse)
async def get_weather(lat: float, lon: float):
    return await fetch_weather_and_aqi(lat, lon)


@app.get("/search/friends", response_model=List[FriendResponse])
async def search_friends(
    q: str = "",
    page: int = Query(default=1, ge=1),
    limit: int = Query(default=10, ge=1, le=50),
    current_user: UserDB = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    query = (
        db.query(UserDB, UserProfileDB)
        .outerjoin(UserProfileDB, UserDB.id == UserProfileDB.user_id)
        .filter(UserDB.id != current_user.id)
    )
    keyword = q.strip()
    if keyword:
        pattern = f"%{keyword}%"
        query = query.filter(
            or_(
                UserProfileDB.full_name.ilike(pattern),
                UserDB.email.ilike(pattern),
            )
        )

    rows = query.order_by(UserDB.id.asc()).offset((page - 1) * limit).limit(limit).all()
    return [
        FriendResponse(
            user_id=user.id,
            full_name=(profile.full_name if profile and profile.full_name else user.email.split("@")[0]),
            avatar_url=(profile.avatar_url if profile and profile.avatar_url else "https://example.com/default-avatar.jpg"),
            mutual_friends=0,
            friend_status="none",
        )
        for user, profile in rows
    ]


@app.get("/search/clubs", response_model=List[ClubResponse])
async def search_clubs(
    q: str = "",
    page: int = Query(default=1, ge=1),
    limit: int = Query(default=10, ge=1, le=50),
    current_user: UserDB = Depends(get_current_user),
):
    clubs_catalog = [
        {"club_id": 1, "name": "RunTrack Ho Chi Minh", "description": "Cộng đồng chạy bộ lớn nhất HCM.", "member_count": 1250, "is_joined": True},
        {"club_id": 2, "name": "Sunrise Runners", "description": "Tập trung chạy dài vào sáng Chủ Nhật hàng tuần.", "member_count": 430, "is_joined": False},
        {"club_id": 3, "name": "UIT Runners", "description": "Club dành cho sinh viên và cựu sinh viên UIT.", "member_count": 890, "is_joined": False},
    ]
    keyword = q.strip().lower()
    results = [club for club in clubs_catalog if keyword in club["name"].lower()]
    start = (page - 1) * limit
    return [ClubResponse(**club) for club in results[start:start + limit]]
