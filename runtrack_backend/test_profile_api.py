from datetime import datetime, timedelta, timezone

from models import ActivityDB, UserDB
from services import VN_TZ, compute_recovery_metrics, get_current_week_bounds


def _register_and_login(client, email="profile@runtrack.vn", full_name="Minh Trí"):
    client.post(
        "/auth/register",
        json={"email": email, "password": "Password123", "full_name": full_name},
    )
    token = client.post(
        "/auth/login",
        json={"email": email, "password": "Password123"},
    ).json()["access_token"]
    return {"Authorization": f"Bearer {token}"}


def test_profile_partial_update_and_consistent_progress(client):
    headers = _register_and_login(client)

    initial = client.get("/user/profile", headers=headers)
    assert initial.status_code == 200
    body = initial.json()
    assert body["full_name"] == "Minh Trí"
    assert body["recovery_completed"] is True
    assert body["recovery_percentage"] == 100
    assert body["recovery_status_color"] == "green"
    assert body["progress_bar_percentage"] == 0

    # Cập nhật chỉ weekly_goal_km và height -> phải giữ nguyên full_name cũ
    updated = client.put(
        "/user/profile",
        headers=headers,
        json={"height": 173.5, "weekly_goal_km": 35.0},
    )
    assert updated.status_code == 200
    upd_body = updated.json()
    assert upd_body["full_name"] == "Minh Trí"
    assert upd_body["height"] == 173.5
    assert upd_body["weekly_goal_km"] == 35.0
    assert upd_body["progress_bar_percentage"] == body["progress_bar_percentage"]


def test_profile_recovery_and_weekly_progress_from_activities(client, db_session):
    headers = _register_and_login(client, email="active@runtrack.vn", full_name="Active Runner")
    user = db_session.query(UserDB).filter_by(email="active@runtrack.vn").first()

    week_start_vn, _, _ = get_current_week_bounds()
    # Tạo 2 buổi chạy trong tuần (Thứ 2 và Thứ 4), buổi gần nhất cách hiện tại 10 giờ
    act1 = ActivityDB(
        user_id=user.id,
        title="Morning Run",
        distance_km=5.0,
        duration_seconds=1800,
        start_time=week_start_vn.astimezone(timezone.utc) + timedelta(hours=7),
    )
    recent_time = datetime.now(timezone.utc) - timedelta(hours=10)
    act2 = ActivityDB(
        user_id=user.id,
        title="Recent Run",
        distance_km=6.0,
        duration_seconds=2100,
        start_time=recent_time,
    )
    db_session.add_all([act1, act2])
    db_session.commit()

    res = client.get("/user/profile", headers=headers)
    assert res.status_code == 200
    data = res.json()
    assert data["last_workout_time"] is not None
    assert data["recovery_completed"] is False
    assert 13.5 <= data["recovery_hours_remaining"] <= 14.5
    assert data["recovery_percentage"] == 41
    assert data["recovery_status_color"] == "red"
    assert data["recovery_status_label"] == "CẦN NGHỈ"
    assert data["weekly_progress"]["monday"] is True
    # progress_bar_percentage khớp đúng số ngày completed / 7 * 100
    days_true = sum(1 for v in data["weekly_progress"].values() if v)
    assert data["progress_bar_percentage"] == int((days_true / 7) * 100)


def test_compute_recovery_metrics_boundaries():
    now = datetime(2026, 11, 11, 12, 0, tzinfo=timezone.utc)
    res_yellow = compute_recovery_metrics(now - timedelta(hours=17), now_utc=now)
    assert res_yellow["recovery_status_color"] == "yellow"
    assert res_yellow["recovery_percentage"] == 70
    assert res_yellow["recovery_completed"] is False

    res_done = compute_recovery_metrics(now - timedelta(hours=25), now_utc=now)
    assert res_done["recovery_completed"] is True
    assert res_done["recovery_percentage"] == 100
    assert res_done["recovery_hours_remaining"] == 0.0
