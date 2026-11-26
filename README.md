# RunTrack — Ứng Dụng Theo Dõi Chạy Bộ, Sinh Trắc Học & Hệ Thống Quản Trị

**RunTrack** là hệ thống theo dõi chạy bộ đa nền tảng gồm 3 thành phần tích hợp:
1. **Android Application (`app/`):** Ứng dụng di động (Kotlin/Java) hỗ trợ xác thực JWT, thống kê mục tiêu tuần, tính điểm phục hồi 24h (Recovery Score), đo nhịp tim/HRV qua Bluetooth Low Energy (BLE GATT), thời tiết & chỉ số bụi mịn AQI, tìm kiếm bạn bè/CLB và lưu lịch sử buổi chạy.
2. **Backend REST API (`runtrack_backend/`):** Dịch vụ FastAPI + SQLAlchemy 2.x + PostgreSQL 16 (hỗ trợ SQLite khi chạy local), gồm 28 API endpoints (Auth + JWT Blacklist, Profile, Dashboard, Activities, Weather/AQI, Search/Clubs, và Admin Stats + CRUD).
3. **Web-Admin Dashboard (`runtrack_web_admin/`):** Giao diện quản trị React + Vite + TypeScript + Chart.js phục vụ thống kê hệ thống 7 ngày, bảng xếp hạng Top Runners và CRUD đầy đủ 3 bảng dữ liệu (`Users`, `Activities`, `Clubs`).

---

## 1. Yêu Cầu Môi Trường (Prerequisites)

| Thành phần | Phiên bản tối thiểu / khuyến nghị |
| :--- | :--- |
| **Docker & Docker Compose** *(Cách 1 — Khuyến nghị)* | Docker Desktop 24+ / Docker Compose v2 |
| **Python** *(Cách 2 — Chạy Local không cần Docker)* | Python `3.11+` |
| **Node.js & npm** *(Cách 2 — Chạy Local Web-Admin)* | Node.js `20+` (npm `10+`) |
| **Android Studio** *(Build & chạy Android App)* | Android Studio hỗ trợ AGP `9.3.3`, Gradle `9.5.0`, Android SDK `37` (`minSdk = 24`) |

---

## 2. Hướng Dẫn Tái Lập Hệ Thống (Reproducibility Guide)

### Cách 1: Khởi chạy toàn bộ Backend + Database + Web-Admin bằng Docker Compose (1 lệnh)
> Cách nhanh và ổn định nhất để tái lập đồng thời **PostgreSQL 16 + FastAPI Backend + React Web-Admin** kèm dữ liệu mẫu tự động (`AUTO_SEED_DEMO=true`).

```bash
cd runtrack_backend
cp .env.example .env
docker compose up --build -d
```

Sau khi container khởi động (~20–30 giây), truy cập ngay:
* **Web-Admin:** `http://localhost:3000`
* **Backend Swagger UI:** `http://localhost:8000/docs`
* **PostgreSQL:** `localhost:5432` (DB: `runtrack_db`, User: `runtrack_admin`)

> *(Tùy chọn)* Nếu muốn import thủ công bằng `psql`/pgAdmin, sử dụng file SQL chuẩn tại `Others/database/runtrack_schema_and_seed.sql`.

---

### Cách 2: Khởi chạy Local không cần Docker (Windows PowerShell)
Chạy duy nhất **1 lệnh** từ thư mục gốc dự án:

```powershell
powershell -ExecutionPolicy Bypass -File .\run_local_demo.ps1
```

Script tự động tạo `.venv`, cài đặt thư viện, nạp dữ liệu mẫu vào SQLite (`seed.py`) và mở 2 cửa sổ:
* **Web-Admin:** `http://127.0.0.1:5173`
* **Backend Swagger UI:** `http://127.0.0.1:8000/docs`

*(Hoặc chạy thủ công trên macOS/Linux)*:
```bash
# Terminal 1 — Backend
cd runtrack_backend
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
DATABASE_URL="sqlite:///./runtrack_demo.db" AUTO_SEED_DEMO=true uvicorn main:app --host 127.0.0.1 --port 8000

# Terminal 2 — Web-Admin
cd runtrack_web_admin
npm install
VITE_API_BASE_URL="http://127.0.0.1:8000" npm run dev -- --port 5173
```

---

### Tài Khoản Demo Mặc Định
Hệ thống tự động nạp sẵn các tài khoản sau (idempotent):

