# FEATURE_MAP.md — RunTrack Web-Admin

## 1. Kiến trúc & Ranh giới Thư mục (Trust Pyramid: `web-js-ui`)
- `src/core/`: Logic thuần & HTTP client (`types.ts`, `apiClient.ts`, `formatters.ts`). **CẤM** dùng `window`, `document`, `localStorage` và **CẤM** import từ `src/ui/`.
- `src/ui/`: Giao diện React + Chart.js (Dark Athletic `#0B0F14` + Neon Green `#39FF14`), nhận dữ liệu từ `src/core/`.

## 2. Luồng Tính Năng Bắt Buộc (Runtime UI Verification)
1. **Đăng nhập Admin (`/login`):**
   - Nhập `admin@runtrack.app` / `Admin@123` → gọi `POST /auth/login` → xác thực quyền qua `GET /admin/stats` → vào Dashboard.
   - Nếu tài khoản thường (`role="user"`) → báo lỗi quyền quản trị (403), không cho vào Dashboard.
   - Nút Đăng xuất → gọi `POST /auth/logout` và xóa token phiên.
2. **Dashboard Thống kê (`StatsDashboard`):**
   - Hiển thị 5 KPI cards: Tổng người dùng, Tổng buổi chạy, Tổng quãng đường (km), Tổng calories, Tổng câu lạc bộ.
   - Biểu đồ 7 ngày gần nhất (Quãng đường km & Số buổi chạy theo ngày).
   - Bảng xếp hạng Top 5 Runners.
3. **CRUD Người dùng (`UsersPage`):**
   - Tìm kiếm, xem danh sách, tạo user mới (`POST /admin/users`), sửa thông tin/role/mục tiêu (`PUT /admin/users/{id}`), xóa user (`DELETE /admin/users/{id}`, hiển thị lỗi nếu tự xóa chính mình).
4. **CRUD Buổi chạy (`ActivitiesPage`):**
   - Lọc theo `user_id`, xem danh sách, tạo mới (`POST /admin/activities`), sửa (`PUT /admin/activities/{id}`), xóa (`DELETE /admin/activities/{id}`).
5. **CRUD Câu lạc bộ (`ClubsPage`):**
   - Tìm kiếm, xem danh sách kèm số thành viên, tạo mới (`POST /admin/clubs`), sửa (`PUT /admin/clubs/{id}`), xóa (`DELETE /admin/clubs/{id}`).
