from fastapi import FastAPI, HTTPException, Header, Depends
from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime, timedelta
import httpx
from passlib.context import CryptContext
import jwt

app = FastAPI(title="RunTrack Backend API")

# ==========================================
# AUTHENTICATION & SECURITY SETUP
# ==========================================
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
SECRET_KEY = "runtrack_super_secret_jwt_key_2026"
ALGORITHM = "HS256"

# In-memory user database storage
# email -> {"email": str, "password_hash": str, "full_name": str, "height": float, "weight": float, "weekly_goal_km": float}
fake_users_db = {}

def get_password_hash(password: str) -> str:
    return pwd_context.hash(password)

def verify_password(plain_password: str, hashed_password: str) -> bool:
    return pwd_context.verify(plain_password, hashed_password)

def create_access_token(data: dict, expires_delta: timedelta = timedelta(days=7)):
    to_encode = data.copy()
    expire = datetime.utcnow() + expires_delta
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

def decode_token(authorization: Optional[str] = Header(None)) -> str:
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Không tìm thấy token xác thực hoặc sai định dạng Bearer")
    token = authorization.split(" ")[1]
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        email: str = payload.get("sub")
        if email is None:
            raise HTTPException(status_code=401, detail="Token không hợp lệ")
        return email
    except jwt.PyJWTError:
        raise HTTPException(status_code=401, detail="Token hết hạn hoặc không hợp lệ")

# ==========================================
# 1. SCHEMAS (Request & Response Models)
# ==========================================
class UserRegister(BaseModel):
    email: str
    password: str
    full_name: str

class UserLogin(BaseModel):
    email: str
    password: str

class PasswordChange(BaseModel):
    old_password: str
    new_password: str

class ForgotPassword(BaseModel):
    email: str

class WeeklyProgress(BaseModel):
    monday: bool
    tuesday: bool
    wednesday: bool
    thursday: bool
    friday: bool
    saturday: bool
    sunday: bool

class UserProfileResponse(BaseModel):
    full_name: str
    last_workout_time: Optional[str] = None
    recovery_completed: bool
    recovery_hours_remaining: float
    weekly_progress: WeeklyProgress
    progress_bar_percentage: int

class UserProfileUpdate(BaseModel):
    full_name: Optional[str] = None
    height: Optional[float] = None
    weight: Optional[float] = None
    weekly_goal_km: Optional[float] = None

class HomeStatsResponse(BaseModel):
    total_distance_km: float
    weekly_goal_km: float
    completion_percentage: int

class ActivityItem(BaseModel):
    id: int
    distance_km: float
    duration_minutes: int
    run_date: str

class FriendResponse(BaseModel):
    user_id: int
    full_name: str
    avatar_url: str
    mutual_friends: int
    friend_status: str

class ClubResponse(BaseModel):
    club_id: int
    name: str
    description: str
    member_count: int
    is_joined: bool

# ==========================================
# 2. AUTHENTICATION APIs
# ==========================================
@app.post("/auth/register")
async def register(data: UserRegister):
    if data.email in fake_users_db:
        raise HTTPException(status_code=400, detail="Email đã được đăng ký hệ thống")

    hashed_password = get_password_hash(data.password)
    fake_users_db[data.email] = {
        "email": data.email,
        "password_hash": hashed_password,
        "full_name": data.full_name,
        "height": 175.0,
        "weight": 70.0,
        "weekly_goal_km": 30.0
    }
    return {"message": "Đăng ký thành công", "user": data.email}

@app.post("/auth/login")
async def login(data: UserLogin):
    user = fake_users_db.get(data.email)
    if not user or not verify_password(data.password, user["password_hash"]):
        raise HTTPException(status_code=401, detail="Email hoặc mật khẩu không chính xác")

    access_token = create_access_token(data={"sub": user["email"]})
    return {"access_token": access_token, "token_type": "bearer"}

@app.post("/auth/logout")
async def logout(authorization: Optional[str] = Header(None)):
    return {"message": "Đăng xuất thành công"}

@app.put("/auth/change-password")
async def change_password(data: PasswordChange, email: str = Depends(decode_token)):
    user = fake_users_db.get(email)
    if not user or not verify_password(data.old_password, user["password_hash"]):
        raise HTTPException(status_code=400, detail="Mật khẩu hiện tại không chính xác")

    user["password_hash"] = get_password_hash(data.new_password)
    return {"message": "Đổi mật khẩu thành công"}

@app.post("/auth/forgot-password")
async def forgot_password(data: ForgotPassword):
    return {"message": f"Hướng dẫn đặt lại mật khẩu đã được gửi tới {data.email}"}

# ==========================================
# 3. USER & DASHBOARD APIs
# ==========================================
@app.get("/user/profile", response_model=UserProfileResponse)
async def get_user_profile(email: str = Depends(decode_token)):
    user = fake_users_db.get(email, {"full_name": "Nguyễn Võ Minh Trí"})
    last_workout_str = datetime.now().isoformat()
    
    weekly_data = {
        "monday": True,
        "tuesday": True,
        "wednesday": False,
        "thursday": True,
        "friday": False,
        "saturday": False,
        "sunday": False
    }
    days_completed = sum(1 for v in weekly_data.values() if v)
    progress_percentage = int((days_completed / 7) * 100)

    return UserProfileResponse(
        full_name=user.get("full_name", "Nguyễn Võ Minh Trí"),
        last_workout_time=last_workout_str,
        recovery_completed=True,
        recovery_hours_remaining=0.0,
        weekly_progress=WeeklyProgress(**weekly_data),
        progress_bar_percentage=progress_percentage
    )

