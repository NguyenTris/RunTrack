# Bản Đồ Tính Năng & Bất Biến Hệ Thống (Feature Map & Invariants)

> AI Agent BẮT BUỘC đọc và cập nhật tài liệu này ở Bước 3 (Brainstorming), Bước 4 (Lập kế hoạch) và Bước 5 (Xác minh).

## 1. Kiến trúc Phân tầng & Ranh giới Thư mục (Profile: $Profile)
- **Python (python-iot-ai)**: edge/ không được import từ cloud/ hoặc dashboard/. Dùng common/ cho logic chia sẻ.
- **Web JS (web-js-ui)**: core/ chỉ chứa logic/thuật toán thuần túy, cấm gọi trực tiếp DOM (document, window) hoặc import từ ui/.
- **C++ (cpp-embedded)**: core/ chứa thuật toán độc lập phần cứng, cấm #include trực tiếp từ hal/ hoặc platform/.

## 2. Luồng Nghiệp Vụ Sống Còn (Critical User / System Flows)
| Mã luồng | Tên luồng tính năng | Điểm vào (Entry Point) | Kết quả kỳ vọng & Tiêu chí Runtime Verification |
| :--- | :--- | :--- | :--- |
| FLOW-01 | Khởi động hệ thống / Giao diện chính | main / index.html | Khởi chạy không lỗi, không có console.error hoặc Uncaught Exception. |

## 3. Các Bất Biến Kỹ Thuật (System Invariants - Không bao giờ được vi phạm)
1. **Zero Local Hardcode**: Không có đường dẫn tuyệt đối máy cá nhân (C:\Users\...) hoặc secret cứng trong mã nguồn.
2. **Local-Production Parity**: Mọi thư viện import đều phải được khóa phiên bản trong manifest (equirements.txt / package.json).
3. **Zero Code Debt**: Không hàm nào vượt quá 50 dòng hoặc độ phức tạp Cyclomatic > 10; không để lại code cũ bị comment-out.
