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
    recovery_percentage: int
    recovery_status_color: str # "green", "yellow", "red"
    recovery_status_label: str # "SẴN SÀNG", "TRUNG BÌNH", "CẦN NGHỈ"
    recovery_display_text: str # "Tối ưu (24h)" hoặc "Còn Xh nữa"
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
# --- BỔ SUNG Body ---
class UserProfileUpdate(BaseModel):
    full_name: Optional[str] = None
    height: Optional[float] = None  # Chiều cao (cm hoặc m)
    weight: Optional[float] = None  # Cân nặng (kg)
    weekly_goal_km: Optional[float] = None  # Mục tiêu chạy tuần (km)

class HomeStatsResponse(BaseModel):
    total_distance_km: float
    weekly_goal_km: float
    completion_percentage: int

class ActivityItem(BaseModel):
    id: int
    distance_km: float
    duration_minutes: int
    run_date: datetime
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
# ==========================================
# 3.1. BỔ SUNG: CẬP NHẬT HỒ SƠ (PUT /user/profile)
# ==========================================
@app.put("/user/profile", response_model=UserProfileResponse)
async def update_user_profile(data: UserProfileUpdate, authorization: str = Header(None)):
    """
    API cập nhật thông tin cá nhân và mục tiêu chạy bộ của người dùng.
    """
    # TODO: Thay thế đoạn này bằng câu lệnh SQL UPDATE vào bảng 'profiles' trên Database VPS
    # Ví dụ: UPDATE profiles SET full_name = :full_name, height = :height, ... WHERE user_id = :user_id
    
    # Đoạn code mô phỏng trả về dữ liệu sau khi update thành công:
    last_workout = datetime.now() - timedelta(hours=10)
    return UserProfileResponse(
        full_name=data.full_name or "Nguyễn Võ Minh Trí",
        last_workout_time=last_workout,
        recovery_completed=True,
        recovery_hours_remaining=0.0,
        weekly_progress=WeeklyProgress(
            monday=True, tuesday=True, wednesday=False, 
            thursday=True, friday=False, saturday=False, sunday=False
        ),
        progress_bar_percentage=57
    )


# ==========================================
# 3.2. BỔ SUNG: THỐNG KÊ TRANG CHỦ (GET /dashboard/home-stats)
# ==========================================
@app.get("/dashboard/home-stats", response_model=HomeStatsResponse)
async def get_home_stats(authorization: str = Header(None)):
    """
    API trả về tổng số km đã chạy trong tuần và mục tiêu tuần của người dùng.
    """
    # TODO: Viết SQL query tính tổng distance_km từ bảng 'run_activities' trong tuần hiện tại
    # và lấy 'weekly_goal_km' từ bảng 'profiles'.
    # Ví dụ SQL: SELECT SUM(distance_km) FROM run_activities WHERE user_id = :user_id AND ...
    
    # Mock data mẫu:
    total_km = 18.5  # Tổng km đã chạy tuần này
    goal_km = 30.0   # Mục tiêu tuần
    percentage = int((total_km / goal_km) * 100) if goal_km > 0 else 0

    return HomeStatsResponse(
        total_distance_km=total_km,
        weekly_goal_km=goal_km,
        completion_percentage=percentage
    )


# ==========================================
# 3.3. BỔ SUNG: DANH SÁCH HOẠT ĐỘNG GẦN ĐÂY (GET /dashboard/recent-activities)
# ==========================================
@app.get("/dashboard/recent-activities", response_model=list[ActivityItem])
async def get_recent_activities(limit: int = 5, authorization: str = Header(None)):
    """
    API lấy danh sách các buổi chạy gần đây nhất của người dùng.
    """
    # TODO: Viết SQL query từ bảng 'run_activities'
    # Ví dụ SQL: SELECT id, distance_km, duration_minutes, run_date FROM run_activities 
    #            WHERE user_id = :user_id ORDER BY run_date DESC LIMIT :limit
    
    # Mock data mẫu:
    mock_activities = [
        ActivityItem(id=1, distance_km=5.2, duration_minutes=28, run_date=datetime.now() - timedelta(days=1)),
        ActivityItem(id=2, distance_km=4.0, duration_minutes=22, run_date=datetime.now() - timedelta(days=3)),
        ActivityItem(id=3, distance_km=6.5, duration_minutes=35, run_date=datetime.now() - timedelta(days=5)),
    ]
    
    return mock_activities[:limit]
