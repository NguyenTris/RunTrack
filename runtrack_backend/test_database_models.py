from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

import database
from models import ActivityDB, Base, TokenBlacklistDB, UserDB, UserProfileDB


def test_build_database_url_variants(monkeypatch):
    monkeypatch.delenv("DATABASE_URL", raising=False)
    monkeypatch.delenv("DB_HOST", raising=False)
    assert database.build_database_url() == "sqlite:///./runtrack_local.db"

    monkeypatch.setenv("DB_HOST", "db")
    monkeypatch.setenv("DB_USER", "runtrack_admin")
    monkeypatch.setenv("DB_PASSWORD", "RunTrackSecure2026!@#")
    monkeypatch.setenv("DB_PORT", "5432")
    monkeypatch.setenv("DB_NAME", "runtrack_db")
    url = database.build_database_url()
    assert url == "postgresql://runtrack_admin:RunTrackSecure2026%21%40%23@db:5432/runtrack_db"

    monkeypatch.setenv("DATABASE_URL", "postgresql://custom:pass@host:5432/custom_db")
    assert database.build_database_url() == "postgresql://custom:pass@host:5432/custom_db"


def test_models_crud_in_memory():
    engine = create_engine("sqlite:///:memory:", connect_args={"check_same_thread": False})
    Base.metadata.create_all(bind=engine)
    Session = sessionmaker(bind=engine)

    with Session() as db:
        user = UserDB(email="unit@runtrack.vn", password_hash="hashed")
        db.add(user)
        db.commit()
        db.refresh(user)

        profile = UserProfileDB(user_id=user.id, full_name="Unit Runner", weekly_goal_km=25.0)
        activity = ActivityDB(user_id=user.id, title="Test Run", distance_km=5.5, duration_seconds=1800)
        db.add_all([profile, activity])
        db.commit()

        assert db.query(UserDB).count() == 1
        assert db.query(UserProfileDB).filter_by(user_id=user.id).first().full_name == "Unit Runner"
        assert db.query(ActivityDB).filter_by(user_id=user.id).first().distance_km == 5.5
        assert TokenBlacklistDB.__tablename__ == "token_blacklist"
