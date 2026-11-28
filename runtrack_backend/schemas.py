from datetime import datetime
from typing import List, Literal, Optional
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


class ForgotPasswordResponse(BaseModel):
    message: str
    demo_otp: Optional[str] = None


class ResetPasswordRequest(BaseModel):
    email: EmailStr
    otp: str = Field(..., min_length=4, max_length=10)
    new_password: str = Field(..., min_length=1)


class WeeklyProgress(BaseModel):
    monday: bool
    tuesday: bool
    wednesday: bool
    thursday: bool
    friday: bool
    saturday: bool
    sunday: bool


class UserResponse(BaseModel):
    id: int
    email: EmailStr
    full_name: str
    avatar_url: Optional[str] = None


class UserProfileResponse(BaseModel):
    id: Optional[int] = None
    email: Optional[str] = None
    full_name: str
    avatar_url: Optional[str] = None
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
    active_days: List[int] = Field(default_factory=list)


class RoutePoint(BaseModel):
    lat: float = Field(..., ge=-90, le=90)
    lng: float = Field(..., ge=-180, le=180)


class ActivityItem(BaseModel):
    id: str
    title: str = "Buổi chạy bộ"
    distance_km: float
    duration_minutes: int
    run_date: str
    route_points: List[RoutePoint] = Field(default_factory=list)


class ActivityCreate(BaseModel):
    title: str = Field(default="Buổi chạy bộ", min_length=1, max_length=150)
    distance_km: float = Field(..., gt=0)
    duration_seconds: int = Field(..., gt=0)
    avg_pace_seconds: Optional[int] = Field(default=None, ge=0)
    calories_burned: Optional[int] = Field(default=None, ge=0)
    start_time: Optional[datetime] = None
    route_points: List[RoutePoint] = Field(default_factory=list)


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


class AdminDailyStat(BaseModel):
    date: str
    activities_count: int
    distance_km: float


class AdminTopRunner(BaseModel):
    user_id: int
    full_name: str
    email: str
    total_distance_km: float
    activities_count: int


class AdminStatsResponse(BaseModel):
    total_users: int
    total_activities: int
    total_distance_km: float
    total_calories: int
    total_clubs: int
    daily_stats: List[AdminDailyStat]
    top_runners: List[AdminTopRunner]


class AdminUserItem(BaseModel):
    id: int
    email: str
    full_name: str
    role: str
    height_cm: Optional[float] = None
    weight_kg: Optional[float] = None
    weekly_goal_km: float = 40.0
    activities_count: int = 0
    total_distance_km: float = 0.0
    created_at: Optional[str] = None


class AdminUserCreate(BaseModel):
    email: EmailStr
    password: str = Field(..., min_length=1)
    full_name: str = Field(..., min_length=1, max_length=100)
    role: Literal["user", "admin"] = "user"
    weekly_goal_km: float = Field(default=40.0, gt=0)


class AdminUserUpdate(BaseModel):
    full_name: Optional[str] = Field(default=None, min_length=1, max_length=100)
    role: Optional[Literal["user", "admin"]] = None
    height_cm: Optional[float] = Field(default=None, gt=0)
    weight_kg: Optional[float] = Field(default=None, gt=0)
    weekly_goal_km: Optional[float] = Field(default=None, gt=0)


class AdminActivityItem(BaseModel):
    id: str
    user_id: int
    user_email: str
    user_full_name: str
    title: str
    distance_km: float
    duration_seconds: int
    avg_pace_seconds: int
    calories_burned: int
    start_time: str


class AdminActivityCreate(BaseModel):
    user_id: int = Field(..., ge=1)
    title: str = Field(default="Buổi chạy bộ", min_length=1, max_length=150)
    distance_km: float = Field(..., gt=0)
    duration_seconds: int = Field(..., gt=0)
    avg_pace_seconds: Optional[int] = Field(default=None, ge=0)
    calories_burned: Optional[int] = Field(default=None, ge=0)
    start_time: Optional[datetime] = None


class AdminActivityUpdate(BaseModel):
    title: Optional[str] = Field(default=None, min_length=1, max_length=150)
    distance_km: Optional[float] = Field(default=None, gt=0)
    duration_seconds: Optional[int] = Field(default=None, gt=0)
    avg_pace_seconds: Optional[int] = Field(default=None, ge=0)
    calories_burned: Optional[int] = Field(default=None, ge=0)


class AdminClubCreate(BaseModel):
    name: str = Field(..., min_length=1, max_length=120)
    description: str = Field(default="", max_length=1000)


class AdminClubUpdate(BaseModel):
    name: Optional[str] = Field(default=None, min_length=1, max_length=120)
    description: Optional[str] = Field(default=None, max_length=1000)


class MonthDayHeat(BaseModel):
    day: int
    date: str
    distance_km: float
    activities_count: int
    heat_level: int


class ProgressRecordItem(BaseModel):
    label: str
    value: str
    date: str


class ProgressMonthResponse(BaseModel):
    year: int
    month: int
    first_weekday: int
    days_in_month: int
    total_distance_km: float
    total_activities: int
    total_duration_minutes: int
    days: List[MonthDayHeat]
    records: List[ProgressRecordItem]


class QuarterStatItem(BaseModel):
    quarter: str
    distance_km: float
    target_km: float
    completion_percentage: int


class AchievementItem(BaseModel):
    title: str
    subtitle: str
    unlocked: bool


class ProgressYearResponse(BaseModel):
    year: int
    total_distance_km: float
    total_activities: int
    quarters: List[QuarterStatItem]
    records: List[ProgressRecordItem]
    achievements: List[AchievementItem]


class LeaderboardEntry(BaseModel):
    rank: int
    user_id: int
    full_name: str
    total_distance_km: float
    total_activities: int
    avg_pace_display: str
    is_current_user: bool


class LeaderboardResponse(BaseModel):
    scope: str
    total_runners: int
    current_user_rank: Optional[int] = None
    entries: List[LeaderboardEntry]


class ClubMemberItem(BaseModel):
    user_id: int
    full_name: str
    weekly_km: float
    joined_at: Optional[str] = None


class ClubEventItem(BaseModel):
    event_id: int
    club_id: int
    title: str
    schedule_text: str
    location: str
    registered_count: int
    is_registered: bool


class ClubDetailResponse(BaseModel):
    club_id: int
    name: str
    description: str
    member_count: int
    is_joined: bool
    members: List[ClubMemberItem]
    events: List[ClubEventItem]


class GpsArtRouteItem(BaseModel):
    id: int
    name: str
    distance_km: float
    city: str
    icon_key: str
    points: List[RoutePoint]


class ConversationResponse(BaseModel):
    conversation_id: str
    name: str
    last_message: str
    time: str
    created_at: Optional[str] = None
    unread_count: int = 0
    is_online: bool = False
    is_group: bool = False
    category: str = "friends"
    receiver_id: Optional[int] = None
    club_id: Optional[int] = None


class ChatMessageResponse(BaseModel):
    id: str
    conversation_id: str
    sender_id: int
    sender_name: str
    receiver_id: Optional[int] = None
    club_id: Optional[int] = None
    content: str
    created_at: str
    is_from_me: bool
    time: str


class SendMessageRequest(BaseModel):
    conversation_id: Optional[str] = None
    receiver_id: Optional[int] = None
    club_id: Optional[int] = None
    content: str = Field(..., min_length=1)

