from datetime import datetime, timedelta, timezone

from auth import get_password_hash
from models import ActivityDB, ClubDB, ClubEventDB, PasswordResetTokenDB, UserDB, UserProfileDB
from seed import seed_database


def _register_and_login(client, email: str, secret: str, full_name: str) -> dict[str, str]:
    reg = client.post(
        "/auth/register",
        json={"email": email, "password": secret, "full_name": full_name},
    )
    assert reg.status_code == 200
    login = client.post("/auth/login", json={"email": email, "password": secret})
    assert login.status_code == 200
    return {"Authorization": f"Bearer {login.json()['access_token']}"}


def _login_headers(client, email: str, secret: str) -> dict[str, str]:
    resp = client.post("/auth/login", json={"email": email, "password": secret})
    assert resp.status_code == 200
    return {"Authorization": f"Bearer {resp.json()['access_token']}"}


def _create_user_with_goal(
    db_session, email: str, secret: str, full_name: str, weekly_goal_km: float = 40.0
) -> UserDB:
    user = UserDB(email=email, password_hash=get_password_hash(secret), role="user")
    db_session.add(user)
    db_session.commit()
    db_session.refresh(user)
    db_session.add(
        UserProfileDB(user_id=user.id, full_name=full_name, weekly_goal_km=weekly_goal_km)
    )
    db_session.commit()
    return user


def _seed_activity(
    db_session,
    user_id: int,
    title: str,
    distance_km: float,
    duration_sec: int,
    start_utc: datetime,
) -> ActivityDB:
    act = ActivityDB(
        user_id=user_id,
        title=title,
        distance_km=distance_km,
        duration_seconds=duration_sec,
        avg_pace_seconds=int(duration_sec / distance_km),
        calories_burned=int(distance_km * 65),
        start_time=start_utc,
    )
    db_session.add(act)
    db_session.commit()
    return act


def test_forgot_and_reset_password_full_flow(client, db_session):
    email = "reset.flow@runtrack.vn"
    _register_and_login(client, email, "OldPass@123", "Reset Runner")

    unknown = client.post("/auth/forgot-password", json={"email": "nobody@runtrack.vn"})
    assert unknown.status_code == 200
    assert unknown.json()["demo_otp"] is None

    forgot = client.post("/auth/forgot-password", json={"email": email})
    assert forgot.status_code == 200
    otp = forgot.json()["demo_otp"]
    assert isinstance(otp, str) and len(otp) == 6 and otp.isdigit()

    wrong_otp = "000000" if otp != "000000" else "999999"
    bad_resp = client.post(
        "/auth/reset-password",
        json={"email": email, "otp": wrong_otp, "new_password": "NewPass@456"},
    )
    assert bad_resp.status_code == 400

    ok_resp = client.post(
        "/auth/reset-password",
        json={"email": email, "otp": otp, "new_password": "NewPass@456"},
    )
    assert ok_resp.status_code == 200

    reuse_resp = client.post(
        "/auth/reset-password",
        json={"email": email, "otp": otp, "new_password": "Another@789"},
    )
    assert reuse_resp.status_code == 400

    assert client.post("/auth/login", json={"email": email, "password": "OldPass@123"}).status_code == 401
    assert client.post("/auth/login", json={"email": email, "password": "NewPass@456"}).status_code == 200


def test_reset_password_expired_and_validation_edge_cases(client, db_session):
    user = _create_user_with_goal(db_session, "expired@runtrack.vn", "Pass@123", "Expired User")
    expired_at = datetime.now(timezone.utc).replace(tzinfo=None) - timedelta(minutes=5)
    db_session.add(
        PasswordResetTokenDB(user_id=user.id, otp_code="123456", expires_at=expired_at, used=0)
    )
    db_session.commit()

    exp_resp = client.post(
        "/auth/reset-password",
        json={"email": "expired@runtrack.vn", "otp": "123456", "new_password": "New@123"},
    )
    assert exp_resp.status_code == 400

    missing_user = client.post(
        "/auth/reset-password",
        json={"email": "ghost@runtrack.vn", "otp": "123456", "new_password": "New@123"},
    )
    assert missing_user.status_code == 400

    short_otp = client.post(
        "/auth/reset-password",
        json={"email": "expired@runtrack.vn", "otp": "12", "new_password": "New@123"},
    )
    assert short_otp.status_code == 422


