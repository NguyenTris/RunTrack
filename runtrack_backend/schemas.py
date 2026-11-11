from datetime import datetime
from typing import Optional
from pydantic import BaseModel, EmailStr, Field


class UserRegister(BaseModel):
    email: EmailStr
    password: str = Field(..., min_length=1)
    full_name: str = Field(..., min_length=1)


class UserLogin(BaseModel):
    email: EmailStr
    password: str = Field(..., min_length=1)


class PasswordChange(BaseModel):
    old_password: str = Field(..., min_length=1)
    new_password: str = Field(..., min_length=1)


class ForgotPassword(BaseModel):
    email: EmailStr


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
    height: Optional[float] = None
    weight: Optional[float] = None
    weekly_goal_km: float = 40.0
    last_workout_time: Optional[str] = None
    recovery_completed: bool
    recovery_hours_remaining: float
    recovery_percentage: int
    recovery_status_color: str
    recovery_status_label: str
    recovery_display_text: str
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
    active_days: List[int] = []


class ActivityItem(BaseModel):
    id: str
    title: str = "Buổi chạy bộ"
    distance_km: float
    duration_minutes: int
    run_date: str


class ActivityCreate(BaseModel):
    title: str = Field(default="Buổi chạy bộ", min_length=1, max_length=150)
    distance_km: float = Field(..., gt=0)
    duration_seconds: int = Field(..., gt=0)
    avg_pace_seconds: Optional[int] = Field(default=None, ge=0)
    calories_burned: Optional[int] = Field(default=None, ge=0)
    start_time: Optional[datetime] = None


class WeeklyActivityDay(BaseModel):
    day: str
    date: str
    distance_km: float
    duration_minutes: int
    completed: bool


class WeeklyActivitiesResponse(BaseModel):
    week_start: str
    week_end: str
    total_distance_km: float
    days: list[WeeklyActivityDay]


class WeatherResponse(BaseModel):
    city: str
    temperature: float
    humidity: int
    condition: str
    weather_main: str
    icon: str
    aqi: int


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
