# FEATURE_MAP.md — Bản Đồ Tính Năng & Hợp Đồng API (RunTrack)

Tài liệu này là nguồn sự thật duy nhất (Contract-First) cho đội ngũ phát triển (`coordinator`, `backend-dev`, `ui-ux-dev`, `tester`).

## 1. Bất Biến Nghiệp Vụ (Business Invariants)
- **Phân quyền (RBAC):** Mọi tài khoản có `role` thuộc `{"user", "admin"}` (mặc định `"user"`). Toàn bộ endpoint `/admin/*` yêu cầu JWT hợp lệ (401 nếu thiếu/hết hạn/thu hồi) và `role == "admin"` (403 nếu là `"user"`).
- **An toàn xóa Admin:** Admin không được tự xóa chính tài khoản đang đăng nhập (`DELETE /admin/users/{self_id}` trả `400`).
- **Tương thích ngược DB VPS:** Khi Backend khởi động (`lifespan`), tự động kiểm tra và bổ sung cột `users.role` nếu DB cũ chưa có; tạo bảng `clubs` và `club_members` an toàn.
- **Thu hồi phiên:** `POST /auth/logout` đưa JWT vào `token_blacklist`; Android luôn xóa token local dù mạng lỗi.
- **Buổi chạy hợp lệ:** `distance_km > 0`, `duration_seconds > 0`. Khi người dùng dừng chạy trên `TrackingActivity` (máy ảo/demo), quãng đường mô phỏng luôn ≥ `0.1 km` và thời lượng ≥ `1 giây` để lưu thành công qua `POST /activities`.
- **CLB dữ liệu thật:** `GET /search/clubs` và `POST /clubs/{club_id}/toggle-join` đọc/ghi trực tiếp bảng `clubs` & `club_members`, không dùng danh sách hardcode.

## 2. Hợp Đồng API (API Contract)

### 2.1. Auth & User
| Method | Endpoint | Auth | Request / Query | Response |
| :--- | :--- | :---: | :--- | :--- |
| `POST` | `/auth/register` | Public | `{email, password, full_name}` | `200 {message, user}` |
| `POST` | `/auth/login` | Public | `{email, password}` | `200 {access_token, token_type}` |
| `POST` | `/auth/logout` | Bearer | — | `200 {message}` |
| `PUT` | `/auth/change-password` | Bearer | `{old_password, new_password}` | `200 {message}` / `400` |
| `GET` | `/user/profile` | Bearer | — | `200 UserProfileResponse` |
| `PUT` | `/user/profile` | Bearer | `{full_name?, height?, weight?, weekly_goal_km?}` | `200 UserProfileResponse` |

### 2.2. Dashboard, Activities & Search
| Method | Endpoint | Auth | Request / Query | Response |
| :--- | :--- | :---: | :--- | :--- |
| `GET` | `/dashboard/home-stats` | Bearer | — | `200 {total_distance_km, weekly_goal_km, completion_percentage, active_days: int[]}` |
| `GET` | `/dashboard/recent-activities` | Bearer | `?limit=5` | `200 ActivityItem[]` |
| `POST` | `/activities` | Bearer | `{title, distance_km, duration_seconds, avg_pace_seconds?, calories_burned?, start_time?}` | `201 ActivityItem` |
| `GET` | `/activities/weekly` | Bearer | — | `200 WeeklyActivitiesResponse` |
| `GET` | `/search/friends` | Bearer | `?q=&page=1&limit=10` | `200 FriendResponse[]` |
| `GET` | `/search/clubs` | Bearer | `?q=&page=1&limit=10` | `200 ClubResponse[]` |
| `POST` | `/clubs/{club_id}/toggle-join` | Bearer | — | `200 ClubResponse` / `404` |

