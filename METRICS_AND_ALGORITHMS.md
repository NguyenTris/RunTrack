# ĐẶC TẢ THUẬT TOÁN, CÔNG THỨC & CẤU HÌNH HỆ THỐNG RUNTRACK

Tài liệu này mô tả chi tiết các thuật toán tính toán sinh trắc học, công thức đánh giá thời tiết, và cấu hình phân quyền Bluetooth cho hệ điều hành Android nhằm phục vụ tính năng màn hình chính (Dashboard / Home).

---

## 1. Thuật toán Tính Tỷ Lệ Phục Hồi (Recovery Score & Readiness)

### 1.1. Cơ sở sinh lý học
Sau mỗi buổi chạy, cơ thể cần thời gian để tái tạo glycogen trong cơ bắp, phục hồi vi tổn thương sợi cơ và đưa hệ thần kinh tự chủ về trạng thái cân bằng. Trong hệ thống RunTrack, chu kỳ phục hồi sinh lý chuẩn được định mức là **24 giờ** (tương đương 100% hồi phục).

### 1.2. Công thức tính toán
- Gọi $T_{\text{last}}$ là thời điểm kết thúc buổi chạy gần nhất.
- Gọi $T_{\text{now}}$ là thời điểm hiện tại.
- Thời gian đã trôi qua kể từ buổi chạy cuối:
  $$\Delta t = \frac{T_{\text{now}} - T_{\text{last}}}{3600} \quad (\text{đơn vị: giờ})$$
- Tỷ lệ phục hồi ($R_{\%}$):
  $$R_{\%} = \min\left(100, \text{round}\left(\frac{\Delta t}{24.0} \times 100\right)\right)$$
- Số giờ còn lại để đạt 100% phục hồi ($t_{\text{rem}}$):
  $$t_{\text{rem}} = \max(0.0, 24.0 - \Delta t) \quad (\text{đơn vị: giờ})$$

### 1.3. Phân cấp màu sắc & Trạng thái Readiness
| Tỷ lệ phục hồi ($R_{\%}$) | Nhãn hiển thị | Màu sắc giao diện | Mã màu Hex | Ý nghĩa thể thao |
| :--- | :--- | :--- | :--- | :--- |
| **> 85%** | **SẴN SÀNG** | Xanh Neon (`@color/neon_green`) | `#39FF14` | Cơ thể đã hồi phục hoàn toàn, sẵn sàng cho các bài tập nặng (Interval, Tempo, Long Run). |
| **> 60% và $\le$ 85%** | **TRUNG BÌNH** | Vàng cảnh báo (`@color/accent_yellow`) | `#FFB300` / `#FFCC00` | Cơ thể đã hồi phục một phần, phù hợp cho bài chạy nhẹ (Easy Run, Recovery Run). |
| **$\le$ 60%** | **CẦN NGHỈ** | Đỏ cảnh báo (`@color/hr_zone_5`) | `#FF3300` | Cơ thể còn mệt mỏi, tích tụ axit lactic hoặc vi tổn thương, khuyến nghị nghỉ ngơi hoặc đi bộ nhẹ. |

---

## 2. Chuẩn Y Khoa & Đánh Giá Chỉ Số HRV (Heart Rate Variability)

### 2.1. Nguồn dữ liệu & Giao thức BLE (Bluetooth Low Energy)
- Thiết bị ngoại vi (Smartwatch / Đai đo nhịp tim) gửi dữ liệu qua Bluetooth GATT:
  - **Service UUID:** `0000180D-0000-1000-8000-00805F9B34FB` (Heart Rate Service)
  - **Characteristic UUID:** `00002A37-0000-1000-8000-00805F9B34FB` (Heart Rate Measurement)
- Gói tin chứa các khoảng $\text{RR-Interval}$ (khoảng cách giữa hai đỉnh R liên tiếp của phức bộ QRS trên điện tâm đồ).
- Đơn vị thô trong BLE là $\frac{1}{1024}$ giây. Chuyển đổi sang mili-giây:
  $$\text{RR}_i (\text{ms}) = \frac{\text{RR}_{\text{raw}} \times 1000}{1024}$$

### 2.2. Công thức RMSSD (Chuẩn vàng y học thể thao)
RMSSD (Root Mean Square of Successive Differences) phản ánh độ biến thiên giữa các nhịp tim liên tiếp, chịu sự chi phối trực tiếp của dây thần kinh phế vị (hệ thần kinh phó giao cảm - Parasympathetic):
$$\text{RMSSD} = \sqrt{\frac{1}{N - 1} \sum_{i=1}^{N-1} (\text{RR}_{i+1} - \text{RR}_i)^2}$$

