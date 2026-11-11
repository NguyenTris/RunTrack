import asyncio
import os
from datetime import datetime, timedelta, timezone
from typing import Optional

import httpx
from fastapi import HTTPException
from sqlalchemy.orm import Session

from models import ActivityDB, UserDB, UserProfileDB
from schemas import (
    ActivityItem,
    UserProfileResponse,
    WeatherResponse,
    WeeklyActivitiesResponse,
    WeeklyActivityDay,
    WeeklyProgress,
)

VN_TZ = timezone(timedelta(hours=7))
DAY_KEYS = (
    "monday",
    "tuesday",
    "wednesday",
    "thursday",
    "friday",
    "saturday",
    "sunday",
)


def ensure_utc(dt: datetime) -> datetime:
    """Chuẩn hóa datetime (naive từ SQLite hoặc aware từ PostgreSQL) về UTC."""
    if dt.tzinfo is None:
        return dt.replace(tzinfo=timezone.utc)
    return dt.astimezone(timezone.utc)


def get_current_week_bounds(reference_utc: Optional[datetime] = None) -> tuple[datetime, datetime, datetime]:
    """Trả về (week_start_vn, week_start_utc, week_end_utc) cho tuần hiện tại theo múi giờ VN."""
    now_utc = reference_utc or datetime.now(timezone.utc)
    now_vn = now_utc.astimezone(VN_TZ)
    week_start_vn = now_vn.replace(hour=0, minute=0, second=0, microsecond=0) - timedelta(days=now_vn.weekday())
    week_end_vn = week_start_vn + timedelta(days=7)
    return week_start_vn, week_start_vn.astimezone(timezone.utc), week_end_vn.astimezone(timezone.utc)


def compute_recovery_metrics(
    last_workout_utc: Optional[datetime],
    now_utc: Optional[datetime] = None,
) -> dict:
    """Tính toàn bộ chỉ số phục hồi (24h = 100%) khớp với thuật toán phía Android."""
    if last_workout_utc is None:
        return {
            "last_workout_time": None,
            "recovery_completed": True,
            "recovery_hours_remaining": 0.0,
            "recovery_percentage": 100,
            "recovery_status_color": "green",
            "recovery_status_label": "SẴN SÀNG",
            "recovery_display_text": "Tối ưu (24h)",
        }

    ref_now = now_utc or datetime.now(timezone.utc)
    hours_passed = max(0.0, (ref_now - last_workout_utc).total_seconds() / 3600.0)
    hours_remaining = round(max(0.0, 24.0 - hours_passed), 1)
    completed = hours_passed >= 24.0
    percentage = min(100, int((hours_passed / 24.0) * 100))

    if percentage > 85:
        color, label, display_text = "green", "SẴN SÀNG", "Tối ưu (24h)"
    elif percentage > 60:
        color, label = "yellow", "TRUNG BÌNH"
        display_text = f"Còn {hours_remaining:.1f}h ({percentage}%)"
    else:
        color, label = "red", "CẦN NGHỈ"
        display_text = f"Còn {hours_remaining:.1f}h ({percentage}%)"

    return {
        "last_workout_time": last_workout_utc.astimezone(VN_TZ).isoformat(),
        "recovery_completed": completed,
        "recovery_hours_remaining": hours_remaining,
        "recovery_percentage": percentage,
        "recovery_status_color": color,
        "recovery_status_label": label,
        "recovery_display_text": display_text,
    }


def get_week_activities(user_id: int, db: Session) -> tuple[datetime, list[ActivityDB]]:
    """Lấy toàn bộ buổi chạy trong tuần hiện tại của user theo múi giờ VN."""
    week_start_vn, week_start_utc, week_end_utc = get_current_week_bounds()
    activities = (
        db.query(ActivityDB)
        .filter(
            ActivityDB.user_id == user_id,
            ActivityDB.start_time >= week_start_utc,
            ActivityDB.start_time < week_end_utc,
        )
        .all()
    )
    return week_start_vn, activities


