from datetime import datetime, timezone
from typing import Literal, Optional

from fastapi import APIRouter, Depends, Query
from sqlalchemy import func
from sqlalchemy.orm import Session

from auth import get_current_user
from database import get_db
from models import ActivityDB, ClubMemberDB, UserDB, UserProfileDB
from schemas import (
    AchievementItem,
    LeaderboardEntry,
    LeaderboardResponse,
    MonthDayHeat,
    ProgressMonthResponse,
    ProgressRecordItem,
    ProgressYearResponse,
    QuarterStatItem,
)
from services import VN_TZ, ensure_utc

router = APIRouter(tags=["Progress & Leaderboard"])


def _format_pace(pace_seconds: int) -> str:
    if pace_seconds <= 0:
        return "--"
    return f"{pace_seconds // 60}'{pace_seconds % 60:02d}\""


def _format_duration(seconds: int) -> str:
    mins, secs = divmod(max(0, int(seconds)), 60)
    hours, mins = divmod(mins, 60)
    if hours > 0:
        return f"{hours}:{mins:02d}:{secs:02d}"
    return f"{mins:02d}:{secs:02d}"


def _calc_heat_level(distance_km: float) -> int:
    if distance_km <= 0:
        return 0
    if distance_km < 5.0:
        return 1
    if distance_km <= 10.0:
        return 2
    return 3


def _activity_pace_seconds(act: ActivityDB) -> int:
    if act.avg_pace_seconds and act.avg_pace_seconds > 0:
        return int(act.avg_pace_seconds)
    if act.distance_km and act.distance_km > 0 and act.duration_seconds:
        return int(act.duration_seconds / act.distance_km)
    return 0


def _activity_date_label(act: ActivityDB) -> str:
    if not act.start_time:
        return ""
    return ensure_utc(act.start_time).astimezone(VN_TZ).strftime("%d/%m/%Y")


def _find_distance_record(
    activities: list[ActivityDB],
    min_km: float,
    label: str,
) -> Optional[ProgressRecordItem]:
    eligible = [a for a in activities if float(a.distance_km or 0) >= min_km and (a.duration_seconds or 0) > 0]
    if not eligible:
        return None
    best = min(eligible, key=lambda a: (a.duration_seconds / float(a.distance_km)) * min_km)
    est_seconds = int((best.duration_seconds / float(best.distance_km)) * min_km)
    return ProgressRecordItem(label=label, value=_format_duration(est_seconds), date=_activity_date_label(best))


def _build_records(activities: list[ActivityDB]) -> list[ProgressRecordItem]:
    if not activities:
        return []
    records: list[ProgressRecordItem] = []
    longest = max(activities, key=lambda a: float(a.distance_km or 0))
    records.append(
        ProgressRecordItem(
            label="Buổi chạy dài nhất",
            value=f"{float(longest.distance_km):.2f} km",
            date=_activity_date_label(longest),
        )
    )
    paced = [(a, _activity_pace_seconds(a)) for a in activities]
    paced = [(a, p) for a, p in paced if p > 0]
    if paced:
        fastest_act, fastest_pace = min(paced, key=lambda item: item[1])
        records.append(
            ProgressRecordItem(
                label="Pace nhanh nhất",
                value=f"{_format_pace(fastest_pace)}/km",
                date=_activity_date_label(fastest_act),
            )
        )
    rec_5k = _find_distance_record(activities, 5.0, "5K nhanh nhất")
    if rec_5k:
        records.append(rec_5k)
    rec_10k = _find_distance_record(activities, 10.0, "10K nhanh nhất")
    if rec_10k:
        records.append(rec_10k)
    return records


def _build_year_achievements(activities: list[ActivityDB], total_km: float) -> list[AchievementItem]:
    has_5k_sub_30 = any(
        float(a.distance_km or 0) >= 5.0
        and (a.duration_seconds or 0) > 0
        and (a.duration_seconds / float(a.distance_km)) * 5.0 < 1800
        for a in activities
    )
    has_10k = any(float(a.distance_km or 0) >= 10.0 for a in activities)
    return [
        AchievementItem(title="5K < 30'", subtitle="Hoàn thành 5 km dưới 30 phút", unlocked=has_5k_sub_30),
        AchievementItem(title="10K RUNNER", subtitle="Hoàn thành buổi chạy từ 10 km", unlocked=has_10k),
        AchievementItem(title="10 BUỔI CHẠY", subtitle="Tích lũy từ 10 buổi chạy trong năm", unlocked=len(activities) >= 10),
        AchievementItem(title="100 KM/NĂM", subtitle="Chinh phục mốc 100 km trong năm", unlocked=total_km >= 100.0),
    ]


