import json
from pathlib import Path
import re

from auth import get_password_hash
from main import app
from models import UserDB, UserProfileDB
from seed import seed_database


def _login_headers(client, email: str, password: str) -> dict[str, str]:
    resp = client.post("/auth/login", json={"email": email, "password": password})
    assert resp.status_code == 200
    return {"Authorization": f"Bearer {resp.json()['access_token']}"}


def _create_account(db_session, email: str, password: str, role: str, full_name: str) -> UserDB:
    user = UserDB(email=email, password_hash=get_password_hash(password), role=role)
    db_session.add(user)
    db_session.commit()
    db_session.refresh(user)
    db_session.add(UserProfileDB(user_id=user.id, full_name=full_name, weekly_goal_km=40.0))
    db_session.commit()
    return user


def _collect_postman_routes(items: list[dict], out: set[tuple[str, str]]) -> None:
    for item in items:
        if "item" in item:
            _collect_postman_routes(item["item"], out)
            continue
        req = item["request"]
        method = req["method"].upper()
        segments = [re.sub(r"^\{\{.+\}\}$", "{param}", seg) for seg in req["url"]["path"]]
        out.add((method, "/" + "/".join(segments)))


def test_admin_rbac_401_403_and_200(client, db_session):
    _create_account(db_session, "admin@test.app", "Admin@123", "admin", "Admin User")
    _create_account(db_session, "member@test.app", "User@123", "user", "Normal User")

    for path in ("/admin/stats", "/admin/users", "/admin/activities", "/admin/clubs"):
        assert client.get(path).status_code == 401

    user_headers = _login_headers(client, "member@test.app", "User@123")
    assert client.get("/admin/stats", headers=user_headers).status_code == 403
    assert client.get("/admin/users", headers=user_headers).status_code == 403
    assert (
        client.post(
            "/admin/clubs",
            headers=user_headers,
            json={"name": "Blocked Club", "description": "No access"},
        ).status_code
        == 403
    )

    admin_headers = _login_headers(client, "admin@test.app", "Admin@123")
    assert client.get("/admin/stats", headers=admin_headers).status_code == 200
    created = client.post(
        "/admin/clubs",
        headers=admin_headers,
        json={"name": "Admin Verified Club", "description": "Created by admin"},
    )
    assert created.status_code == 201


def test_admin_stats_aggregates_7_days_and_top_runners(client, db_session):
    summary = seed_database(db_session)
    admin_headers = _login_headers(client, "admin@runtrack.app", "Admin@123")

    resp = client.get("/admin/stats", headers=admin_headers)
    assert resp.status_code == 200
    data = resp.json()
    assert data["total_users"] == summary["users"]
    assert data["total_activities"] == summary["activities"]
    assert data["total_clubs"] == summary["clubs"]
    assert data["total_distance_km"] > 0
    assert data["total_calories"] > 0
    assert len(data["daily_stats"]) == 7
    assert sum(day["activities_count"] for day in data["daily_stats"]) == summary["activities"]

    top_runners = data["top_runners"]
    assert len(top_runners) >= 3
    distances = [runner["total_distance_km"] for runner in top_runners]
    assert distances == sorted(distances, reverse=True)


def test_admin_users_crud_and_self_delete_guard(client, db_session):
    admin = _create_account(db_session, "root@test.app", "Admin@123", "admin", "Root Admin")
    headers = _login_headers(client, "root@test.app", "Admin@123")

    create_payload = {
        "email": "new.runner@test.app",
        "password": "Password123",
        "full_name": "Nguyễn Văn A",
        "role": "user",
        "weekly_goal_km": 35.0,
    }
    created = client.post("/admin/users", headers=headers, json=create_payload)
    assert created.status_code == 201
    user_id = created.json()["id"]

    dup = client.post("/admin/users", headers=headers, json=create_payload)
    assert dup.status_code == 400

    listed = client.get("/admin/users?q=new.runner&page=1&limit=10", headers=headers)
    assert listed.status_code == 200
    assert len(listed.json()) == 1
    assert listed.json()[0]["id"] == user_id

    updated = client.put(
        f"/admin/users/{user_id}",
        headers=headers,
        json={"role": "admin", "full_name": "Nguyễn Văn B", "weekly_goal_km": 60.0},
    )
    assert updated.status_code == 200
    assert updated.json()["role"] == "admin"
    assert updated.json()["full_name"] == "Nguyễn Văn B"
    assert updated.json()["weekly_goal_km"] == 60.0

    assert client.delete(f"/admin/users/{admin.id}", headers=headers).status_code == 400
    assert client.delete("/admin/users/999999", headers=headers).status_code == 404
    assert client.delete(f"/admin/users/{user_id}", headers=headers).status_code == 200
    assert client.delete(f"/admin/users/{user_id}", headers=headers).status_code == 404


