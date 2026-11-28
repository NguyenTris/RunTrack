import pytest

from auth import create_access_token, get_password_hash
from models import UserDB, UserProfileDB


@pytest.fixture
def test_users(db_session):
    """Fixture tạo 2 người dùng để kiểm thử Chat API."""
    u1 = db_session.query(UserDB).filter(UserDB.email == "chat_user1@test.com").first()
    if not u1:
        u1 = UserDB(email="chat_user1@test.com", password_hash=get_password_hash("Pass123"))
        db_session.add(u1)
        db_session.commit()
        db_session.refresh(u1)
        db_session.add(UserProfileDB(user_id=u1.id, full_name="User Chat One"))
        db_session.commit()

    u2 = db_session.query(UserDB).filter(UserDB.email == "chat_user2@test.com").first()
    if not u2:
        u2 = UserDB(email="chat_user2@test.com", password_hash=get_password_hash("Pass123"))
        db_session.add(u2)
        db_session.commit()
        db_session.refresh(u2)
        db_session.add(UserProfileDB(user_id=u2.id, full_name="User Chat Two"))
        db_session.commit()

    token1 = create_access_token({"sub": u1.email})
    token2 = create_access_token({"sub": u2.email})
    return {"u1": u1, "u2": u2, "token1": token1, "token2": token2}


def test_send_and_get_chat_messages(client, test_users):
    token1 = test_users["token1"]
    u2 = test_users["u2"]
    headers1 = {"Authorization": f"Bearer {token1}"}

    # 1. Send message from User 1 to User 2
    send_payload = {
        "receiver_id": u2.id,
        "content": "Xin chào từ User 1!",
    }
    response = client.post("/chat/messages", json=send_payload, headers=headers1)
    assert response.status_code == 201
    data = response.json()
    assert data["content"] == "Xin chào từ User 1!"
    assert data["is_from_me"] is True
    conv_id = data["conversation_id"]
    assert conv_id == f"dm_{min(test_users['u1'].id, u2.id)}_{max(test_users['u1'].id, u2.id)}"

    # 2. Get messages for this conversation as User 2
    token2 = test_users["token2"]
    headers2 = {"Authorization": f"Bearer {token2}"}
    get_res = client.get(f"/chat/messages?conversation_id={conv_id}", headers=headers2)
    assert get_res.status_code == 200
    msg_list = get_res.json()
    assert len(msg_list) >= 1
    last_msg = msg_list[-1]
    assert last_msg["content"] == "Xin chào từ User 1!"
    assert last_msg["is_from_me"] is False

    # 3. Get conversations list as User 2
    conv_res = client.get("/chat/conversations", headers=headers2)
    assert conv_res.status_code == 200
    convs = conv_res.json()
    assert any(c["conversation_id"] == conv_id for c in convs)