### 2.3. Thang đánh giá trạng thái HRV nghỉ (Resting HRV)
| Chỉ số HRV (RMSSD) | Phân loại | Nhãn hiển thị | Đánh giá & Khuyến nghị |
| :--- | :--- | :--- | :--- |
| **$\ge$ 55 ms** | **Tốt** | `XX ms • Tốt` | Hệ thần kinh phó giao cảm hoạt động mạnh. Hệ tim mạch và thần kinh tự chủ ở trạng thái sung mãn nhất. |
| **40 ms - 54 ms** | **Bình thường** | `XX ms • Bình thường` | Hệ thần kinh tự chủ ở trạng thái cân bằng sinh lý tiêu chuẩn. Thích hợp duy trì khối lượng luyện tập thông thường. |
| **< 40 ms** | **Thấp** | `XX ms • Thấp` | Hệ thần kinh giao cảm (Sympathetic) chiếm ưu thế, phản ánh tình trạng căng thẳng (stress), thiếu ngủ hoặc cơ thể chưa hồi phục sau tải tập nặng. |

---

## 3. Khai Báo Quyền Bluetooth Cho Android (Android 11- & Android 12+)

Android 12 (API 31) đã thay đổi hoàn toàn cơ chế phân quyền Bluetooth, tách biệt quyền quét/kết nối Bluetooth khỏi quyền truy cập vị trí địa lý.

### 3.1. Khai báo trong `AndroidManifest.xml`

```xml
<!-- ======================================================== -->
<!-- QUYỀN CHO ANDROID 11 TRỞ XUỐNG (API <= 30)                -->
<!-- Trên Android 6.0 - 11.0, quét BLE bắt buộc quyền Location -->
<!-- ======================================================== -->
<uses-permission
    android:name="android.permission.BLUETOOTH"
    android:maxSdkVersion="30" />
<uses-permission
    android:name="android.permission.BLUETOOTH_ADMIN"
    android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- ======================================================== -->
<!-- QUYỀN MỚI CHO ANDROID 12 TRỞ LÊN (API 31+)                -->
<!-- Không còn phụ thuộc vào Location nếu có neverForLocation  -->
<!-- ======================================================== -->
<uses-permission
    android:name="android.permission.BLUETOOTH_SCAN"
    android:usesPermissionFlags="neverForLocation"
    tools:targetApi="s" />
<uses-permission
    android:name="android.permission.BLUETOOTH_CONNECT"
    tools:targetApi="s" />
```

### 3.2. Yêu cầu Runtime Permission (Kotlin)
- **Trên Android 12+ (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S):**
  Yêu cầu: `Manifest.permission.BLUETOOTH_SCAN` và `Manifest.permission.BLUETOOTH_CONNECT`.
- **Trên Android 11 trở xuống:**
  Yêu cầu: `Manifest.permission.ACCESS_FINE_LOCATION` (để quét thiết bị BLE).

---

## 4. Kiến Trúc & Logic Gọi Dữ Liệu Thời Tiết (Weather & AQI)

### 4.1. Sơ đồ luồng dữ liệu kép (Fallback Strategy)

```
[Android App] 
     │
     ├── (1) Ưu tiên gọi Backend: http://20.6.128.94:8000/weather?lat=...&lon=...
     │         (Hoặc cấu hình linh hoạt trong ApiConfig.kt)
     │         │
     │         ├── Thành công (HTTP 200) ──> Parse kết quả & cập nhật UI
     │         │
     │         └── Thất bại (Timeout / Server chưa bật / Mất kết nối)
     │                 │
     │                 ▼
     └── (2) Tự động kích hoạt Fallback: Gọi trực tiếp OpenWeatherMap API
               ├── Weather API: /data/2.5/weather
               └── Air Pollution API: /data/2.5/air_pollution (Tính AQI từ PM2.5)
                       │
                       └──> Cập nhật UI thời gian thực
```

### 4.2. Cấu hình địa chỉ Server Backend
- **Azure VPS Public IP:** `http://20.6.128.94:8000` (Primary NIC Public IP).
- **Local Emulator:** `http://10.0.2.2:8000` (khi chạy máy ảo Android Studio).
- **Cấu hình tập trung tại:** `com.example.runtrack.network.ApiConfig.BASE_URL`.
- **Cơ chế Timeout ngắn:** Đặt kết nối `connectTimeout = 3000ms`, `readTimeout = 3000ms`. Nếu server Azure chưa bật hoặc đang bảo trì, ứng dụng sẽ lập tức kích hoạt Fallback trực tiếp sang OpenWeatherMap để trải nghiệm người dùng luôn thông suốt.

### 4.3. Công thức tính US AQI từ nồng độ bụi mịn PM2.5 (Chuẩn EPA)
$$\text{AQI} = \frac{I_{\text{high}} - I_{\text{low}}}{C_{\text{high}} - C_{\text{low}}} (C - C_{\text{low}}) + I_{\text{low}}$$
Trong đó $C$ là nồng độ $\text{PM2.5}$ ($\mu g/m^3$) lấy từ OpenWeatherMap Air Pollution API. Kết quả được ánh xạ tương thích với thang đo trong ảnh mẫu (ví dụ: $\text{AQI 32}$ tương ứng với mức chất lượng không khí trong lành).

