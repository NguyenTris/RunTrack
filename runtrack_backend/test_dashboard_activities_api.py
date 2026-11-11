from datetime import datetime, timedelta, timezone

from models import ActivityDB, UserDB
from services import get_current_week_bounds


def _auth_headers(client, email="dash@runtrack.vn", full_name="Dash Runner"):
    client.post(
        "/auth/register",
        json={"email": email, "password": "Password123", "full_name": full_name},
    )
    token = client.post(
        "/auth/login",
        json={"email": email, "password": "Password123"},
    ).json()["access_token"]
    return {"Authorization": f"Bearer {token}"}


def test_create_activity_recent_and_weekly_stats(client, db_session):
    headers = _auth_headers(client)
    client.put("/user/profile", headers=headers, json={"weekly_goal_km": 20.0})

    # Thêm 1 buổi chạy từ tuần trước (không được cộng vào home-stats tuần này)
    user = db_session.query(UserDB).filter_by(email="dash@runtrack.vn").first()
    week_start_vn, week_start_utc, _ = get_current_week_bounds()
    old_run = ActivityDB(
        user_id=user.id,
        title="Old Week Run",
        distance_km=15.0,
        duration_seconds=4500,
        start_time=week_start_utc - timedelta(days=2),
    )
    db_session.add(old_run)
    db_session.commit()

    # Tạo 2 buổi chạy trong tuần hiện tại qua POST /activities
    r1 = client.post(
        "/activities",
        headers=headers,
        json={
            "title": "Chạy sáng Thứ 2",
            "distance_km": 5.5,
            "duration_seconds": 1800,
            "start_time": (week_start_vn + timedelta(hours=6)).isoformat(),
        },
    )
    assert r1.status_code == 201
    assert r1.json()["title"] == "Chạy sáng Thứ 2"
    assert r1.json()["duration_minutes"] == 30

    r2 = client.post(
        "/activities",
        headers=headers,
        json={
            "title": "Chạy chiều cùng ngày",
            "distance_km": 4.5,
            "duration_seconds": 1500,
            "start_time": (week_start_vn + timedelta(hours=17)).isoformat(),
        },
    )
    assert r2.status_code == 201

    # Home stats chỉ cộng 5.5 + 4.5 = 10.0 km (bỏ qua 15.0 km tuần trước)
    stats = client.get("/dashboard/home-stats", headers=headers)
    assert stats.status_code == 200
    assert stats.json()["total_distance_km"] == 10.0
    assert stats.json()["weekly_goal_km"] == 20.0
    assert stats.json()["completion_percentage"] == 50

    # Recent activities trả đủ cả 3 (mới nhất trước), tôn trọng limit
    recent = client.get("/dashboard/recent-activities?limit=2", headers=headers)
    assert recent.status_code == 200
    assert len(recent.json()) == 2
    assert isinstance(recent.json()[0]["id"], str)

    # Weekly activities trả đủ 7 ngày T2..CN với monday = 10.0 km
    weekly = client.get("/activities/weekly", headers=headers)
    assert weekly.status_code == 200
    wdata = weekly.json()
    assert wdata["total_distance_km"] == 10.0
    assert len(wdata["days"]) == 7
    assert wdata["days"][0]["day"] == "monday"
    assert wdata["days"][0]["distance_km"] == 10.0
    assert wdata["days"][0]["completed"] is True
    assert wdata["days"][6]["completed"] is False


def test_create_activity_validation_error(client):
    headers = _auth_headers(client, email="val@runtrack.vn")
    bad = client.post(
        "/activities",
        headers=headers,
        json={"distance_km": -2.0, "duration_seconds": 0},
    )
    assert bad.status_code == 422