def to_activity_item(act: ActivityDB) -> ActivityItem:
    """Chuyển bản ghi ORM ActivityDB sang schema ActivityItem trả về API."""
    run_dt = ensure_utc(act.start_time).astimezone(VN_TZ) if act.start_time else datetime.now(VN_TZ)
    return ActivityItem(
        id=str(act.id),
        title=act.title or "Buổi chạy bộ",
        distance_km=round(float(act.distance_km), 2),
        duration_minutes=int(act.duration_seconds // 60) if act.duration_seconds else 0,
        run_date=run_dt.isoformat(),
    )


def build_user_profile_response(
    user: UserDB,
    profile: Optional[UserProfileDB],
    db: Session,
) -> UserProfileResponse:
    """Tổng hợp hồ sơ, trạng thái phục hồi và tiến độ tuần từ dữ liệu thực tế trong DB."""
    latest_activity = (
        db.query(ActivityDB)
        .filter(ActivityDB.user_id == user.id)
        .order_by(ActivityDB.start_time.desc())
        .first()
    )
    last_workout_utc = ensure_utc(latest_activity.start_time) if latest_activity and latest_activity.start_time else None
    recovery = compute_recovery_metrics(last_workout_utc)

    _, weekly_activities = get_week_activities(user.id, db)

    weekly_flags = {day: False for day in DAY_KEYS}
    for act in weekly_activities:
        if act.start_time:
            day_idx = ensure_utc(act.start_time).astimezone(VN_TZ).weekday()
            weekly_flags[DAY_KEYS[day_idx]] = True

    days_completed = sum(1 for done in weekly_flags.values() if done)
    progress_percentage = int((days_completed / 7) * 100)

    return UserProfileResponse(
        full_name=profile.full_name if profile and profile.full_name else "Runner",
        height=float(profile.height_cm) if profile and profile.height_cm is not None else None,
        weight=float(profile.weight_kg) if profile and profile.weight_kg is not None else None,
        weekly_goal_km=float(profile.weekly_goal_km) if profile and profile.weekly_goal_km is not None else 40.0,
        weekly_progress=WeeklyProgress(**weekly_flags),
        progress_bar_percentage=progress_percentage,
        **recovery,
    )


def build_weekly_activities_response(user_id: int, db: Session) -> WeeklyActivitiesResponse:
    """Tổng hợp chi tiết từng ngày (T2..CN) trong tuần hiện tại cho biểu đồ tuần."""
    week_start_vn, weekly_activities = get_week_activities(user_id, db)
    per_day = [
        {"distance_km": 0.0, "duration_seconds": 0, "completed": False}
        for _ in range(7)
    ]

    for act in weekly_activities:
        if act.start_time:
            day_idx = ensure_utc(act.start_time).astimezone(VN_TZ).weekday()
            per_day[day_idx]["distance_km"] += float(act.distance_km)
            per_day[day_idx]["duration_seconds"] += int(act.duration_seconds or 0)
            per_day[day_idx]["completed"] = True

    days = []
    total_distance = 0.0
    for idx, day_key in enumerate(DAY_KEYS):
        day_date = (week_start_vn + timedelta(days=idx)).date().isoformat()
        dist = round(per_day[idx]["distance_km"], 2)
        total_distance += dist
        days.append(
            WeeklyActivityDay(
                day=day_key,
                date=day_date,
                distance_km=dist,
                duration_minutes=int(per_day[idx]["duration_seconds"] // 60),
                completed=per_day[idx]["completed"],
            )
        )

    week_end_date = (week_start_vn + timedelta(days=6)).date().isoformat()
    return WeeklyActivitiesResponse(
        week_start=week_start_vn.date().isoformat(),
        week_end=week_end_date,
        total_distance_km=round(total_distance, 2),
        days=days,
    )


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


async def fetch_weather_and_aqi(lat: float, lon: float) -> WeatherResponse:
    """Gọi song song OpenWeatherMap Weather & Air Pollution, trả về đủ 7 trường cho Android."""
    api_key = os.getenv("OPENWEATHER_API_KEY", "").strip()
    if not api_key:
        raise HTTPException(status_code=503, detail="Chưa cấu hình OPENWEATHER_API_KEY trên máy chủ")
    weather_url = (
        f"https://api.openweathermap.org/data/2.5/weather"
        f"?lat={lat}&lon={lon}&appid={api_key}&units=metric&lang=vi"
    )
    pollution_url = (
        f"https://api.openweathermap.org/data/2.5/air_pollution"
        f"?lat={lat}&lon={lon}&appid={api_key}"
    )

    async with httpx.AsyncClient(timeout=5.0) as client:
        weather_res, pollution_res = await asyncio.gather(
            client.get(weather_url),
            client.get(pollution_url),
            return_exceptions=True,
        )

    if isinstance(weather_res, Exception) or weather_res.status_code != 200:
        raise HTTPException(status_code=502, detail="Không thể tải dữ liệu thời tiết từ OpenWeatherMap")

    weather_data = weather_res.json()
    weather_info = (weather_data.get("weather") or [{}])[0]
    main_info = weather_data.get("main", {})

    aqi = 32
    if not isinstance(pollution_res, Exception) and pollution_res.status_code == 200:
        p_list = pollution_res.json().get("list") or [{}]
        pm25 = p_list[0].get("components", {}).get("pm2_5", 7.8)
        aqi = calculate_us_aqi_pm25(float(pm25))

    return WeatherResponse(
        city=weather_data.get("name") or "Hà Nội",
        temperature=round(float(main_info.get("temp", 24.0)), 1),
        humidity=int(main_info.get("humidity", 68)),
        condition=weather_info.get("description") or "Mây thưa",
        weather_main=weather_info.get("main") or "Clouds",
        icon=weather_info.get("icon") or "02d",
        aqi=aqi,
    )
