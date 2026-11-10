from fastapi import FastAPI, HTTPException, Header, Depends
from pydantic import BaseModel
from typing import Optional
from datetime import datetime, timedelta
import httpx

app = FastAPI(title="RunTrack Backend API")

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
    last_workout_time: Optional[datetime]
    recovery_completed: bool
    recovery_hours_remaining: float
    weekly_progress: WeeklyProgress
    progress_bar_percentage: int

class FriendResponse(BaseModel):
    user_id: int
    full_name: str
    avatar_url: str
    mutual_friends: int
    friend_status: str # "none", "pending", "friends"

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
    # Logic lưu DB tại đây
    return {"message": "Đăng ký thành công", "user": data.email}

@app.post("/auth/login")
async def login(data: UserLogin):
    # Logic verify mật khẩu -> tạo JWT
    return {"access_token": "mock_jwt_token_123", "token_type": "bearer"}

@app.post("/auth/logout")
async def logout(authorization: str = Header(None)):
    # Đưa token vào blacklist hoặc client tự xóa
    return {"message": "Đăng xuất thành công"}

@app.put("/auth/change-password")
async def change_password(data: PasswordChange, authorization: str = Header(None)):
    # Verify token & update password
    return {"message": "Đổi mật khẩu thành công"}

@app.post("/auth/forgot-password")
async def forgot_password(data: ForgotPassword):
    # Tạo mã OTP và gửi email
    return {"message": f"Hướng dẫn đặt lại mật khẩu đã được gửi tới {data.email}"}

# ==========================================
# 3. USER & TRACKING APIs
# ==========================================
@app.get("/user/profile", response_model=UserProfileResponse)
async def get_user_profile(authorization: str = Header(None)):
    # Mock data: Lấy thông tin user từ Database
    last_workout = datetime.now() - timedelta(hours=10) # Lần chạy cuối cách đây 10 tiếng
    
    # --- XỬ LÝ LOGIC PHỤC HỒI (24 GIỜ) ---
    time_since_last_workout = datetime.now() - last_workout
    hours_passed = time_since_last_workout.total_seconds() / 3600
    recovery_hours_remaining = max(0.0, 24.0 - hours_passed)
    recovery_completed = recovery_hours_remaining <= 0

    # --- XỬ LÝ LOGIC TIẾN TRÌNH TUẦN (7 NGÀY) ---
    # True = Ngày đó có hoàn thành nhiệm vụ (được tick)
    weekly_data = {
        "monday": True,
        "tuesday": True,
        "wednesday": False,
        "thursday": True,
        "friday": False,
        "saturday": False,
        "sunday": False
    }
    
    # --- TÍNH PHẦN TRĂM PROGRESS BAR ---
    days_completed = sum(1 for is_completed in weekly_data.values() if is_completed)
    progress_percentage = int((days_completed / 7) * 100)

    return UserProfileResponse(
        full_name="Nguyễn Võ Minh Trí",
        last_workout_time=last_workout,
        recovery_completed=recovery_completed,
        recovery_hours_remaining=round(recovery_hours_remaining, 1),
        weekly_progress=WeeklyProgress(**weekly_data),
        progress_bar_percentage=progress_percentage
    )

# ==========================================
# 4. EXTERNAL APIs
# ==========================================
@app.get("/weather")
async def get_weather(lat: float, lon: float):
    """
    Tích hợp OpenWeatherMap API để lấy thời tiết realtime tại vị trí chạy
    """
    api_key = "4a537541384c5250c53db97422d7a283"
    url = f"https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={api_key}&units=metric&lang=vi"
    
    async with httpx.AsyncClient() as client:
        response = await client.get(url)
        if response.status_code == 200:
            data = response.json()
            return {
                "temperature": round(data["main"]["temp"], 1),
                "condition": data["weather"][0]["description"],
                "icon": data["weather"][0]["icon"],
                "city": data["name"]
            }
        raise HTTPException(status_code=400, detail="Không thể tải dữ liệu thời tiết")

        # ==========================================
# 5. SEARCH APIs (FRIENDS & CLUBS)
# ==========================================
@app.get("/search/friends", response_model=list[FriendResponse])
async def search_friends(q: str = "", page: int = 1, limit: int = 10, authorization: str = Header(None)):
    """
    Tìm kiếm người dùng khác theo tên hoặc email.
    """
    # Mock data mô phỏng kết quả query từ Database với từ khóa 'q'
    mock_db = [
        {"user_id": 101, "full_name": "Lê Văn A", "avatar_url": "https://example.com/a.jpg", "mutual_friends": 12, "friend_status": "friends"},
        {"user_id": 102, "full_name": "Trần Thị B", "avatar_url": "https://example.com/b.jpg", "mutual_friends": 5, "friend_status": "none"},
        {"user_id": 103, "full_name": "Phạm Văn C", "avatar_url": "https://example.com/c.jpg", "mutual_friends": 0, "friend_status": "pending"}
    ]
    
    # Lọc kết quả theo từ khóa (không phân biệt hoa thường)
    results = [user for user in mock_db if q.lower() in user["full_name"].lower()]
    
    # Logic phân trang
    start = (page - 1) * limit
    end = start + limit
    paginated_results = results[start:end]

    return [FriendResponse(**user) for user in paginated_results]

@app.get("/search/clubs", response_model=list[ClubResponse])
async def search_clubs(q: str = "", page: int = 1, limit: int = 10, authorization: str = Header(None)):
    """
    Tìm kiếm câu lạc bộ chạy bộ (Club) theo tên.
    """
    # Mock data mô phỏng kết quả query từ Database
    mock_db = [
        {"club_id": 1, "name": "RunTrack Ho Chi Minh", "description": "Cộng đồng chạy bộ lớn nhất HCM.", "member_count": 1250, "is_joined": True},
        {"club_id": 2, "name": "Sunday Long Run", "description": "Tập trung chạy dài vào sáng Chủ Nhật hàng tuần.", "member_count": 430, "is_joined": False},
        {"club_id": 3, "name": "UIT Runners", "description": "Club dành cho sinh viên và cựu sinh viên UIT.", "member_count": 890, "is_joined": False}
    ]
    
    # Lọc kết quả theo từ khóa
    results = [club for club in mock_db if q.lower() in club["name"].lower()]
    
    # Logic phân trang
    start = (page - 1) * limit
    end = start + limit
    paginated_results = results[start:end]

    return [ClubResponse(**club) for club in paginated_results]