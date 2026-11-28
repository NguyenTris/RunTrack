import pytest

from auth import create_access_token, get_password_hash
from models import UserDB, UserProfileDB


@pytest.fixture
def friend_users(db_session):
    """Fixture tạo 3 người dùng để kiểm thử Friends & Chat API."""
    u1 = db_session.query(UserDB).filter(UserDB.email == "friend_u1@test.com").first()
    if not u1:
        u1 = UserDB(email="friend_u1@test.com", password_hash=get_password_hash("Pass123"))
        db_session.add(u1)
        db_session.commit()
        db_session.refresh(u1)
        db_session.add(UserProfileDB(user_id=u1.id, full_name="User Friend One"))
        db_session.commit()

    u2 = db_session.query(UserDB).filter(UserDB.email == "friend_u2@test.com").first()
    if not u2:
        u2 = UserDB(email="friend_u2@test.com", password_hash=get_password_hash("Pass123"))
        db_session.add(u2)
        db_session.commit()
        db_session.refresh(u2)
        db_session.add(UserProfileDB(user_id=u2.id, full_name="User Friend Two"))
        db_session.commit()

    u3 = db_session.query(UserDB).filter(UserDB.email == "friend_u3@test.com").first()
    if not u3:
        u3 = UserDB(email="friend_u3@test.com", password_hash=get_password_hash("Pass123"))
        db_session.add(u3)
        db_session.commit()
        db_session.refresh(u3)
        db_session.add(UserProfileDB(user_id=u3.id, full_name="User Friend Three"))
        db_session.commit()

    t1 = create_access_token({"sub": u1.email})
    t2 = create_access_token({"sub": u2.email})
    t3 = create_access_token({"sub": u3.email})

    return {
        "u1": u1, "u2": u2, "u3": u3,
        "h1": {"Authorization": f"Bearer {t1}"},
        "h2": {"Authorization": f"Bearer {t2}"},
        "h3": {"Authorization": f"Bearer {t3}"},
    }


def test_send_and_accept_friend_request(client, friend_users):
    u1 = friend_users["u1"]
    u2 = friend_users["u2"]
    h1 = friend_users["h1"]
    h2 = friend_users["h2"]

    # 1. Gửi lời mời kết bạn từ User 1 -> User 2
    res_req = client.post(f"/friends/request/{u2.id}", headers=h1)
    assert res_req.status_code == 200
    assert res_req.json()["status"] == "pending_sent"

    # 2. Kiểm tra overview phía User 1
    ov1 = client.get("/friends", headers=h1).json()
    assert len(ov1["pending_sent"]) == 1
    assert ov1["pending_sent"][0]["id"] == u2.id

    # 3. Kiểm tra overview phía User 2
    ov2 = client.get("/friends", headers=h2).json()
    assert len(ov2["pending_received"]) == 1
    assert ov2["pending_received"][0]["id"] == u1.id

    # 4. User 2 chấp nhận lời mời kết bạn
    res_accept = client.post(f"/friends/accept/{u1.id}", headers=h2)
    assert res_accept.status_code == 200
    assert res_accept.json()["status"] == "accepted"

    # 5. Kiểm tra overview bạn bè đã xác nhận
    ov1_after = client.get("/friends", headers=h1).json()
    assert len(ov1_after["friends"]) == 1
    assert ov1_after["friends"][0]["id"] == u2.id


def test_auto_accept_friend_request(client, friend_users):
    u2 = friend_users["u2"]
    u3 = friend_users["u3"]
    h2 = friend_users["h2"]
    h3 = friend_users["h3"]

    # User 2 gửi lời mời kết bạn cho User 3
    client.post(f"/friends/request/{u3.id}", headers=h2)

    # User 3 cũng gửi lời mời kết bạn cho User 2 -> Tự động chuyển status thành accepted
    res = client.post(f"/friends/request/{u2.id}", headers=h3)
    assert res.status_code == 200
    assert res.json()["status"] == "accepted"


def test_search_friends_with_status(client, friend_users):
    u1 = friend_users["u1"]
    u2 = friend_users["u2"]
    h1 = friend_users["h1"]

    # Tìm kiếm User 2 khi chưa có lời mời
    res_search1 = client.get(f"/friends/search?q={u2.email}", headers=h1)
    assert res_search1.status_code == 200
    items1 = res_search1.json()
    assert len(items1) >= 1
    assert items1[0]["friend_status"] == "none"

    # Gửi lời mời -> Kiểm tra friend_status chuyển thành pending_sent
    client.post(f"/friends/request/{u2.id}", headers=h1)
    res_search2 = client.get(f"/friends/search?q={u2.email}", headers=h1)
    assert res_search2.json()[0]["friend_status"] == "pending_sent"


def test_chat_permission_enforcement(client, friend_users):
    u1 = friend_users["u1"]
    u2 = friend_users["u2"]
    h1 = friend_users["h1"]
    h2 = friend_users["h2"]

    conv_id = f"dm_{min(u1.id, u2.id)}_{max(u1.id, u2.id)}"

    # 1. Chưa kết bạn -> nhắn tin bị trả về 403 Forbidden
    send_payload = {"receiver_id": u2.id, "content": "Hello khi chưa kết bạn"}
    res_send_fail = client.post("/chat/messages", json=send_payload, headers=h1)
    assert res_send_fail.status_code == 403
    assert "chưa kết bạn" in res_send_fail.json()["detail"]

    # Lấy danh sách tin nhắn cũng bị 403
    res_get_fail = client.get(f"/chat/messages?conversation_id={conv_id}", headers=h1)
    assert res_get_fail.status_code == 403

    # 2. Kết bạn thành công
    client.post(f"/friends/request/{u2.id}", headers=h1)
    client.post(f"/friends/accept/{u1.id}", headers=h2)

    # 3. Đã kết bạn -> Nhắn tin thành công 201
    res_send_ok = client.post("/chat/messages", json=send_payload, headers=h1)
    assert res_send_ok.status_code == 201
    assert res_send_ok.json()["content"] == "Hello khi chưa kết bạn"

    # 4. Hủy kết bạn
    res_unfriend = client.delete(f"/friends/{u2.id}", headers=h1)
    assert res_unfriend.status_code == 200

    # 5. Sau khi hủy kết bạn -> nhắn tin lại bị 403 Forbidden
    res_send_block = client.post("/chat/messages", json=send_payload, headers=h1)
    assert res_send_block.status_code == 403