| Vai trò | Email | Mật khẩu | Mục đích kiểm thử |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin@runtrack.app` | `Admin@123` | Đăng nhập Web-Admin & gọi toàn bộ API `/admin/*` |
| **Runner 1** | `runner1@runtrack.app` | `Runner@123` | Đăng nhập Android App / kiểm thử chặn quyền `403` trên Web-Admin |
| **Runner 2** | `runner2@runtrack.app` | `Runner@123` | Kiểm thử tìm kiếm bạn bè & bảng xếp hạng |
| **Runner 3** | `runner3@runtrack.app` | `Runner@123` | Kiểm thử tìm kiếm bạn bè & bảng xếp hạng |

---

## 3. Hướng Dẫn Chạy Ứng Dụng Android (`app/`)

1. Mở thư mục gốc `RunTrack` bằng **Android Studio** và chờ Gradle Sync hoàn tất.
2. Cấu hình **Google Maps API Key** (cho màn hình GPS Art & Tracking): mở file `local.properties` ở thư mục gốc (tham khảo `local.properties.example`) và thêm dòng:
   ```properties
   MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY
   ```
3. Kiểm tra địa chỉ máy chủ Backend tại `app/src/main/java/com/example/runtrack/network/ApiConfig.kt`:
   * Kết nối **Azure VPS (mặc định):** `BASE_URL = "http://20.6.128.94:8000"`
   * Kết nối **Backend chạy Local trên máy tính (Android Emulator):** đổi thành `BASE_URL = "http://10.0.2.2:8000"`
3. Chọn máy ảo Android (API 24+) hoặc thiết bị thật và nhấn **Run** (hoặc build APK debug qua lệnh `./gradlew assembleDebug`).
4. Đăng nhập bằng `runner1@runtrack.app` / `Runner@123` để trải nghiệm:
   * Màn hình Home (Mục tiêu tuần, Phục hồi 24h, Thời tiết/AQI, BLE HRV).
   * Chi tiết mục tiêu tuần & popup hoạt động từng ngày.
   * Màn hình Tracking: nhấn bắt đầu/tạm dừng/dừng để lưu buổi chạy mới lên Backend (`POST /activities`).
   * Tìm kiếm bạn bè & tham gia/rời CLB (`SearchActivity`).
   * Đổi mật khẩu & đăng xuất thu hồi JWT (`ChangePasswordFragment`, `ProfileFragment`).

---

## 4. Kiểm Thử Tự Động & Xác Minh API (Postman / Swagger / Pytest / Vitest)

* **Swagger UI (trực quan):** Mở `http://localhost:8000/docs` (hoặc `http://20.6.128.94:8000/docs` trên VPS).
* **Postman Collection (28 API kèm test assertions):**
  * Import file `runtrack_backend/RunTrack_API.postman_collection.json` (hoặc bản sao trong `Others/postman/RunTrack_API.postman_collection.json`).
  * Chỉnh biến `base_url` thành `http://127.0.0.1:8000` (local) hoặc `http://20.6.128.94:8000` (VPS) rồi chạy **Collection Runner**.
* **Automated Test Suites:**
  ```powershell
  # 1. Backend (28/28 pytest — bao gồm đối chiếu 2 chiều Postman <-> OpenAPI)
  cd runtrack_backend
  .\.venv\Scripts\python.exe -m pytest -v

  # 2. Web-Admin (29/29 Vitest + TypeScript + Vite build + Trust Pyramid)
  cd ..\runtrack_web_admin
  npm test
  npm run build

  # 3. Android Debug Build
  cd ..
  .\gradlew.bat assembleDebug
  ```

---

## 5. Quy Trình Triển Khai Trên Cloud / Azure VPS

1. SSH vào máy chủ Ubuntu (Azure VPS) đã cài Docker & Docker Compose.
2. Clone repo và cấu hình biến môi trường thực tế:
   ```bash
   git clone https://github.com/NguyenTris/RunTrack.git
   cd RunTrack/runtrack_backend
   cp .env.example .env
   # Chỉnh VITE_API_BASE_URL=http://<IP_VPS>:8000 và SECRET_KEY trong .env
   docker compose up --build -d
   ```
3. Mở cổng Inbound trên Azure Network Security Group (NSG) / UFW:
   * Port `8000` (Backend FastAPI)
   * Port `3000` (Web-Admin Nginx)

---

## 6. Cấu Trúc Thư Mục Nộp Bài Tham Khảo

* `app/` — Mã nguồn Android Application (Front-end Mobile)
* `runtrack_web_admin/` — Mã nguồn Web-Admin (Front-end Web)
* `runtrack_backend/` — Mã nguồn Backend API, Docker Compose & bộ Test
* `Others/` — Chứa SQL schema + dữ liệu mẫu (`database/`), mẫu cấu hình (`configure/`), Postman Collection (`postman/`) và liên kết GitHub (`github_link.txt`).
* `METRICS_AND_ALGORITHMS.md` — Đặc tả thuật toán Recovery Score, HRV RMSSD, BLE Android 12+ và US AQI.
