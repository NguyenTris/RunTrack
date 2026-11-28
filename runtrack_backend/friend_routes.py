from typing import List

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy import and_, or_
from sqlalchemy.orm import Session

from auth import get_current_user
from database import get_db
from models import FriendshipDB, UserDB, UserProfileDB
from schemas import (
    FriendSearchUserItem,
    FriendsOverviewResponse,
    FriendUserItem,
)

router = APIRouter(prefix="/friends", tags=["Friends System"])


def _build_user_item(user: UserDB, profile: UserProfileDB) -> FriendUserItem:
    full_name = profile.full_name if profile and profile.full_name else user.email.split("@")[0]
    avatar = getattr(user, "avatar_url", None) or (profile.avatar_url if profile else None)
    return FriendUserItem(
        id=user.id,
        full_name=full_name,
        email=user.email,
        avatar_url=avatar,
    )


@router.get("", response_model=FriendsOverviewResponse)
async def get_friends_overview(
    current_user: UserDB = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """Lấy danh sách bạn bè đã xác nhận, cùng lời mời kết bạn nhận được và đã gửi."""
    all_friendships = (
        db.query(FriendshipDB)
        .filter(
            or_(
                FriendshipDB.requester_id == current_user.id,
                FriendshipDB.addressee_id == current_user.id,
            )
        )
        .all()
    )

    accepted_ids = set()
    pending_received_ids = set()
    pending_sent_ids = set()

    for f in all_friendships:
        if f.status == "accepted":
            friend_id = f.addressee_id if f.requester_id == current_user.id else f.requester_id
            accepted_ids.add(friend_id)
        elif f.status == "pending":
            if f.addressee_id == current_user.id:
                pending_received_ids.add(f.requester_id)
            elif f.requester_id == current_user.id:
                pending_sent_ids.add(f.addressee_id)

    all_target_ids = accepted_ids | pending_received_ids | pending_sent_ids
    if not all_target_ids:
        return FriendsOverviewResponse()

    users_and_profiles = (
        db.query(UserDB, UserProfileDB)
        .outerjoin(UserProfileDB, UserDB.id == UserProfileDB.user_id)
        .filter(UserDB.id.in_(all_target_ids))
        .all()
    )

    user_map = {u.id: _build_user_item(u, p) for u, p in users_and_profiles}

    friends_list = [user_map[uid] for uid in accepted_ids if uid in user_map]
    received_list = [user_map[uid] for uid in pending_received_ids if uid in user_map]
    sent_list = [user_map[uid] for uid in pending_sent_ids if uid in user_map]

    return FriendsOverviewResponse(
        friends=friends_list,
        pending_received=received_list,
        pending_sent=sent_list,
    )


@router.get("/search", response_model=List[FriendSearchUserItem])
async def search_friends(
    q: str = Query(default="", description="Từ khóa tìm kiếm theo tên hoặc email"),
    current_user: UserDB = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """Tìm kiếm người dùng khác kèm trạng thái quan hệ bạn bè với user hiện tại."""
    query = (
        db.query(UserDB, UserProfileDB)
        .outerjoin(UserProfileDB, UserDB.id == UserProfileDB.user_id)
        .filter(UserDB.id != current_user.id)
    )

    keyword = q.strip()
    if keyword:
        pattern = f"%{keyword}%"
        query = query.filter(
            or_(
                UserProfileDB.full_name.ilike(pattern),
                UserDB.email.ilike(pattern),
            )
        )

    matched_users = query.limit(50).all()
    if not matched_users:
        return []

    target_ids = [u.id for u, _ in matched_users]
    friendships = (
        db.query(FriendshipDB)
        .filter(
            or_(
                and_(FriendshipDB.requester_id == current_user.id, FriendshipDB.addressee_id.in_(target_ids)),
                and_(FriendshipDB.addressee_id == current_user.id, FriendshipDB.requester_id.in_(target_ids)),
            )
        )
        .all()
    )

    rel_map = {}
    for f in friendships:
        other_id = f.addressee_id if f.requester_id == current_user.id else f.requester_id
        if f.status == "accepted":
            rel_map[other_id] = "accepted"
        elif f.status == "pending":
            if f.requester_id == current_user.id:
                rel_map[other_id] = "pending_sent"
            else:
                rel_map[other_id] = "pending_received"

    result = []
    for u, p in matched_users:
        item = _build_user_item(u, p)
        st = rel_map.get(u.id, "none")
        result.append(
            FriendSearchUserItem(
                id=item.id,
                full_name=item.full_name,
                email=item.email,
                avatar_url=item.avatar_url,
                friend_status=st,
            )
        )
    return result


@router.post("/request/{target_user_id}")
async def send_friend_request(
    target_user_id: int,
    current_user: UserDB = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """Gửi lời mời kết bạn. Tự động chấp nhận nếu đối phương đã gửi lời mời trước đó."""
    if target_user_id == current_user.id:
        raise HTTPException(status_code=400, detail="Không thể gửi lời mời kết bạn cho chính mình")

    target_user = db.query(UserDB).filter(UserDB.id == target_user_id).first()
    if not target_user:
        raise HTTPException(status_code=444, detail="Người dùng không tồn tại")

    existing = (
        db.query(FriendshipDB)
        .filter(
            or_(
                and_(FriendshipDB.requester_id == current_user.id, FriendshipDB.addressee_id == target_user_id),
                and_(FriendshipDB.requester_id == target_user_id, FriendshipDB.addressee_id == current_user.id),
            )
        )
        .first()
    )

    if existing:
        if existing.status == "accepted":
            raise HTTPException(status_code=400, detail="Hai người đã là bạn bè")

        if existing.requester_id == current_user.id and existing.status == "pending":
            return {"message": "Lời mời kết bạn đã được gửi trước đó", "status": "pending_sent"}

        if existing.addressee_id == current_user.id and existing.status == "pending":
            existing.status = "accepted"
            db.commit()
            return {"message": "Đã trở thành bạn bè", "status": "accepted"}

    new_friendship = FriendshipDB(
        requester_id=current_user.id,
        addressee_id=target_user_id,
        status="pending",
    )
    db.add(new_friendship)
    db.commit()

    return {"message": "Đã gửi lời mời kết bạn", "status": "pending_sent"}


@router.post("/accept/{requester_id}")
async def accept_friend_request(
    requester_id: int,
    current_user: UserDB = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """Chấp nhận lời mời kết bạn từ requester_id."""
    friendship = (
        db.query(FriendshipDB)
        .filter(
            FriendshipDB.requester_id == requester_id,
            FriendshipDB.addressee_id == current_user.id,
            FriendshipDB.status == "pending",
        )
        .first()
    )

    if not friendship:
        raise HTTPException(status_code=404, detail="Không tìm thấy lời mời kết bạn hợp lệ")

    friendship.status = "accepted"
    db.commit()
    return {"message": "Đã chấp nhận lời mời kết bạn", "status": "accepted"}


@router.delete("/{target_user_id}")
async def remove_friendship(
    target_user_id: int,
    current_user: UserDB = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """Hủy lời mời, từ chối lời mời hoặc hủy kết bạn với target_user_id."""
    friendship = (
        db.query(FriendshipDB)
        .filter(
            or_(
                and_(FriendshipDB.requester_id == current_user.id, FriendshipDB.addressee_id == target_user_id),
                and_(FriendshipDB.requester_id == target_user_id, FriendshipDB.addressee_id == current_user.id),
            )
        )
        .first()
    )

    if not friendship:
        raise HTTPException(status_code=404, detail="Không tìm thấy mối quan hệ bạn bè")

    db.delete(friendship)
    db.commit()
    return {"message": "Đã hủy lời mời hoặc hủy kết bạn thành công", "status": "none"}
