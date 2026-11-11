from datetime import datetime, timedelta, timezone
from typing import Optional

from sqlalchemy.orm import Session

from models import ActivityDB, UserDB, UserProfileDB
from schemas import UserProfileResponse, WeeklyProgress

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

    _, week_start_utc, week_end_utc = get_current_week_bounds()
    weekly_activities = (
        db.query(ActivityDB)
        .filter(
            ActivityDB.user_id == user.id,
            ActivityDB.start_time >= week_start_utc,
            ActivityDB.start_time < week_end_utc,
        )
        .all()
    )

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