def test_create_activity_with_route_points_and_bounds(client):
    headers = _register_and_login(client, "route@runtrack.vn", "Pass@123", "Route Runner")
    coords = [
        {"lat": 10.7769, "lng": 106.7009},
        {"lat": 10.7785, "lng": 106.7031},
        {"lat": 10.7802, "lng": 106.7055},
    ]
    created = client.post(
        "/activities",
        headers=headers,
        json={
            "title": "GPS Route Run",
            "distance_km": 4.2,
            "duration_seconds": 1320,
            "route_points": coords,
        },
    )
    assert created.status_code == 201
    assert created.json()["route_points"] == coords

    recent = client.get("/dashboard/recent-activities?limit=5", headers=headers)
    assert recent.status_code == 200
    assert recent.json()[0]["route_points"] == coords

    bad_lat = client.post(
        "/activities",
        headers=headers,
        json={
            "title": "Bad Lat",
            "distance_km": 2.0,
            "duration_seconds": 600,
            "route_points": [{"lat": 95.0, "lng": 106.7}],
        },
    )
    assert bad_lat.status_code == 422

    bad_lng = client.post(
        "/activities",
        headers=headers,
        json={
            "title": "Bad Lng",
            "distance_km": 2.0,
            "duration_seconds": 600,
            "route_points": [{"lat": 10.7, "lng": -190.0}],
        },
    )
    assert bad_lng.status_code == 422


def test_progress_month_heatmap_records_and_edge_cases(client, db_session):
    assert client.get("/progress/month").status_code == 401
    user = _create_user_with_goal(db_session, "month@runtrack.vn", "Pass@123", "Month Runner")
    headers = _login_headers(client, "month@runtrack.vn", "Pass@123")

    empty = client.get("/progress/month?year=2025&month=2", headers=headers)
    assert empty.status_code == 200
    empty_data = empty.json()
    assert empty_data["first_weekday"] == datetime(2025, 2, 1).weekday()
    assert empty_data["days_in_month"] == 28
    assert len(empty_data["days"]) == 28
    assert empty_data["total_distance_km"] == 0.0
    assert empty_data["records"] == []

    _seed_activity(db_session, user.id, "Short", 3.0, 1080, datetime(2026, 4, 2, 1, 0, tzinfo=timezone.utc))
    _seed_activity(db_session, user.id, "Mid 5K", 6.0, 1800, datetime(2026, 4, 5, 1, 0, tzinfo=timezone.utc))
    _seed_activity(db_session, user.id, "Long 10K", 12.0, 4200, datetime(2026, 4, 10, 1, 0, tzinfo=timezone.utc))

    resp = client.get("/progress/month?year=2026&month=4", headers=headers)
    assert resp.status_code == 200
    data = resp.json()
    assert data["first_weekday"] == datetime(2026, 4, 1).weekday()
    assert data["days_in_month"] == 30
    assert data["total_distance_km"] == 21.0
    assert data["total_activities"] == 3
    assert data["total_duration_minutes"] == 118
    assert data["days"][1]["heat_level"] == 1
    assert data["days"][4]["heat_level"] == 2
    assert data["days"][9]["heat_level"] == 3
    labels = {r["label"] for r in data["records"]}
    assert {"Buổi chạy dài nhất", "Pace nhanh nhất", "5K nhanh nhất", "10K nhanh nhất"} <= labels
    assert client.get("/progress/month?year=2026&month=13", headers=headers).status_code == 422


def test_progress_year_quarters_records_and_achievements(client, db_session):
    assert client.get("/progress/year").status_code == 401
    user = _create_user_with_goal(
        db_session, "year@runtrack.vn", "Pass@123", "Year Runner", weekly_goal_km=10.0
    )
    headers = _login_headers(client, "year@runtrack.vn", "Pass@123")

    locked_resp = client.get("/progress/year?year=2025", headers=headers)
    assert locked_resp.status_code == 200
    locked_data = locked_resp.json()
    assert [q["quarter"] for q in locked_data["quarters"]] == ["Q1", "Q2", "Q3", "Q4"]
    assert all(not ach["unlocked"] for ach in locked_data["achievements"])

    for idx in range(10):
        month_num = (idx % 4) * 3 + 1
        dist = 11.0 if idx == 0 else 10.0
        dur = 3000 if idx == 0 else 2800
        _seed_activity(
            db_session,
            user.id,
            f"Run {idx + 1}",
            dist,
            dur,
            datetime(2026, month_num, 5 + (idx % 10), 1, 0, tzinfo=timezone.utc),
        )

    unlocked_resp = client.get("/progress/year?year=2026", headers=headers)
    assert unlocked_resp.status_code == 200
    data = unlocked_resp.json()
    assert data["total_activities"] == 10
    assert data["total_distance_km"] == 101.0
    assert len(data["quarters"]) == 4
    assert all(q["target_km"] == 130.0 for q in data["quarters"])
    assert sum(q["distance_km"] for q in data["quarters"]) == 101.0
    assert len(data["records"]) >= 4
    assert all(ach["unlocked"] for ach in data["achievements"])


