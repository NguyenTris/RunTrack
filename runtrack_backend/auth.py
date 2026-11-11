import os
import uuid
from datetime import datetime, timedelta, timezone
from typing import Optional

import bcrypt
import jwt
from fastapi import Depends, Header, HTTPException
from sqlalchemy.orm import Session

from database import get_db
from models import TokenBlacklistDB, UserDB

SECRET_KEY = os.getenv("SECRET_KEY", "runtrack_super_secret_jwt_key_2026")
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_DAYS = 7


def get_password_hash(password: str) -> str:
    """Băm mật khẩu bằng bcrypt (tối đa 72 byte theo chuẩn bcrypt)."""
    pwd_bytes = password.encode("utf-8")[:72]
    return bcrypt.hashpw(pwd_bytes, bcrypt.gensalt()).decode("utf-8")


def verify_password(plain_password: str, hashed_password: str) -> bool:
    """Kiểm tra mật khẩu thuần khớp với chuỗi băm bcrypt trong DB."""
    if not hashed_password:
        return False
    try:
        pwd_bytes = plain_password.encode("utf-8")[:72]
        return bcrypt.checkpw(pwd_bytes, hashed_password.encode("utf-8"))
    except ValueError:
        return False


def create_access_token(data: dict, expires_delta: timedelta = timedelta(days=ACCESS_TOKEN_EXPIRE_DAYS)) -> str:
    """Tạo JWT HS256 kèm jti duy nhất để hỗ trợ thu hồi từng phiên đăng nhập."""
    to_encode = data.copy()
    expire = datetime.now(timezone.utc) + expires_delta
    to_encode.update({"exp": expire, "jti": str(uuid.uuid4())})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)


def extract_bearer_token(authorization: Optional[str]) -> str:
    """Trích xuất token từ header Authorization: Bearer <token>."""
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(
            status_code=401,
            detail="Không tìm thấy token xác thực hoặc sai định dạng Bearer",
        )
    token = authorization.split(" ", 1)[1].strip()
    if not token:
        raise HTTPException(
            status_code=401,
            detail="Không tìm thấy token xác thực hoặc sai định dạng Bearer",
        )
    return token


def decode_token_payload(token: str) -> dict:
    """Giải mã và xác thực chữ ký, hạn sử dụng và subject của JWT."""
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        if not payload.get("sub"):
            raise HTTPException(status_code=401, detail="Token không hợp lệ")
        return payload
    except jwt.PyJWTError:
        raise HTTPException(status_code=401, detail="Token hết hạn hoặc không hợp lệ")


def is_token_revoked(token: str, db: Session) -> bool:
    """Kiểm tra token có nằm trong bảng token_blacklist hay không."""
    return db.query(TokenBlacklistDB).filter(TokenBlacklistDB.token == token).first() is not None


def revoke_token(token: str, db: Session) -> None:
    """Đưa token vào bảng token_blacklist (idempotent nếu token đã thu hồi trước đó)."""
    if is_token_revoked(token, db):
        return
    payload = decode_token_payload(token)
    exp_ts = payload.get("exp")
    expires_at = (
        datetime.fromtimestamp(exp_ts, tz=timezone.utc).replace(tzinfo=None)
        if exp_ts
        else datetime.now(timezone.utc).replace(tzinfo=None) + timedelta(days=ACCESS_TOKEN_EXPIRE_DAYS)
    )
    db.add(TokenBlacklistDB(token=token, expires_at=expires_at))
    db.commit()


def get_current_user(
    authorization: Optional[str] = Header(None),
    db: Session = Depends(get_db),
) -> UserDB:
    """Dependency xác thực JWT, kiểm tra blacklist và trả về bản ghi UserDB hiện tại."""
    token = extract_bearer_token(authorization)
    if is_token_revoked(token, db):
        raise HTTPException(status_code=401, detail="Token đã bị thu hồi, vui lòng đăng nhập lại")

    payload = decode_token_payload(token)
    email: str = payload["sub"]
    user = db.query(UserDB).filter(UserDB.email == email).first()
    if not user:
        raise HTTPException(status_code=401, detail="Tài khoản không tồn tại hoặc đã bị xóa")
    return user
