from contextlib import asynccontextmanager
from datetime import datetime, timezone
from typing import List, Optional

import httpx
from fastapi import Depends, FastAPI, Header, HTTPException, Query, status
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
    WeeklyActivitiesResponse,
)
from services import (
    build_user_profile_response,
    build_weekly_activities_response,
    ensure_utc,
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
    return HomeStatsResponse(
        total_distance_km=total_km,
        weekly_goal_km=goal_km,
        completion_percentage=percentage,
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


def calculate_us_aqi_pm25(pm25: float) -> int:
    """Tính chỉ số US AQI chuẩn EPA từ nồng độ PM2.5 (ug/m3)."""
    breakpoints = [
        (0.0, 12.0, 0, 50),
        (12.1, 35.4, 51, 100),
        (35.5, 55.4, 101, 150),
        (55.5, 150.4, 151, 200),
        (150.5, 250.4, 201, 300),
        (250.5, 500.4, 301, 500),
    ]
    for c_low, c_high, i_low, i_high in breakpoints:
        if c_low <= pm25 <= c_high:
            return round(((i_high - i_low) / (c_high - c_low)) * (pm25 - c_low) + i_low)
    return 300 if pm25 > 500.4 else 0


@app.get("/weather")
async def get_weather(lat: float, lon: float):
    api_key = "4a537541384c5250c53db97422d7a283"
    weather_url = f"https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={api_key}&units=metric&lang=vi"
    pollution_url = f"https://api.openweathermap.org/data/2.5/air_pollution?lat={lat}&lon={lon}&appid={api_key}"

    async with httpx.AsyncClient(timeout=5.0) as client:
        weather_res = await client.get(weather_url)
        if weather_res.status_code != 200:
            raise HTTPException(status_code=400, detail="Không thể tải dữ liệu thời tiết")

        weather_data = weather_res.json()
        aqi = 32
        try:
            pollution_res = await client.get(pollution_url)
            if pollution_res.status_code == 200:
                p_data = pollution_res.json()
                pm25 = p_data.get("list", [{}])[0].get("components", {}).get("pm2_5", 7.8)
                aqi = calculate_us_aqi_pm25(pm25)
        except Exception:
            pass

        return {
            "city": weather_data.get("name", "Hà Nội"),
            "temperature": round(weather_data["main"]["temp"], 1),
            "humidity": weather_data["main"]["humidity"],
            "condition": weather_data["weather"][0]["description"],
            "weather_main": weather_data["weather"][0]["main"],
            "icon": weather_data["weather"][0]["icon"],
            "aqi": aqi,
        }


@app.get("/search/friends", response_model=List[FriendResponse])
async def search_friends(
    q: str = "",
    page: int = 1,
    limit: int = 10,
    current_user: UserDB = Depends(get_current_user),
):
    mock_db = [
        {"user_id": 101, "full_name": "Đức Huy", "avatar_url": "https://example.com/a.jpg", "mutual_friends": 12, "friend_status": "friends"},
        {"user_id": 102, "full_name": "Ngọc Mai", "avatar_url": "https://example.com/b.jpg", "mutual_friends": 5, "friend_status": "none"},
        {"user_id": 103, "full_name": "Hoàng Nam", "avatar_url": "https://example.com/c.jpg", "mutual_friends": 8, "friend_status": "pending"},
    ]
    results = [user for user in mock_db if q.lower() in user["full_name"].lower()]
    start = (page - 1) * limit
    end = start + limit
    return [FriendResponse(**user) for user in results[start:end]]


@app.get("/search/clubs", response_model=List[ClubResponse])
async def search_clubs(
    q: str = "",
    page: int = 1,
    limit: int = 10,
    current_user: UserDB = Depends(get_current_user),
):
    mock_db = [
        {"club_id": 1, "name": "RunTrack Ho Chi Minh", "description": "Cộng đồng chạy bộ lớn nhất HCM.", "member_count": 1250, "is_joined": True},
        {"club_id": 2, "name": "Sunrise Runners", "description": "Tập trung chạy dài vào sáng Chủ Nhật hàng tuần.", "member_count": 430, "is_joined": False},
        {"club_id": 3, "name": "UIT Runners", "description": "Club dành cho sinh viên và cựu sinh viên UIT.", "member_count": 890, "is_joined": False},
    ]
    results = [club for club in mock_db if q.lower() in club["name"].lower()]
    start = (page - 1) * limit
    end = start + limit
    return [ClubResponse(**club) for club in results[start:end]]
