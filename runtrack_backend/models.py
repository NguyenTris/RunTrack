import uuid
from datetime import datetime, timezone
from sqlalchemy import Boolean, Column, DateTime, Float, ForeignKey, Integer, String, Text, UniqueConstraint, Uuid
from database import Base


class UserDB(Base):
    """Bảng tài khoản người dùng (khớp schema users trên PostgreSQL VPS)."""

    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    email = Column(String(255), unique=True, index=True, nullable=False)
    password_hash = Column(String(255), nullable=False)
    role = Column(String(20), nullable=False, default="user")
    avatar_url = Column(String(500), nullable=True)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc).replace(tzinfo=None))


class UserProfileDB(Base):
    """Bảng hồ sơ cá nhân (khớp schema profiles trên PostgreSQL VPS)."""

    __tablename__ = "profiles"

    id = Column(Uuid(as_uuid=False), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), unique=True, nullable=False)
    full_name = Column(String(100), nullable=True)
    avatar_url = Column(Text, nullable=True)
    bio = Column(Text, nullable=True)
    gender = Column(String(10), nullable=True)
    height_cm = Column(Float, nullable=True)
    weight_kg = Column(Float, nullable=True)
    weekly_goal_km = Column(Float, default=40.0)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc).replace(tzinfo=None))


class ActivityDB(Base):
    """Bảng lịch sử buổi chạy (khớp schema run_activities trên PostgreSQL VPS)."""

    __tablename__ = "run_activities"

    id = Column(Uuid(as_uuid=False), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    title = Column(String(150), nullable=False, default="Buổi chạy bộ")
    distance_km = Column(Float, nullable=False)
    duration_seconds = Column(Integer, nullable=False)
    avg_pace_seconds = Column(Integer, nullable=False, default=0)
    calories_burned = Column(Integer, nullable=False, default=0)
    start_time = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))
    route_points = Column(Text, nullable=True)


class TokenBlacklistDB(Base):
    """Bảng lưu JWT đã thu hồi khi người dùng đăng xuất."""

    __tablename__ = "token_blacklist"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    token = Column(Text, unique=True, index=True, nullable=False)
    expires_at = Column(DateTime, nullable=False)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc).replace(tzinfo=None))


class ClubDB(Base):
    """Bảng câu lạc bộ chạy bộ."""

    __tablename__ = "clubs"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    name = Column(String(120), unique=True, index=True, nullable=False)
    description = Column(Text, nullable=False, default="")
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc).replace(tzinfo=None))


class ClubMemberDB(Base):
    """Bảng thành viên tham gia câu lạc bộ."""

    __tablename__ = "club_members"
    __table_args__ = (UniqueConstraint("club_id", "user_id", name="uq_club_member"),)

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    club_id = Column(Integer, ForeignKey("clubs.id", ondelete="CASCADE"), nullable=False, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    joined_at = Column(DateTime, default=lambda: datetime.now(timezone.utc).replace(tzinfo=None))


class ClubEventDB(Base):
    """Bảng sự kiện chạy bộ của câu lạc bộ."""

    __tablename__ = "club_events"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    club_id = Column(Integer, ForeignKey("clubs.id", ondelete="CASCADE"), nullable=False, index=True)
    title = Column(String(150), nullable=False)
    schedule_text = Column(String(120), nullable=False, default="")
    location = Column(String(200), nullable=False, default="")
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc).replace(tzinfo=None))


class ClubEventRegistrationDB(Base):
    """Bảng đăng ký tham gia sự kiện câu lạc bộ."""

    __tablename__ = "club_event_registrations"
    __table_args__ = (UniqueConstraint("event_id", "user_id", name="uq_club_event_registration"),)

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    event_id = Column(Integer, ForeignKey("club_events.id", ondelete="CASCADE"), nullable=False, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    registered_at = Column(DateTime, default=lambda: datetime.now(timezone.utc).replace(tzinfo=None))


class GpsArtRouteDB(Base):
    """Bảng tuyến đường vẽ hình GPS Art."""

    __tablename__ = "gps_art_routes"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    name = Column(String(150), unique=True, index=True, nullable=False)
    distance_km = Column(Float, nullable=False)
    city = Column(String(100), nullable=False, default="Hà Nội")
    icon_key = Column(String(50), nullable=False, default="star")
    points_json = Column(Text, nullable=False, default="[]")
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc).replace(tzinfo=None))


class PasswordResetTokenDB(Base):
    """Bảng lưu mã OTP đặt lại mật khẩu có thời hạn."""

    __tablename__ = "password_reset_tokens"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    otp_code = Column(String(10), nullable=False)
    expires_at = Column(DateTime, nullable=False)
    used = Column(Integer, nullable=False, default=0)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc).replace(tzinfo=None))


class ChatMessageDB(Base):
    """Bảng lưu tin nhắn chat người dùng và câu lạc bộ."""

    __tablename__ = "chat_messages"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    conversation_id = Column(String(100), nullable=False, index=True)
    sender_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    receiver_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=True, index=True)
    club_id = Column(Integer, ForeignKey("clubs.id", ondelete="CASCADE"), nullable=True, index=True)
    content = Column(Text, nullable=False)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc).replace(tzinfo=None))
    is_read = Column(Boolean, default=False, nullable=False)


class FriendshipDB(Base):
    """Bảng mối quan hệ bạn bè giữa người dùng."""

    __tablename__ = "friendships"
    __table_args__ = (UniqueConstraint("requester_id", "addressee_id", name="uq_friendship"),)

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    requester_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    addressee_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    status = Column(String(20), nullable=False, default="pending")  # "pending" hoặc "accepted"
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc).replace(tzinfo=None))
    updated_at = Column(
        DateTime,
        default=lambda: datetime.now(timezone.utc).replace(tzinfo=None),
        onupdate=lambda: datetime.now(timezone.utc).replace(tzinfo=None),
    )