@router.get("/progress/month", response_model=ProgressMonthResponse)
async def get_progress_month(
    year: Optional[int] = Query(default=None, ge=2000, le=2100),
    month: Optional[int] = Query(default=None, ge=1, le=12),
    current_user: UserDB = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    now_vn = datetime.now(VN_TZ)
    target_year = year or now_vn.year
    target_month = month or now_vn.month

    start_vn = datetime(target_year, target_month, 1, tzinfo=VN_TZ)
    next_year, next_month = (target_year + 1, 1) if target_month == 12 else (target_year, target_month + 1)
    end_vn = datetime(next_year, next_month, 1, tzinfo=VN_TZ)
    first_weekday = start_vn.weekday()
    days_in_month = (end_vn - start_vn).days

    activities = (
        db.query(ActivityDB)
        .filter(
            ActivityDB.user_id == current_user.id,
            ActivityDB.start_time >= start_vn.astimezone(timezone.utc),
            ActivityDB.start_time < end_vn.astimezone(timezone.utc),
        )
        .all()
    )

    per_day = {d: {"distance": 0.0, "count": 0} for d in range(1, days_in_month + 1)}
    total_duration_sec = 0
    for act in activities:
        total_duration_sec += int(act.duration_seconds or 0)
        if act.start_time:
            day_num = ensure_utc(act.start_time).astimezone(VN_TZ).day
            per_day[day_num]["distance"] += float(act.distance_km or 0)
            per_day[day_num]["count"] += 1

    days = [
        MonthDayHeat(
            day=d,
            date=f"{target_year:04d}-{target_month:02d}-{d:02d}",
            distance_km=round(per_day[d]["distance"], 2),
            activities_count=per_day[d]["count"],
            heat_level=_calc_heat_level(per_day[d]["distance"]),
        )
        for d in range(1, days_in_month + 1)
    ]
    total_km = round(sum(float(a.distance_km or 0) for a in activities), 2)
    return ProgressMonthResponse(
        year=target_year,
        month=target_month,
        first_weekday=first_weekday,
        days_in_month=days_in_month,
        total_distance_km=total_km,
        total_activities=len(activities),
        total_duration_minutes=int(total_duration_sec // 60),
        days=days,
        records=_build_records(activities),
    )


@router.get("/progress/year", response_model=ProgressYearResponse)
async def get_progress_year(
    year: Optional[int] = Query(default=None, ge=2000, le=2100),
    current_user: UserDB = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    target_year = year or datetime.now(VN_TZ).year
    start_vn = datetime(target_year, 1, 1, tzinfo=VN_TZ)
    end_vn = datetime(target_year + 1, 1, 1, tzinfo=VN_TZ)

    activities = (
        db.query(ActivityDB)
        .filter(
            ActivityDB.user_id == current_user.id,
            ActivityDB.start_time >= start_vn.astimezone(timezone.utc),
            ActivityDB.start_time < end_vn.astimezone(timezone.utc),
        )
        .all()
    )
    profile = db.query(UserProfileDB).filter(UserProfileDB.user_id == current_user.id).first()
    weekly_goal = float(profile.weekly_goal_km) if profile and profile.weekly_goal_km else 40.0
    quarter_target = round(weekly_goal * 13.0, 1)

    q_totals = [0.0, 0.0, 0.0, 0.0]
    for act in activities:
        if act.start_time:
            q_idx = (ensure_utc(act.start_time).astimezone(VN_TZ).month - 1) // 3
            q_totals[q_idx] += float(act.distance_km or 0)

    quarters = [
        QuarterStatItem(
            quarter=f"Q{idx + 1}",
            distance_km=round(dist, 2),
            target_km=quarter_target,
            completion_percentage=min(100, int((dist / quarter_target) * 100)) if quarter_target > 0 else 0,
        )
        for idx, dist in enumerate(q_totals)
    ]
    total_km = round(sum(float(a.distance_km or 0) for a in activities), 2)
    return ProgressYearResponse(
        year=target_year,
        total_distance_km=total_km,
        total_activities=len(activities),
        quarters=quarters,
        records=_build_records(activities),
        achievements=_build_year_achievements(activities, total_km),
    )


def _resolve_friend_scope_user_ids(current_user_id: int, db: Session) -> set[int]:
    my_club_ids = [
        row[0] for row in db.query(ClubMemberDB.club_id).filter(ClubMemberDB.user_id == current_user_id).all()
    ]
    user_ids = {current_user_id}
    if my_club_ids:
        peer_rows = db.query(ClubMemberDB.user_id).filter(ClubMemberDB.club_id.in_(my_club_ids)).all()
        user_ids.update(row[0] for row in peer_rows)
    return user_ids


@router.get("/leaderboard", response_model=LeaderboardResponse)
async def get_leaderboard(
    scope: Literal["global", "friends"] = Query(default="global"),
    limit: int = Query(default=20, ge=1, le=100),
    current_user: UserDB = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    total_km_col = func.coalesce(func.sum(ActivityDB.distance_km), 0.0).label("total_km")
    total_dur_col = func.coalesce(func.sum(ActivityDB.duration_seconds), 0).label("total_dur")
    total_cnt_col = func.count(ActivityDB.id).label("total_cnt")

    query = (
        db.query(UserDB, UserProfileDB, total_km_col, total_dur_col, total_cnt_col)
        .outerjoin(UserProfileDB, UserDB.id == UserProfileDB.user_id)
        .outerjoin(ActivityDB, UserDB.id == ActivityDB.user_id)
    )
    if scope == "friends":
        allowed_ids = _resolve_friend_scope_user_ids(current_user.id, db)
        query = query.filter(UserDB.id.in_(allowed_ids))

    rows = (
        query.group_by(UserDB.id, UserProfileDB.id)
        .order_by(total_km_col.desc(), total_cnt_col.desc(), UserDB.id.asc())
        .all()
    )

    entries: list[LeaderboardEntry] = []
    current_user_rank: Optional[int] = None
    for idx, (user, profile, total_km, total_dur, total_cnt) in enumerate(rows, start=1):
        km_val = round(float(total_km or 0.0), 2)
        pace_sec = int(float(total_dur) / km_val) if km_val > 0 and total_dur else 0
        is_me = user.id == current_user.id
        if is_me:
            current_user_rank = idx
        if idx <= limit:
            full_name = profile.full_name if profile and profile.full_name else user.email.split("@")[0]
            entries.append(
                LeaderboardEntry(
                    rank=idx,
                    user_id=user.id,
                    full_name=full_name,
                    total_distance_km=km_val,
                    total_activities=int(total_cnt or 0),
                    avg_pace_display=_format_pace(pace_sec),
                    is_current_user=is_me,
                )
            )

    return LeaderboardResponse(
        scope=scope,
        total_runners=len(rows),
        current_user_rank=current_user_rank,
        entries=entries,
    )