@app.get("/user/profile", response_model=UserProfileResponse)
async def get_user_profile(authorization: str = Header(None)):
    # Mock data: Lấy thông tin user từ Database (buổi chạy trước cách đây 22.1 tiếng để đạt 92% readiness)
    last_workout = datetime.now() - timedelta(hours=22.1)
    
    # --- XỬ LÝ LOGIC PHỤC HỒI (24 GIỜ LÀ 100%) ---
    time_since_last_workout = datetime.now() - last_workout
    hours_passed = time_since_last_workout.total_seconds() / 3600
    recovery_hours_remaining = max(0.0, 24.0 - hours_passed)
    recovery_completed = recovery_hours_remaining <= 0

    # Tính phần trăm phục hồi: 24 giờ = 100%
    recovery_percentage = min(100, int((hours_passed / 24.0) * 100))

    # Đánh giá màu sắc & trạng thái
    if recovery_percentage > 85:
        recovery_status_color = "green"
        recovery_status_label = "SẴN SÀNG"
        recovery_display_text = "Tối ưu (24h)"
    elif recovery_percentage > 60:
        recovery_status_color = "yellow"
        recovery_status_label = "TRUNG BÌNH"
        recovery_display_text = f"Còn {recovery_hours_remaining:.1f}h ({recovery_percentage}%)"
    else:
        recovery_status_color = "red"
        recovery_status_label = "CẦN NGHỈ"
        recovery_display_text = f"Còn {recovery_hours_remaining:.1f}h ({recovery_percentage}%)"

    # --- XỬ LÝ LOGIC TIẾN TRÌNH TUẦN (7 NGÀY) ---
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
        full_name="Hùng Runner",
        last_workout_time=last_workout,
        recovery_completed=recovery_completed,
        recovery_hours_remaining=round(recovery_hours_remaining, 1),
        recovery_percentage=recovery_percentage,
        recovery_status_color=recovery_status_color,
        recovery_status_label=recovery_status_label,
        recovery_display_text=recovery_display_text,
        weekly_progress=WeeklyProgress(**weekly_data),
        progress_bar_percentage=progress_percentage
    )

# ==========================================
# 4. EXTERNAL APIs (WEATHER & AIR POLLUTION)
# ==========================================
def calculate_us_aqi_pm25(pm25: float) -> int:
    """
    Tính chỉ số US AQI chuẩn EPA từ nồng độ PM2.5 (ug/m3)
    """
    breakpoints = [
        (0.0, 12.0, 0, 50),
        (12.1, 35.4, 51, 100),
        (35.5, 55.4, 101, 150),
        (55.5, 150.4, 151, 200),
        (150.5, 250.4, 201, 300),
        (250.5, 500.4, 301, 500)
    ]
    for c_low, c_high, i_low, i_high in breakpoints:
        if c_low <= pm25 <= c_high:
            return round(((i_high - i_low) / (c_high - c_low)) * (pm25 - c_low) + i_low)
    return 300 if pm25 > 500.4 else 0

@app.get("/weather")
async def get_weather(lat: float, lon: float):
    """
    Tích hợp OpenWeatherMap API: Current Weather & Air Pollution AQI
    """
    api_key = "4a537541384c5250c53db97422d7a283"
    weather_url = f"https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={api_key}&units=metric&lang=vi"
    pollution_url = f"https://api.openweathermap.org/data/2.5/air_pollution?lat={lat}&lon={lon}&appid={api_key}"
    
    async with httpx.AsyncClient(timeout=5.0) as client:
        # Gọi song song weather và air pollution
        weather_res = await client.get(weather_url)
        if weather_res.status_code != 200:
            raise HTTPException(status_code=400, detail="Không thể tải dữ liệu thời tiết")
        
        weather_data = weather_res.json()
        
        # Lấy AQI thực tế từ API Air Pollution nếu có
        aqi = 32 # Default fallback nếu API air pollution gặp sự cố
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
            "weather_main": weather_data["weather"][0]["main"], # "Clear", "Rain", "Clouds"
            "icon": weather_data["weather"][0]["icon"],
            "aqi": aqi
        }

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