def test_leaderboard_global_and_friends_scope(client, db_session):
    assert client.get("/leaderboard").status_code == 401
    seed_database(db_session)
    _create_user_with_goal(db_session, "solo@runtrack.app", "Pass@123", "Solo Runner")
    headers = _login_headers(client, "runner1@runtrack.app", "Runner@123")

    global_resp = client.get("/leaderboard?scope=global&limit=10", headers=headers)
    assert global_resp.status_code == 200
    g_data = global_resp.json()
    assert g_data["scope"] == "global"
    assert g_data["total_runners"] == 5
    distances = [e["total_distance_km"] for e in g_data["entries"]]
    assert distances == sorted(distances, reverse=True)
    me_global = [e for e in g_data["entries"] if e["is_current_user"]]
    assert len(me_global) == 1
    assert g_data["current_user_rank"] == me_global[0]["rank"]

    friends_resp = client.get("/leaderboard?scope=friends&limit=10", headers=headers)
    assert friends_resp.status_code == 200
    f_data = friends_resp.json()
    assert f_data["scope"] == "friends"
    assert f_data["total_runners"] == 3
    f_names = {e["full_name"] for e in f_data["entries"]}
    assert "Solo Runner" not in f_names
    assert "RunTrack Admin" not in f_names
    f_distances = [e["total_distance_km"] for e in f_data["entries"]]
    assert f_distances == sorted(f_distances, reverse=True)
    assert client.get("/leaderboard?scope=invalid", headers=headers).status_code == 422


def test_club_detail_and_toggle_event_register(client, db_session):
    assert client.get("/clubs/1/detail").status_code == 401
    seed_database(db_session)
    headers = _login_headers(client, "runner1@runtrack.app", "Runner@123")

    club = db_session.query(ClubDB).filter(ClubDB.name == "RunTrack Ho Chi Minh").first()
    assert club is not None
    detail = client.get(f"/clubs/{club.id}/detail", headers=headers)
    assert detail.status_code == 200
    d_data = detail.json()
    assert d_data["club_id"] == club.id
    assert len(d_data["members"]) == d_data["member_count"]
    assert d_data["members"] == sorted(d_data["members"], key=lambda m: (-m["weekly_km"], m["user_id"]))
    assert len(d_data["events"]) >= 1

    event_id = d_data["events"][0]["event_id"]
    initial_count = d_data["events"][0]["registered_count"]
    assert d_data["events"][0]["is_registered"] is False

    reg = client.post(f"/clubs/events/{event_id}/toggle-register", headers=headers)
    assert reg.status_code == 200
    assert reg.json()["is_registered"] is True
    assert reg.json()["registered_count"] == initial_count + 1

    unreg = client.post(f"/clubs/events/{event_id}/toggle-register", headers=headers)
    assert unreg.status_code == 200
    assert unreg.json()["is_registered"] is False
    assert unreg.json()["registered_count"] == initial_count

    assert client.get("/clubs/999999/detail", headers=headers).status_code == 404
    assert client.post("/clubs/events/999999/toggle-register", headers=headers).status_code == 404
    assert db_session.query(ClubEventDB).count() >= 3


def test_gps_art_routes_public_and_valid_coordinates(client):
    first = client.get("/gps-art/routes")
    second = client.get("/gps-art/routes")
    assert first.status_code == 200
    assert second.status_code == 200
    routes = first.json()
    assert len(routes) == 4
    assert len(second.json()) == 4

    for route in routes:
        assert route["name"]
        assert route["distance_km"] > 0
        assert route["city"]
        assert route["icon_key"]
        assert len(route["points"]) >= 3
        for pt in route["points"]:
            assert -90.0 <= pt["lat"] <= 90.0
            assert -180.0 <= pt["lng"] <= 180.0
