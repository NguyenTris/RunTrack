from datetime import datetime, timedelta, timezone
from typing import List, Optional

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session

from auth import get_current_user
from database import get_db
from models import ChatMessageDB, ClubDB, ClubMemberDB, UserDB, UserProfileDB
from schemas import ChatMessageResponse, ConversationResponse, SendMessageRequest
from services import VN_TZ, ensure_utc

router = APIRouter(tags=["Chat System"])


def format_chat_time(dt: Optional[datetime]) -> str:
    if not dt:
        return ""
    dt_utc = ensure_utc(dt)
    dt_vn = dt_utc.astimezone(VN_TZ)
    now_vn = datetime.now(timezone.utc).astimezone(VN_TZ)
    if dt_vn.date() == now_vn.date():
        return dt_vn.strftime("%H:%M")
    elif dt_vn.date() == (now_vn - timedelta(days=1)).date():
        return "HÔM QUA"
    else:
        return dt_vn.strftime("%d/%m")


@router.get("/chat/conversations", response_model=List[ConversationResponse])
async def get_conversations(
    current_user: UserDB = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    joined_clubs = (
        db.query(ClubDB)
        .join(ClubMemberDB, ClubDB.id == ClubMemberDB.club_id)
        .filter(ClubMemberDB.user_id == current_user.id)
        .all()
    )

    other_users = (
        db.query(UserDB, UserProfileDB)
        .outerjoin(UserProfileDB, UserDB.id == UserProfileDB.user_id)
        .filter(UserDB.id != current_user.id)
        .all()
    )

    conversations = []

    for user, profile in other_users:
        min_id, max_id = sorted([current_user.id, user.id])
        conv_id = f"dm_{min_id}_{max_id}"

        latest_msg = (
            db.query(ChatMessageDB)
            .filter(ChatMessageDB.conversation_id == conv_id)
            .order_by(ChatMessageDB.created_at.desc(), ChatMessageDB.id.desc())
            .first()
        )

        unread_cnt = (
            db.query(ChatMessageDB)
            .filter(
                ChatMessageDB.conversation_id == conv_id,
                ChatMessageDB.sender_id != current_user.id,
                ChatMessageDB.is_read == False,
            )
            .count()
        )

        name = profile.full_name if (profile and profile.full_name) else user.email.split("@")[0]
        avatar = getattr(user, "avatar_url", None) or (profile.avatar_url if profile else None)
        last_content = latest_msg.content if latest_msg else "Chưa có tin nhắn"
        time_str = format_chat_time(latest_msg.created_at) if latest_msg else ""
        created_iso = latest_msg.created_at.isoformat() if latest_msg else ""

        conversations.append(
            ConversationResponse(
                conversation_id=conv_id,
                name=name,
                last_message=last_content,
                time=time_str,
                created_at=created_iso,
                unread_count=unread_cnt,
                is_online=True,
                is_group=False,
                category="friends",
                receiver_id=user.id,
                club_id=None,
                avatar_url=avatar,
            )
        )

    for club in joined_clubs:
        conv_id = f"club_{club.id}"
        latest_msg = (
            db.query(ChatMessageDB)
            .filter(ChatMessageDB.conversation_id == conv_id)
            .order_by(ChatMessageDB.created_at.desc(), ChatMessageDB.id.desc())
            .first()
        )

        unread_cnt = (
            db.query(ChatMessageDB)
            .filter(
                ChatMessageDB.conversation_id == conv_id,
                ChatMessageDB.sender_id != current_user.id,
                ChatMessageDB.is_read == False,
            )
            .count()
        )

        last_content = latest_msg.content if latest_msg else "Chưa có tin nhắn trong nhóm"
        time_str = format_chat_time(latest_msg.created_at) if latest_msg else ""
        created_iso = latest_msg.created_at.isoformat() if latest_msg else ""

        conversations.append(
            ConversationResponse(
                conversation_id=conv_id,
                name=club.name,
                last_message=last_content,
                time=time_str,
                created_at=created_iso,
                unread_count=unread_cnt,
                is_online=False,
                is_group=True,
                category="groups",
                receiver_id=None,
                club_id=club.id,
            )
        )

    conversations.sort(key=lambda c: (c.created_at or "", c.name), reverse=True)
    return conversations


@router.get("/chat/messages", response_model=List[ChatMessageResponse])
async def get_messages(
    conversation_id: str = Query(..., min_length=1),
    current_user: UserDB = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    db.query(ChatMessageDB).filter(
        ChatMessageDB.conversation_id == conversation_id,
        ChatMessageDB.sender_id != current_user.id,
        ChatMessageDB.is_read == False,
    ).update({"is_read": True}, synchronize_session=False)
    db.commit()

    messages = (
        db.query(ChatMessageDB)
        .filter(ChatMessageDB.conversation_id == conversation_id)
        .order_by(ChatMessageDB.created_at.asc(), ChatMessageDB.id.asc())
        .all()
    )

    sender_ids = list({m.sender_id for m in messages})
    sender_names = {}
    sender_avatars = {}
    if sender_ids:
        profiles = (
            db.query(UserDB.id, UserDB.email, UserDB.avatar_url, UserProfileDB.full_name, UserProfileDB.avatar_url.label("profile_avatar"))
            .outerjoin(UserProfileDB, UserDB.id == UserProfileDB.user_id)
            .filter(UserDB.id.in_(sender_ids))
            .all()
        )
        for uid, email, u_avatar, fn, p_avatar in profiles:
            sender_names[uid] = fn if fn else email.split("@")[0]
            sender_avatars[uid] = u_avatar or p_avatar

    result = []
    for msg in messages:
        is_from_me = (msg.sender_id == current_user.id)
        s_name = sender_names.get(msg.sender_id, "Thành viên")
        s_avatar = sender_avatars.get(msg.sender_id)
        result.append(
            ChatMessageResponse(
                id=str(msg.id),
                conversation_id=msg.conversation_id,
                sender_id=msg.sender_id,
                sender_name=s_name,
                receiver_id=msg.receiver_id,
                club_id=msg.club_id,
                content=msg.content,
                created_at=msg.created_at.isoformat() if msg.created_at else "",
                is_from_me=is_from_me,
                time=format_chat_time(msg.created_at),
                sender_avatar_url=s_avatar,
            )
        )
    return result


@router.post("/chat/messages", response_model=ChatMessageResponse, status_code=status.HTTP_201_CREATED)
async def send_message(
    data: SendMessageRequest,
    current_user: UserDB = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    conv_id = data.conversation_id
    if not conv_id:
        if data.receiver_id:
            min_id, max_id = sorted([current_user.id, data.receiver_id])
            conv_id = f"dm_{min_id}_{max_id}"
        elif data.club_id:
            conv_id = f"club_{data.club_id}"
        else:
            raise HTTPException(status_code=400, detail="Thiếu conversation_id, receiver_id hoặc club_id")

    now_utc = datetime.now(timezone.utc).replace(tzinfo=None)
    new_msg = ChatMessageDB(
        conversation_id=conv_id,
        sender_id=current_user.id,
        receiver_id=data.receiver_id,
        club_id=data.club_id,
        content=data.content.strip(),
        created_at=now_utc,
        is_read=False,
    )
    db.add(new_msg)
    db.commit()
    db.refresh(new_msg)

    profile = db.query(UserProfileDB).filter(UserProfileDB.user_id == current_user.id).first()
    sender_name = profile.full_name if (profile and profile.full_name) else current_user.email.split("@")[0]
    sender_avatar = getattr(current_user, "avatar_url", None) or (profile.avatar_url if profile else None)

    return ChatMessageResponse(
        id=str(new_msg.id),
        conversation_id=new_msg.conversation_id,
        sender_id=new_msg.sender_id,
        sender_name=sender_name,
        receiver_id=new_msg.receiver_id,
        club_id=new_msg.club_id,
        content=new_msg.content,
        created_at=new_msg.created_at.isoformat(),
        is_from_me=True,
        time=format_chat_time(new_msg.created_at),
        sender_avatar_url=sender_avatar,
    )
