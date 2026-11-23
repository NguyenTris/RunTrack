-- =====================================================================
-- RunTrack Database Schema & Seed Data (PostgreSQL 16)
-- Khớp 100% với ORM models.py, services.py và seed.py của runtrack_backend
-- Cách nạp bằng psql:
--   psql -U runtrack_admin -d runtrack_db -f runtrack_schema_and_seed.sql
-- Hoặc mở trực tiếp trong pgAdmin Query Tool và nhấn Execute (F5).
-- Script hỗ trợ chạy lặp lại an toàn (idempotent).
-- =====================================================================

BEGIN;

-- =====================================================================
-- 1. KHỞI TẠO 6 BẢNG CƠ SỞ DỮ LIỆU
-- =====================================================================

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'user',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (NOW() AT TIME ZONE 'UTC')
);

CREATE TABLE IF NOT EXISTS profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id INTEGER NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    full_name VARCHAR(100),
    avatar_url TEXT,
    bio TEXT,
    gender VARCHAR(10),
    height_cm DOUBLE PRECISION,
    weight_kg DOUBLE PRECISION,
    weekly_goal_km DOUBLE PRECISION DEFAULT 40.0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (NOW() AT TIME ZONE 'UTC')
);

CREATE TABLE IF NOT EXISTS run_activities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL DEFAULT 'Buổi chạy bộ',
    distance_km DOUBLE PRECISION NOT NULL,
    duration_seconds INTEGER NOT NULL,
    avg_pace_seconds INTEGER NOT NULL DEFAULT 0,
    calories_burned INTEGER NOT NULL DEFAULT 0,
    start_time TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS token_blacklist (
    id SERIAL PRIMARY KEY,
    token TEXT NOT NULL UNIQUE,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (NOW() AT TIME ZONE 'UTC')
);

CREATE TABLE IF NOT EXISTS clubs (
    id SERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    description TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (NOW() AT TIME ZONE 'UTC')
);