### 2.3. Admin Stats & CRUD (Tất cả yêu cầu `role == "admin"`)
| Method | Endpoint | Request / Query | Response |
| :--- | :--- | :--- | :--- |
| `GET` | `/admin/stats` | — | `200 {total_users, total_activities, total_distance_km, total_calories, total_clubs, daily_stats[], top_runners[]}` |
| `GET` | `/admin/users` | `?q=&page=1&limit=20` | `200 AdminUserItem[]` |
| `POST` | `/admin/users` | `{email, password, full_name, role="user", weekly_goal_km=40.0}` | `201 AdminUserItem` |
| `PUT` | `/admin/users/{user_id}` | `{full_name?, role?, height_cm?, weight_kg?, weekly_goal_km?}` | `200 AdminUserItem` |
| `DELETE` | `/admin/users/{user_id}` | — | `200 {message}` / `400` / `404` |
| `GET` | `/admin/activities` | `?user_id=&page=1&limit=20` | `200 AdminActivityItem[]` |
| `POST` | `/admin/activities` | `{user_id, title, distance_km, duration_seconds, avg_pace_seconds?, calories_burned?, start_time?}` | `201 AdminActivityItem` |
| `PUT` | `/admin/activities/{activity_id}` | `{title?, distance_km?, duration_seconds?, avg_pace_seconds?, calories_burned?}` | `200 AdminActivityItem` |
| `DELETE` | `/admin/activities/{activity_id}` | — | `200 {message}` / `404` |
| `GET` | `/admin/clubs` | `?q=&page=1&limit=20` | `200 ClubResponse[]` |
| `POST` | `/admin/clubs` | `{name, description=""}` | `201 ClubResponse` |
| `PUT` | `/admin/clubs/{club_id}` | `{name?, description?}` | `200 ClubResponse` |
| `DELETE` | `/admin/clubs/{club_id}` | — | `200 {message}` / `404` |

## 3. Tài Khoản Seed Mặc Định (`runtrack_backend/seed.py`)
- **Admin:** `admin@runtrack.app` / `Admin@123`
- **Runners:** `runner1@runtrack.app`, `runner2@runtrack.app`, `runner3@runtrack.app` / `Runner@123`

## 4. Hợp Đồng API Cốt Lõi Mở Rộng (Core Features Complete)

### 4.1. Auth Quên mật khẩu OTP
| Method | Endpoint | Auth | Request | Response |
| :--- | :--- | :---: | :--- | :--- |
| `POST` | `/auth/forgot-password` | Public | `{email}` | `200 {message, demo_otp?}` (OTP 6 số, hạn 10 phút, trả `demo_otp` khi `DEMO_MODE=true`) |
| `POST` | `/auth/reset-password` | Public | `{email, otp, new_password}` | `200 {message}` / `400` |

### 4.2. Tiến Độ Tháng / Năm & Bảng Xếp Hạng (`progress_routes.py`)
| Method | Endpoint | Auth | Query | Response |
| :--- | :--- | :---: | :--- | :--- |
| `GET` | `/progress/month` | Bearer | `?year=&month=` | `200 {year, month, first_weekday, days_in_month, total_distance_km, total_activities, total_duration_minutes, days[], records[]}` |
| `GET` | `/progress/year` | Bearer | `?year=` | `200 {year, total_distance_km, total_activities, quarters[], records[], achievements[]}` |
| `GET` | `/leaderboard` | Bearer | `?scope=global\|friends&limit=20` | `200 {scope, total_runners, current_user_rank, entries[]}` |

### 4.3. Chi Tiết CLB, Sự Kiện & Tuyến GPS Art (`community_routes.py`)
| Method | Endpoint | Auth | Request | Response |
| :--- | :--- | :---: | :--- | :--- |
| `GET` | `/clubs/{club_id}/detail` | Bearer | — | `200 {club_id, name, description, member_count, is_joined, members[], events[]}` / `404` |
| `POST` | `/clubs/events/{event_id}/toggle-register` | Bearer | — | `200 ClubEventItem` / `404` |
| `GET` | `/gps-art/routes` | Public | — | `200 GpsArtRouteItem[]` (`{id, name, distance_km, city, icon_key, points: [{lat, lng}]}`) |
| `POST` | `/activities` | Bearer | bổ sung `route_points?: [{lat, lng}]` | `201 ActivityItem` (kèm `route_points`) |

## 5. Ranh Giới Sở Hữu (Dev-Squad Disjoint Ownership)
- `backend-dev`: `runtrack_backend/{models.py, schemas.py, services.py, main.py, progress_routes.py, community_routes.py, seed.py, docker-compose.yml, .env.example}`, `Others/database/runtrack_schema_and_seed.sql`
- `ui-ux-dev`: `app/**` (bao gồm `app/build.gradle.kts`, `AndroidManifest.xml`, `local.properties.example`, `network/**`, Progress/Leaderboard/Club/Auth/Profile/GpsArt/Tracking/RunSummary)
- `tester`: `runtrack_backend/test_*.py`, `runtrack_backend/RunTrack_API.postman_collection.json`, `Others/postman/RunTrack_API.postman_collection.json`