@app.put("/user/profile", response_model=UserProfileResponse)
async def update_user_profile(data: UserProfileUpdate, email: str = Depends(decode_token)):
    user = fake_users_db.get(email)
    if user:
        if data.full_name: user["full_name"] = data.full_name
        if data.height: user["height"] = data.height
        if data.weight: user["weight"] = data.weight
        if data.weekly_goal_km: user["weekly_goal_km"] = data.weekly_goal_km

    weekly_data = {
        "monday": True,
        "tuesday": True,
        "wednesday": False,
        "thursday": True,
        "friday": False,
        "saturday": False,
        "sunday": False
    }
    return UserProfileResponse(
        full_name=user.get("full_name") if user else "Nguyễn Võ Minh Trí",
        last_workout_time=datetime.now().isoformat(),
        recovery_completed=True,
        recovery_hours_remaining=0.0,
        weekly_progress=WeeklyProgress(**weekly_data),
        progress_bar_percentage=57
    )

@app.get("/dashboard/home-stats", response_model=HomeStatsResponse)
async def get_home_stats(email: str = Depends(decode_token)):
    total_km = 18.5
    goal_km = 30.0
    percentage = int((total_km / goal_km) * 100) if goal_km > 0 else 0

    return HomeStatsResponse(
        total_distance_km=total_km,
        weekly_goal_km=goal_km,
        completion_percentage=percentage
    )

@app.get("/dashboard/recent-activities", response_model=List[ActivityItem])
async def get_recent_activities(limit: int = 5, email: str = Depends(decode_token)):
    mock_activities = [
        ActivityItem(id=1, distance_km=5.2, duration_minutes=28, run_date=datetime.now().isoformat()),
        ActivityItem(id=2, distance_km=4.0, duration_minutes=22, run_date=datetime.now().isoformat()),
        ActivityItem(id=3, distance_km=6.5, duration_minutes=35, run_date=datetime.now().isoformat()),
    ]
    return mock_activities[:limit]

# ==========================================
# 4. EXTERNAL APIs (WEATHER)
# ==========================================
@app.get("/weather")
async def get_weather(lat: float, lon: float):
    api_key = "4a537541384c5250c53db97422d7a283"
    url = f"https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={api_key}&units=metric&lang=vi"
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(url)
            if response.status_code == 200:
                data = response.json()
                return {
                    "temperature": round(data["main"]["temp"], 1),
                    "condition": data["weather"][0]["description"],
                    "icon": data["weather"][0]["icon"],
                    "city": data["name"]
                }
        except Exception:
            pass

    # Fallback mock weather if external api fails or key is invalid
    return {
        "temperature": 28.5,
        "condition": "Mây thưa",
        "icon": "02d",
        "city": "Hà Nội"
    }

# ==========================================
# 5. SEARCH APIs (FRIENDS & CLUBS)
# ==========================================
@app.get("/search/friends", response_model=List[FriendResponse])
async def search_friends(q: str = "", page: int = 1, limit: int = 10, email: str = Depends(decode_token)):
    mock_db = [
        {"user_id": 101, "full_name": "Đức Huy", "avatar_url": "https://example.com/a.jpg", "mutual_friends": 12, "friend_status": "friends"},
        {"user_id": 102, "full_name": "Ngọc Mai", "avatar_url": "https://example.com/b.jpg", "mutual_friends": 5, "friend_status": "none"},
        {"user_id": 103, "full_name": "Hoàng Nam", "avatar_url": "https://example.com/c.jpg", "mutual_friends": 8, "friend_status": "pending"}
    ]
    results = [user for user in mock_db if q.lower() in user["full_name"].lower()]
    start = (page - 1) * limit
    end = start + limit
    return [FriendResponse(**user) for user in results[start:end]]

@app.get("/search/clubs", response_model=List[ClubResponse])
async def search_clubs(q: str = "", page: int = 1, limit: int = 10, email: str = Depends(decode_token)):
    mock_db = [
        {"club_id": 1, "name": "RunTrack Ho Chi Minh", "description": "Cộng đồng chạy bộ lớn nhất HCM.", "member_count": 1250, "is_joined": True},
        {"club_id": 2, "name": "Sunrise Runners", "description": "Tập trung chạy dài vào sáng Chủ Nhật hàng tuần.", "member_count": 430, "is_joined": False},
        {"club_id": 3, "name": "UIT Runners", "description": "Club dành cho sinh viên và cựu sinh viên.", "member_count": 890, "is_joined": False}
    ]
    results = [club for club in mock_db if q.lower() in club["name"].lower()]
    start = (page - 1) * limit
    end = start + limit
    return [ClubResponse(**club) for club in results[start:end]]
