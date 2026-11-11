import os
from urllib.parse import quote_plus
from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base, sessionmaker


def build_database_url() -> str:
    """Xây dựng chuỗi kết nối DB từ DATABASE_URL hoặc các biến DB_* (fallback SQLite khi chạy local)."""
    explicit_url = os.getenv("DATABASE_URL")
    if explicit_url:
        return explicit_url

    db_host = os.getenv("DB_HOST")
    if db_host:
        db_user = quote_plus(os.getenv("DB_USER", "runtrack_admin"))
        db_password = quote_plus(os.getenv("DB_PASSWORD", ""))
        db_port = os.getenv("DB_PORT", "5432")
        db_name = os.getenv("DB_NAME", "runtrack_db")
        return f"postgresql://{db_user}:{db_password}@{db_host}:{db_port}/{db_name}"

    return "sqlite:///./runtrack_local.db"


DATABASE_URL = build_database_url()

engine_kwargs = (
    {"connect_args": {"check_same_thread": False}}
    if DATABASE_URL.startswith("sqlite")
    else {"pool_pre_ping": True}
)

engine = create_engine(DATABASE_URL, **engine_kwargs)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()


def get_db():
    """Cung cấp SQLAlchemy Session cho mỗi request FastAPI."""
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