CREATE TABLE IF NOT EXISTS club_members (
    id SERIAL PRIMARY KEY,
    club_id INTEGER NOT NULL REFERENCES clubs(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    joined_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (NOW() AT TIME ZONE 'UTC'),
    CONSTRAINT uq_club_member UNIQUE (club_id, user_id)
);

CREATE INDEX IF NOT EXISTS ix_run_activities_user_id ON run_activities (user_id);
CREATE INDEX IF NOT EXISTS ix_club_members_club_id ON club_members (club_id);
CREATE INDEX IF NOT EXISTS ix_club_members_user_id ON club_members (user_id);

-- =====================================================================
-- 2. NẠP DỮ LIỆU MẪU (USERS, PROFILES, CLUBS, CLUB_MEMBERS, ACTIVITIES)
-- Tài khoản demo:
--   Admin  : admin@runtrack.app   / Admin@123
--   Runners: runner1..3@runtrack.app / Runner@123
-- =====================================================================

-- 2.1. Tài khoản người dùng (bcrypt hash thật tương thích auth.py)
INSERT INTO users (email, password_hash, role) VALUES
    ('admin@runtrack.app', '$2b$12$vKkEgtC9n6imDOOTTLu1YuuuXYfxRyCuxS2RNoRGoJvcvz1eAEKtq', 'admin'),
    ('runner1@runtrack.app', '$2b$12$bqFDFPRM/ocHGMVOrg7feOCX3yeCnILX1wk9bQtB8DTef4Y6HGcle', 'user'),
    ('runner2@runtrack.app', '$2b$12$bqFDFPRM/ocHGMVOrg7feOCX3yeCnILX1wk9bQtB8DTef4Y6HGcle', 'user'),
    ('runner3@runtrack.app', '$2b$12$bqFDFPRM/ocHGMVOrg7feOCX3yeCnILX1wk9bQtB8DTef4Y6HGcle', 'user')
ON CONFLICT (email) DO NOTHING;

-- 2.2. Hồ sơ cá nhân tương ứng
INSERT INTO profiles (user_id, full_name, height_cm, weight_kg, weekly_goal_km)
SELECT u.id, v.full_name, v.height_cm, v.weight_kg, v.weekly_goal_km
FROM (
    VALUES
        ('admin@runtrack.app', 'RunTrack Admin', 175.0, 70.0, 50.0),
        ('runner1@runtrack.app', 'Nguyễn Minh Tuấn', 174.0, 68.0, 45.0),
        ('runner2@runtrack.app', 'Trần Ngọc Mai', 163.0, 52.0, 35.0),
        ('runner3@runtrack.app', 'Lê Hoàng Nam', 178.0, 73.0, 50.0)
) AS v(email, full_name, height_cm, weight_kg, weekly_goal_km)
JOIN users u ON u.email = v.email
ON CONFLICT (user_id) DO NOTHING;

-- 2.3. 3 câu lạc bộ mặc định
INSERT INTO clubs (name, description) VALUES
    ('RunTrack Ho Chi Minh', 'Cộng đồng chạy bộ lớn nhất HCM.'),
    ('Sunrise Runners', 'Tập trung chạy dài vào sáng Chủ Nhật hàng tuần.'),
    ('UIT Runners', 'Club dành cho sinh viên và cựu sinh viên UIT.')
ON CONFLICT (name) DO NOTHING;

-- 2.4. Thành viên tham gia câu lạc bộ
INSERT INTO club_members (club_id, user_id)
SELECT c.id, u.id
FROM (
    VALUES
        ('runner1@runtrack.app', 'RunTrack Ho Chi Minh'),
        ('runner1@runtrack.app', 'UIT Runners'),
        ('runner2@runtrack.app', 'RunTrack Ho Chi Minh'),
        ('runner2@runtrack.app', 'Sunrise Runners'),
        ('runner3@runtrack.app', 'Sunrise Runners'),
        ('runner3@runtrack.app', 'UIT Runners')
) AS m(email, club_name)
JOIN users u ON u.email = m.email
JOIN clubs c ON c.name = m.club_name
ON CONFLICT ON CONSTRAINT uq_club_member DO NOTHING;

-- 2.5. 7 buổi chạy mẫu trong 7 ngày gần nhất
INSERT INTO run_activities (
    id, user_id, title, distance_km, duration_seconds, avg_pace_seconds, calories_burned, start_time
)
SELECT
    a.id::uuid,
    u.id,
    a.title,
    a.distance_km,
    a.duration_seconds,
    a.avg_pace_seconds,
    a.calories_burned,
    a.start_time
FROM (
    VALUES
        ('11111111-1111-4111-8111-000000000001', 'runner1@runtrack.app', 'Chạy sáng Công viên Gia Định', 6.2, 1980, 319, 403, NOW() - INTERVAL '6 days 2 hours'),
        ('11111111-1111-4111-8111-000000000002', 'runner2@runtrack.app', 'Chạy nhẹ quanh hồ Đá', 4.5, 1620, 360, 292, NOW() - INTERVAL '5 days 2 hours'),
        ('11111111-1111-4111-8111-000000000003', 'runner3@runtrack.app', 'Tempo Run Thủ Thiêm', 8.4, 2400, 285, 546, NOW() - INTERVAL '4 days 2 hours'),
        ('11111111-1111-4111-8111-000000000004', 'runner1@runtrack.app', 'Chạy phục hồi buổi chiều', 5.0, 1740, 348, 325, NOW() - INTERVAL '3 days 2 hours'),
        ('11111111-1111-4111-8111-000000000005', 'runner2@runtrack.app', 'Interval 5K sáng sớm', 5.2, 1680, 323, 338, NOW() - INTERVAL '2 days 2 hours'),
        ('11111111-1111-4111-8111-000000000006', 'runner3@runtrack.app', 'Long Run cuối tuần', 12.5, 4200, 336, 812, NOW() - INTERVAL '1 day 2 hours'),
        ('11111111-1111-4111-8111-000000000007', 'runner1@runtrack.app', 'Khởi động đầu ngày', 4.8, 1560, 325, 312, NOW() - INTERVAL '2 hours')
) AS a(id, email, title, distance_km, duration_seconds, avg_pace_seconds, calories_burned, start_time)
JOIN users u ON u.email = a.email
ON CONFLICT (id) DO NOTHING;

COMMIT;