def test_admin_activities_crud_filter_and_validation(client, db_session):
    _create_account(db_session, "root@test.app", "Admin@123", "admin", "Root Admin")
    runner = _create_account(db_session, "r1@test.app", "Pass@123", "user", "Runner One")
    other = _create_account(db_session, "r2@test.app", "Pass@123", "user", "Runner Two")
    headers = _login_headers(client, "root@test.app", "Admin@123")

    invalid = client.post(
        "/admin/activities",
        headers=headers,
        json={"user_id": runner.id, "title": "Bad Run", "distance_km": 0, "duration_seconds": 600},
    )
    assert invalid.status_code == 422

    r1 = client.post(
        "/admin/activities",
        headers=headers,
        json={"user_id": runner.id, "title": "Morning 5K", "distance_km": 5.0, "duration_seconds": 1500},
    )
    r2 = client.post(
        "/admin/activities",
        headers=headers,
        json={"user_id": other.id, "title": "Evening 10K", "distance_km": 10.0, "duration_seconds": 3000},
    )
    assert r1.status_code == 201
    assert r2.status_code == 201
    act_id = r1.json()["id"]

    filtered = client.get(f"/admin/activities?user_id={runner.id}", headers=headers)
    assert filtered.status_code == 200
    assert len(filtered.json()) == 1
    assert filtered.json()[0]["id"] == act_id

    updated = client.put(
        f"/admin/activities/{act_id}",
        headers=headers,
        json={"title": "Morning 6K", "distance_km": 6.0, "calories_burned": 410},
    )
    assert updated.status_code == 200
    assert updated.json()["title"] == "Morning 6K"
    assert updated.json()["distance_km"] == 6.0

    assert client.delete(f"/admin/activities/{act_id}", headers=headers).status_code == 200
    assert client.delete(f"/admin/activities/{act_id}", headers=headers).status_code == 404


def test_admin_clubs_crud_and_duplicate_guard(client, db_session):
    _create_account(db_session, "root@test.app", "Admin@123", "admin", "Root Admin")
    headers = _login_headers(client, "root@test.app", "Admin@123")

    created = client.post(
        "/admin/clubs",
        headers=headers,
        json={"name": "Saigon Pacers", "description": "CLB chạy bộ TP.HCM"},
    )
    assert created.status_code == 201
    club_id = created.json()["club_id"]

    dup = client.post(
        "/admin/clubs",
        headers=headers,
        json={"name": "Saigon Pacers", "description": "Trùng tên"},
    )
    assert dup.status_code == 400

    blank = client.post(
        "/admin/clubs",
        headers=headers,
        json={"name": "   ", "description": "Tên rỗng"},
    )
    assert blank.status_code == 422

    listed = client.get("/admin/clubs?q=Saigon", headers=headers)
    assert listed.status_code == 200
    assert len(listed.json()) == 1

    updated = client.put(
        f"/admin/clubs/{club_id}",
        headers=headers,
        json={"name": "Saigon Pacers Pro", "description": "Mô tả mới"},
    )
    assert updated.status_code == 200
    assert updated.json()["name"] == "Saigon Pacers Pro"

    assert client.delete(f"/admin/clubs/{club_id}", headers=headers).status_code == 200
    assert client.delete(f"/admin/clubs/{club_id}", headers=headers).status_code == 404


def test_search_clubs_and_toggle_join_flow(client, db_session):
    _create_account(db_session, "club.user@test.app", "Pass@123", "user", "Club Runner")
    headers = _login_headers(client, "club.user@test.app", "Pass@123")

    clubs_resp = client.get("/search/clubs?q=&page=1&limit=10", headers=headers)
    assert clubs_resp.status_code == 200
    clubs = clubs_resp.json()
    assert len(clubs) >= 1

    target = clubs[0]
    club_id = target["club_id"]
    initial_count = target["member_count"]
    assert target["is_joined"] is False

    joined = client.post(f"/clubs/{club_id}/toggle-join", headers=headers)
    assert joined.status_code == 200
    assert joined.json()["is_joined"] is True
    assert joined.json()["member_count"] == initial_count + 1

    left = client.post(f"/clubs/{club_id}/toggle-join", headers=headers)
    assert left.status_code == 200
    assert left.json()["is_joined"] is False
    assert left.json()["member_count"] == initial_count

    missing = client.post("/clubs/999999/toggle-join", headers=headers)
    assert missing.status_code == 404


def test_seed_database_idempotent_and_admin_login(client, db_session):
    first = seed_database(db_session)
    second = seed_database(db_session)
    assert first == second
    assert first["users"] == 4
    assert first["clubs"] >= 3
    assert first["activities"] == 7

    admin_headers = _login_headers(client, "admin@runtrack.app", "Admin@123")
    stats = client.get("/admin/stats", headers=admin_headers)
    assert stats.status_code == 200
    assert stats.json()["total_users"] == first["users"]


def test_cors_headers_for_frontend_origin(client):
    origin = "http://localhost:5173"
    preflight = client.options(
        "/admin/stats",
        headers={
            "Origin": origin,
            "Access-Control-Request-Method": "GET",
            "Access-Control-Request-Headers": "Authorization",
        },
    )
    assert preflight.status_code == 200
    assert preflight.headers.get("access-control-allow-origin") in ("*", origin)


def test_postman_collection_matches_openapi_routes():
    collection_path = Path(__file__).with_name("RunTrack_API.postman_collection.json")
    collection = json.loads(collection_path.read_text(encoding="utf-8"))

    postman_routes: set[tuple[str, str]] = set()
    _collect_postman_routes(collection["item"], postman_routes)

    openapi_routes: set[tuple[str, str]] = set()
    for path, ops in app.openapi()["paths"].items():
        normalized = re.sub(r"\{[^}]+\}", "{param}", path)
        for method in ops:
            if method.lower() in {"get", "post", "put", "patch", "delete"}:
                openapi_routes.add((method.upper(), normalized))

    assert postman_routes == openapi_routes
