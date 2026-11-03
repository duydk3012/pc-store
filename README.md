# PC Store Backend

## Chạy với dữ liệu mẫu

Sau khi cấu hình kết nối MySQL và JWT trong `.env`, chạy:

```powershell
.\gradlew.bat bootRun
```

Flyway tự tạo bảng và nạp **400 bản ghi cho 32 bảng** qua migration
`V2__seed_demo_data.sql`. Chỉ nạp một lần; khởi động lại không chèn trùng.
Migration đã gộp dành cho database mới; xem `readme/DATABASE_MIGRATIONS.md` về database đã có lịch sử Flyway.

Tài khoản demo:

| Username | Vai trò |
| --- | --- |
| `demo_admin` | Quản trị |
| `demo_manager` | Quản lý |
| `demo_warehouse` | Nhân viên kho |
| `demo_sales` | Nhân viên bán hàng |
| `demo_tech` | Kỹ thuật viên |
| `demo_customer01` … `demo_customer10` | Khách hàng |

Mật khẩu chung: **`Demo@123456`**.

Dữ liệu mẫu dùng ID từ 10001 và mã `DEMO-*`; sử dụng database phát triển
không trùng các ID/mã này. Không cần import SQL hay bật profile riêng.
