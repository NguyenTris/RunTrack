def test_register_login_and_duplicate_email(client):
    reg = client.post(
        "/auth/register",
        json={"email": "runner1@runtrack.vn", "password": "Password123", "full_name": "Runner One"},
    )
    assert reg.status_code == 200
    assert reg.json()["user"] == "runner1@runtrack.vn"

    dup = client.post(
        "/auth/register",
        json={"email": "runner1@runtrack.vn", "password": "Password123", "full_name": "Runner One"},
    )
    assert dup.status_code == 400

    bad_login = client.post(
        "/auth/login",
        json={"email": "runner1@runtrack.vn", "password": "WrongPassword"},
    )
    assert bad_login.status_code == 401

    login = client.post(
        "/auth/login",
        json={"email": "runner1@runtrack.vn", "password": "Password123"},
    )
    assert login.status_code == 200
    assert login.json()["token_type"] == "bearer"
    assert login.json()["access_token"]


def test_email_validation_422(client):
    res = client.post(
        "/auth/register",
        json={"email": "not-an-email", "password": "Password123", "full_name": "Bad Email"},
    )
    assert res.status_code == 422


def test_change_password_and_logout_blacklist(client):
    client.post(
        "/auth/register",
        json={"email": "sec@runtrack.vn", "password": "OldPassword1", "full_name": "Sec Runner"},
    )
    token = client.post(
        "/auth/login",
        json={"email": "sec@runtrack.vn", "password": "OldPassword1"},
    ).json()["access_token"]
    headers = {"Authorization": f"Bearer {token}"}

    wrong_change = client.put(
        "/auth/change-password",
        headers=headers,
        json={"old_password": "WrongOldPassword", "new_password": "NewPassword2"},
    )
    assert wrong_change.status_code == 400

    ok_change = client.put(
        "/auth/change-password",
        headers=headers,
        json={"old_password": "OldPassword1", "new_password": "NewPassword2"},
    )
    assert ok_change.status_code == 200

    relogin = client.post(
        "/auth/login",
        json={"email": "sec@runtrack.vn", "password": "NewPassword2"},
    )
    assert relogin.status_code == 200
    new_token = relogin.json()["access_token"]
    new_headers = {"Authorization": f"Bearer {new_token}"}

    # Trước khi logout, token hợp lệ truy cập được /user/profile
    assert client.get("/user/profile", headers=new_headers).status_code == 200

    # Logout đưa token vào blacklist (gọi lặp lại vẫn idempotent 200)
    assert client.post("/auth/logout", headers=new_headers).status_code == 200
    assert client.post("/auth/logout", headers=new_headers).status_code == 200

    # Sau khi logout, token cũ bị từ chối 401
    after_logout = client.get("/user/profile", headers=new_headers)
    assert after_logout.status_code == 401

    # Thiếu hoặc sai token trả 401
    assert client.get("/user/profile").status_code == 401
    assert client.get("/user/profile", headers={"Authorization": "Bearer invalid.jwt"}).status_code == 401
