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
-- 1. KHỞI TẠO 10 BẢNG CƠ SỞ DỮ LIỆU
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
    start_time TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    route_points TEXT
);

ALTER TABLE run_activities ADD COLUMN IF NOT EXISTS route_points TEXT;

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

CREATE TABLE IF NOT EXISTS club_events (
    id SERIAL PRIMARY KEY,
    club_id INTEGER NOT NULL REFERENCES clubs(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    schedule_text VARCHAR(120) NOT NULL DEFAULT '',
    location VARCHAR(200) NOT NULL DEFAULT '',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (NOW() AT TIME ZONE 'UTC')
);

CREATE TABLE IF NOT EXISTS club_event_registrations (
    id SERIAL PRIMARY KEY,
    event_id INTEGER NOT NULL REFERENCES club_events(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    registered_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (NOW() AT TIME ZONE 'UTC'),
    CONSTRAINT uq_club_event_registration UNIQUE (event_id, user_id)
);

CREATE TABLE IF NOT EXISTS gps_art_routes (
    id SERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    distance_km DOUBLE PRECISION NOT NULL,
    city VARCHAR(100) NOT NULL DEFAULT 'Hà Nội',
    icon_key VARCHAR(50) NOT NULL DEFAULT 'star',
    points_json TEXT NOT NULL DEFAULT '[]',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (NOW() AT TIME ZONE 'UTC')
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    otp_code VARCHAR(10) NOT NULL,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    used INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (NOW() AT TIME ZONE 'UTC')
);

CREATE INDEX IF NOT EXISTS ix_run_activities_user_id ON run_activities (user_id);
CREATE INDEX IF NOT EXISTS ix_club_members_club_id ON club_members (club_id);
CREATE INDEX IF NOT EXISTS ix_club_members_user_id ON club_members (user_id);
CREATE INDEX IF NOT EXISTS ix_club_events_club_id ON club_events (club_id);
CREATE INDEX IF NOT EXISTS ix_club_event_registrations_event_id ON club_event_registrations (event_id);
CREATE INDEX IF NOT EXISTS ix_club_event_registrations_user_id ON club_event_registrations (user_id);
CREATE INDEX IF NOT EXISTS ix_password_reset_tokens_user_id ON password_reset_tokens (user_id);

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

-- 2.6. 3 sự kiện câu lạc bộ mẫu
INSERT INTO club_events (club_id, title, schedule_text, location)
SELECT c.id, e.title, e.schedule_text, e.location
FROM (
    VALUES
        ('RunTrack Ho Chi Minh', 'Long Run Công Viên Gia Định 10K', 'Chủ Nhật 05:30', 'Công viên Gia Định, Phú Nhuận'),
        ('Sunrise Runners', 'Sunrise Tempo Cầu Ba Son', 'Thứ Bảy 05:15', 'Công viên Bến Bạch Đằng, Quận 1'),
        ('UIT Runners', 'Chạy Giao Lưu Hồ Đá 5K', 'Chủ Nhật 06:00', 'Cổng chính ĐH CNTT (UIT), Thủ Đức')
) AS e(club_name, title, schedule_text, location)
JOIN clubs c ON c.name = e.club_name
WHERE NOT EXISTS (
    SELECT 1 FROM club_events ce WHERE ce.club_id = c.id AND ce.title = e.title
);

-- 2.7. 4 tuyến đường GPS Art mẫu
INSERT INTO gps_art_routes (name, distance_km, city, icon_key, points_json) VALUES
    (
        'Trái Tim Hồ Gươm',
        5.2,
        'Hà Nội',
        'heart',
        '[{"lat":21.0285,"lng":105.8522},{"lat":21.0310,"lng":105.8500},{"lat":21.0322,"lng":105.8520},{"lat":21.0308,"lng":105.8542},{"lat":21.0322,"lng":105.8564},{"lat":21.0308,"lng":105.8582},{"lat":21.0276,"lng":105.8562},{"lat":21.0252,"lng":105.8542},{"lat":21.0276,"lng":105.8522},{"lat":21.0285,"lng":105.8522}]'
    ),
    (
        'Khủng Long Hồ Tây',
        8.6,
        'Hà Nội',
        'dino',
        '[{"lat":21.0450,"lng":105.8180},{"lat":21.0485,"lng":105.8205},{"lat":21.0520,"lng":105.8220},{"lat":21.0510,"lng":105.8260},{"lat":21.0475,"lng":105.8285},{"lat":21.0440,"lng":105.8265},{"lat":21.0425,"lng":105.8230},{"lat":21.0450,"lng":105.8180}]'
    ),
    (
        'Sét Đánh Thủ Thiêm',
        6.4,
        'TP.HCM',
        'bolt',
        '[{"lat":10.7785,"lng":106.7090},{"lat":10.7745,"lng":106.7055},{"lat":10.7740,"lng":106.7100},{"lat":10.7690,"lng":106.7065},{"lat":10.7725,"lng":106.7130},{"lat":10.7735,"lng":106.7095},{"lat":10.7785,"lng":106.7090}]'
    ),
    (
        'Ngôi Sao Công Viên Gia Định',
        4.8,
        'TP.HCM',
        'star',
        '[{"lat":10.8135,"lng":106.6765},{"lat":10.8108,"lng":106.6780},{"lat":10.8105,"lng":106.6812},{"lat":10.8085,"lng":106.6788},{"lat":10.8058,"lng":106.6798},{"lat":10.8075,"lng":106.6765},{"lat":10.8058,"lng":106.6732},{"lat":10.8085,"lng":106.6742},{"lat":10.8105,"lng":106.6718},{"lat":10.8135,"lng":106.6765}]'
    )
ON CONFLICT (name) DO NOTHING;

COMMIT;
