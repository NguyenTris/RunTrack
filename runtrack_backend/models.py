import uuid
from datetime import datetime, timezone
from sqlalchemy import Column, DateTime, Float, ForeignKey, Integer, String, Text, Uuid
from database import Base


class UserDB(Base):
    """Bảng tài khoản người dùng (khớp schema users trên PostgreSQL VPS)."""

    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    email = Column(String(255), unique=True, index=True, nullable=False)
    password_hash = Column(String(255), nullable=False)
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


class TokenBlacklistDB(Base):
    """Bảng lưu JWT đã thu hồi khi người dùng đăng xuất."""

    __tablename__ = "token_blacklist"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    token = Column(Text, unique=True, index=True, nullable=False)
    expires_at = Column(DateTime, nullable=False)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc).replace(tzinfo=None))
