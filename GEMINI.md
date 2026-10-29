# Quy tắc phối hợp và quy trình phát triển (Development & Collaboration Workflow)

Tuân thủ nghiêm ngặt quy trình làm việc 7 bước sau đây cho mọi nhiệm vụ lập trình:

1. **Kiểm tra trạng thái nhánh (Branch Status Check)**:
   - Trước khi bắt đầu bất kỳ công việc nào, kiểm tra xem nhánh làm việc hiện tại đã cập nhật so với `master` (hoặc `origin/master`) hay chưa.
   - Nếu có các thay đổi hoặc commit mới, tóm tắt ngắn gọn các cập nhật đó cho người dùng trước khi tiến hành bước tiếp theo.

2. **Tiếp nhận mô tả nhiệm vụ (Task Requirements)**:
   - Tiếp nhận mô tả hoặc yêu cầu tính năng cụ thể từ người dùng.

3. **Brainstorming & Làm rõ yêu cầu (Brainstorming & Clarification)**:
   - Thảo luận ý tưởng giải pháp, phân tích kiến trúc, và chủ động đặt câu hỏi làm rõ các điểm chưa chắc chắn để hiểu cặn kẽ yêu cầu trước khi bắt tay vào làm.

4. **Lập kế hoạch & Chia nhỏ Mini-task (Planning & WBS)**:
   - Lập kế hoạch chi tiết, chia nhỏ công việc thành các mini-task độc lập, nguyên tử, tuần tự và khả thi.

5. **Chờ người dùng duyệt & Thực thi có kiểm thử (Approval & Test-Driven Execution)**:
   - Chỉ bắt đầu code sau khi người dùng đã duyệt kế hoạch.
   - Khi thực thi từng mini-task, **BẮT BUỘC** chạy kiểm thử tự động (Unit Test / Compile build) để xác nhận chức năng hoạt động đúng trước khi thông báo kết quả cho người dùng.

6. **Commit GitHub cho từng Mini-task (Atomic Commits)**:
   - Mỗi khi một mini-task hoàn thành và vượt qua kiểm thử, commit ngay lên GitHub.
   - Commit message: Viết bằng **tiếng Việt có dấu**, ngắn gọn, rõ ràng theo chuẩn Conventional Commits (ví dụ: `feat(...)`, `fix(...)`, `test(...)`).
   - Tuyệt đối không thêm `(mini task x.y)` vào commit message.

7. **Tạo Pull Request hoàn chỉnh (Pull Request Submission)**:
   - Sau khi hoàn thành toàn bộ các mini-task của nhiệm vụ, tạo Pull Request đưa code lên nhánh chính (`master`).
   - Tiêu đề (Title) và Mô tả (Description) của Pull Request **bắt buộc viết bằng tiếng Việt có dấu**, nêu rõ tổng quan, chi tiết các thay đổi và kết quả kiểm thử.
   - Gửi đường dẫn (link) PR trên GitHub cho người dùng kiểm tra và review.
