-- PC Store: demo data. Requires V1__init_schema.sql.
-- Flyway loads this file once, after the initial schema.

-- ============================================================================
-- DEMO DATA: 400 records across 32 tables; account_challenges starts empty
-- ============================================================================

-- Uses fixed IDs >= 10001. Intended for a fresh development database.
-- All passwords: Demo@123456 (BCrypt). All sample refresh tokens are revoked.
-- No TRUNCATE, DELETE or disabling of foreign-key checks.

-- ---------------------------------------------------------------------------
-- 01. IDENTITY & AUTHENTICATION
-- ---------------------------------------------------------------------------

-- users: 20 records
INSERT INTO users (id, username, password_hash, full_name, email, phone_number, address, citizen_identification_card, role, status, token_version, is_deleted, created_by, created_date, last_modified_by, last_modified_date) VALUES
    (10001, 'demo_admin', '$2a$10$c67h/iZZTnuooeoeT947x.Ghrrn2bFCQqbeNOlpIdnVgRd0/YeHyq', 'Quản trị viên Demo', 'demo_admin@example.com', '0901000001', '1 Đường Demo, Hà Nội', '001099000001', 0, 1, 0, 0, 10001, '2026-07-01 08:00:00', 10001, '2026-07-01 08:00:00'),
    (10002, 'demo_manager', '$2a$10$c67h/iZZTnuooeoeT947x.Ghrrn2bFCQqbeNOlpIdnVgRd0/YeHyq', 'Quản lý Demo', 'demo_manager@example.com', '0901000002', '2 Đường Demo, Hà Nội', '001099000002', 1, 1, 0, 0, 10001, '2026-07-01 08:00:00', 10001, '2026-07-01 08:00:00'),
    (10003, 'demo_warehouse', '$2a$10$c67h/iZZTnuooeoeT947x.Ghrrn2bFCQqbeNOlpIdnVgRd0/YeHyq', 'Nhân viên kho Demo 01', 'demo_warehouse@example.com', '0901000003', '3 Đường Demo, Hà Nội', '001099000003', 3, 1, 0, 0, 10001, '2026-07-01 08:00:00', 10001, '2026-07-01 08:00:00'),
    (10004, 'demo_warehouse02', '$2a$10$c67h/iZZTnuooeoeT947x.Ghrrn2bFCQqbeNOlpIdnVgRd0/YeHyq', 'Nhân viên kho Demo 02', 'demo_warehouse02@example.com', '0901000004', '4 Đường Demo, Hà Nội', '001099000004', 3, 1, 0, 0, 10001, '2026-07-01 08:00:00', 10001, '2026-07-01 08:00:00'),
    (10005, 'demo_sales', '$2a$10$c67h/iZZTnuooeoeT947x.Ghrrn2bFCQqbeNOlpIdnVgRd0/YeHyq', 'Nhân viên bán hàng Demo 01', 'demo_sales@example.com', '0901000005', '5 Đường Demo, Hà Nội', '001099000005', 4, 1, 0, 0, 10001, '2026-07-01 08:00:00', 10001, '2026-07-01 08:00:00'),
    (10006, 'demo_sales02', '$2a$10$c67h/iZZTnuooeoeT947x.Ghrrn2bFCQqbeNOlpIdnVgRd0/YeHyq', 'Nhân viên bán hàng Demo 02', 'demo_sales02@example.com', '0901000006', '6 Đường Demo, Hà Nội', '001099000006', 4, 1, 0, 0, 10001, '2026-07-01 08:00:00', 10001, '2026-07-01 08:00:00'),
    (10007, 'demo_tech', '$2a$10$c67h/iZZTnuooeoeT947x.Ghrrn2bFCQqbeNOlpIdnVgRd0/YeHyq', 'Kỹ thuật viên Demo 01', 'demo_tech@example.com', '0901000007', '7 Đường Demo, Hà Nội', '001099000007', 5, 1, 0, 0, 10001, '2026-07-01 08:00:00', 10001, '2026-07-01 08:00:00'),
    (10008, 'demo_tech02', '$2a$10$c67h/iZZTnuooeoeT947x.Ghrrn2bFCQqbeNOlpIdnVgRd0/YeHyq', 'Kỹ thuật viên Demo 02', 'demo_tech02@example.com', '0901000008', '8 Đường Demo, Hà Nội', '001099000008', 5, 1, 0, 0, 10001, '2026-07-01 08:00:00', 10001, '2026-07-01 08:00:00'),
    (10009, 'demo_manager02', '$2a$10$c67h/iZZTnuooeoeT947x.Ghrrn2bFCQqbeNOlpIdnVgRd0/YeHyq', 'Quản lý Demo 02', 'demo_manager02@example.com', '0901000009', '9 Đường Demo, Hà Nội', '001099000009', 1, 1, 0, 0, 10001, '2026-07-01 08:00:00', 10001, '2026-07-01 08:00:00'),
    (10010, 'demo_tech03', '$2a$10$c67h/iZZTnuooeoeT947x.Ghrrn2bFCQqbeNOlpIdnVgRd0/YeHyq', 'Kỹ thuật viên Demo 03', 'demo_tech03@example.com', '0901000010', '10 Đường Demo, Hà Nội', '001099000010', 5, 1, 0, 0, 10001, '2026-07-01 08:00:00', 10001, '2026-07-01 08:00:00'),
    (10011, 'demo_customer01', '$2a$10$c67h/iZZTnuooeoeT947x.Ghrrn2bFCQqbeNOlpIdnVgRd0/YeHyq', 'Nguyễn Minh An', 'demo_customer01@example.com', '0901000011', '11 Đường Demo, Hà Nội', '001099000011', 2, 1, 0, 0, 10001, '2026-07-01 08:00:00', 10001, '2026-07-01 08:00:00'),
    (10012, 'demo_customer02', '$2a$10$c67h/iZZTnuooeoeT947x.Ghrrn2bFCQqbeNOlpIdnVgRd0/YeHyq', 'Trần Thanh Bình', 'demo_customer02@example.com', '0901000012', '12 Đường Demo, Hà Nội', '001099000012', 2, 1, 0, 0, 10001, '2026-07-01 08:00:00', 10001, '2026-07-01 08:00:00'),
    (10013, 'demo_customer03', '$2a$10$c67h/iZZTnuooeoeT947x.Ghrrn2bFCQqbeNOlpIdnVgRd0/YeHyq', 'Lê Hoàng Chi', 'demo_customer03@example.com', '0901000013', '13 Đường Demo, Hà Nội', '001099000013', 2, 1, 0, 0, 10001, '2026-07-01 08:00:00', 10001, '2026-07-01 08:00:00'),
    (10014, 'demo_customer04', '$2a$10$c67h/iZZTnuooeoeT947x.Ghrrn2bFCQqbeNOlpIdnVgRd0/YeHyq', 'Phạm Hải Dương', 'demo_customer04@example.com', '0901000014', '14 Đường Demo, Hà Nội', '001099000014', 2, 1, 0, 0, 10001, '2026-07-01 08:00:00', 10001, '2026-07-01 08:00:00'),
    (10015, 'demo_customer05', '$2a$10$c67h/iZZTnuooeoeT947x.Ghrrn2bFCQqbeNOlpIdnVgRd0/YeHyq', 'Hoàng Gia Hân', 'demo_customer05@example.com', '0901000015', '15 Đường Demo, Hà Nội', '001099000015', 2, 1, 0, 0, 10001, '2026-07-01 08:00:00', 10001, '2026-07-01 08:00:00'),
    (10016, 'demo_customer06', '$2a$10$c67h/iZZTnuooeoeT947x.Ghrrn2bFCQqbeNOlpIdnVgRd0/YeHyq', 'Đặng Trung Hiếu', 'demo_customer06@example.com', '0901000016', '16 Đường Demo, Hà Nội', '001099000016', 2, 1, 0, 0, 10001, '2026-07-01 08:00:00', 10001, '2026-07-01 08:00:00'),
    (10017, 'demo_customer07', '$2a$10$c67h/iZZTnuooeoeT947x.Ghrrn2bFCQqbeNOlpIdnVgRd0/YeHyq', 'Vũ Ngọc Khánh', 'demo_customer07@example.com', '0901000017', '17 Đường Demo, Hà Nội', '001099000017', 2, 1, 0, 0, 10001, '2026-07-01 08:00:00', 10001, '2026-07-01 08:00:00'),
    (10018, 'demo_customer08', '$2a$10$c67h/iZZTnuooeoeT947x.Ghrrn2bFCQqbeNOlpIdnVgRd0/YeHyq', 'Bùi Mai Linh', 'demo_customer08@example.com', '0901000018', '18 Đường Demo, Hà Nội', '001099000018', 2, 1, 0, 0, 10001, '2026-07-01 08:00:00', 10001, '2026-07-01 08:00:00'),
    (10019, 'demo_customer09', '$2a$10$c67h/iZZTnuooeoeT947x.Ghrrn2bFCQqbeNOlpIdnVgRd0/YeHyq', 'Đỗ Quang Minh', 'demo_customer09@example.com', '0901000019', '19 Đường Demo, Hà Nội', '001099000019', 2, 1, 0, 0, 10001, '2026-07-01 08:00:00', 10001, '2026-07-01 08:00:00'),
    (10020, 'demo_customer10', '$2a$10$c67h/iZZTnuooeoeT947x.Ghrrn2bFCQqbeNOlpIdnVgRd0/YeHyq', 'Ngô Thu Trang', 'demo_customer10@example.com', '0901000020', '20 Đường Demo, Hà Nội', '001099000020', 2, 1, 0, 0, 10001, '2026-07-01 08:00:00', 10001, '2026-07-01 08:00:00');

-- refresh_tokens: 10 records
INSERT INTO refresh_tokens (id, user_id, token_hash, expires_at, revoked, created_date) VALUES
    (10001, 10011, '6886636881eac826ef35d443a11e6adeb1e38a44379d29474778cdb4bef3d529', '2026-09-08 08:00:00', 1, '2026-09-01 08:00:00'),
    (10002, 10012, '53388a5472558ed4442a9e1903d020c1f58644e165c19e673c812a19d8c4deb7', '2026-09-08 08:00:00', 1, '2026-09-01 08:00:00'),
    (10003, 10013, 'fb03e169b0f8b95108906ff66454bf3e9c945856cb1bc26088a74eb67dabc05b', '2026-09-08 08:00:00', 1, '2026-09-01 08:00:00'),
    (10004, 10014, 'c3add401ed83bf92fb4935d9992e6eae729f1659b25558f93470aa6a72f9a1ce', '2026-09-08 08:00:00', 1, '2026-09-01 08:00:00'),
    (10005, 10015, '3ae47848232e9f380f49931d62a01c65479046c0b751fa644bacd3ab77e88448', '2026-09-08 08:00:00', 1, '2026-09-01 08:00:00'),
    (10006, 10016, '5890e086fc212e21112cb15705879d4a14cfc07959b5f05ca20a454ea5ef73ac', '2026-09-08 08:00:00', 1, '2026-09-01 08:00:00'),
    (10007, 10017, 'cec21ce379296c96970ec1e8141b519734c6ec36a24f12152b5abb1d2b579373', '2026-09-08 08:00:00', 1, '2026-09-01 08:00:00'),
    (10008, 10018, 'f6e5c10d24f29249df89baac7cc8960a4548e5d073259c8fed37c4bb6144afb1', '2026-09-08 08:00:00', 1, '2026-09-01 08:00:00'),
    (10009, 10019, '347c002793683658c2018dd37a16c0574797beaa826b78265005ae928bd7807c', '2026-09-08 08:00:00', 1, '2026-09-01 08:00:00'),
    (10010, 10020, '8d5d4337af7073137efd934b9f41c17e92111f47901e1ee60eddbd72ac57224b', '2026-09-08 08:00:00', 1, '2026-09-01 08:00:00');

-- ---------------------------------------------------------------------------
-- 02. SUPPLIERS
-- ---------------------------------------------------------------------------

-- suppliers: 10 records
INSERT INTO suppliers (id, code, name, contact_person, email, phone_number, address, tax_code, status, is_deleted, created_by, created_date, last_modified_by, last_modified_date) VALUES
    (10001, 'DEMO-NCC-001', 'Nhà cung cấp máy tính Demo 01', 'Đại diện Demo 01', 'demo.ncc01@example.com', '0912000001', '10 Đường Kho Vận Demo, Hà Nội', 'DEMO-MST-001', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10002, 'DEMO-NCC-002', 'Nhà cung cấp máy tính Demo 02', 'Đại diện Demo 02', 'demo.ncc02@example.com', '0912000002', '20 Đường Kho Vận Demo, Hà Nội', 'DEMO-MST-002', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10003, 'DEMO-NCC-003', 'Nhà cung cấp máy tính Demo 03', 'Đại diện Demo 03', 'demo.ncc03@example.com', '0912000003', '30 Đường Kho Vận Demo, Hà Nội', 'DEMO-MST-003', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10004, 'DEMO-NCC-004', 'Nhà cung cấp máy tính Demo 04', 'Đại diện Demo 04', 'demo.ncc04@example.com', '0912000004', '40 Đường Kho Vận Demo, Hà Nội', 'DEMO-MST-004', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10005, 'DEMO-NCC-005', 'Nhà cung cấp máy tính Demo 05', 'Đại diện Demo 05', 'demo.ncc05@example.com', '0912000005', '50 Đường Kho Vận Demo, Hà Nội', 'DEMO-MST-005', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10006, 'DEMO-NCC-006', 'Nhà cung cấp máy tính Demo 06', 'Đại diện Demo 06', 'demo.ncc06@example.com', '0912000006', '60 Đường Kho Vận Demo, Hà Nội', 'DEMO-MST-006', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10007, 'DEMO-NCC-007', 'Nhà cung cấp máy tính Demo 07', 'Đại diện Demo 07', 'demo.ncc07@example.com', '0912000007', '70 Đường Kho Vận Demo, Hà Nội', 'DEMO-MST-007', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10008, 'DEMO-NCC-008', 'Nhà cung cấp máy tính Demo 08', 'Đại diện Demo 08', 'demo.ncc08@example.com', '0912000008', '80 Đường Kho Vận Demo, Hà Nội', 'DEMO-MST-008', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10009, 'DEMO-NCC-009', 'Nhà cung cấp máy tính Demo 09', 'Đại diện Demo 09', 'demo.ncc09@example.com', '0912000009', '90 Đường Kho Vận Demo, Hà Nội', 'DEMO-MST-009', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10010, 'DEMO-NCC-010', 'Nhà cung cấp máy tính Demo 10', 'Đại diện Demo 10', 'demo.ncc10@example.com', '0912000010', '100 Đường Kho Vận Demo, Hà Nội', 'DEMO-MST-010', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00');

-- ---------------------------------------------------------------------------
-- 03. PRODUCT CATALOG
-- ---------------------------------------------------------------------------

-- categories: 10 records
INSERT INTO categories (id, name, slug, description, status, parent_id, is_deleted, created_by, created_date, last_modified_by, last_modified_date) VALUES
    (10001, 'Linh kiện', 'demo-linh-kien', 'Danh mục mẫu: Linh kiện', 1, NULL, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10002, 'CPU', 'demo-cpu', 'Danh mục mẫu: CPU', 1, 10001, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10003, 'Mainboard', 'demo-mainboard', 'Danh mục mẫu: Mainboard', 1, 10001, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10004, 'RAM', 'demo-ram', 'Danh mục mẫu: RAM', 1, 10001, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10005, 'SSD', 'demo-ssd', 'Danh mục mẫu: SSD', 1, 10001, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10006, 'Card đồ họa', 'demo-card-do-hoa', 'Danh mục mẫu: Card đồ họa', 1, 10001, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10007, 'Phụ kiện', 'demo-phu-kien', 'Danh mục mẫu: Phụ kiện', 1, NULL, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10008, 'Laptop', 'demo-laptop', 'Danh mục mẫu: Laptop', 1, NULL, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10009, 'Bộ nâng cấp PC', 'demo-bo-nang-cap-pc', 'Danh mục mẫu: Bộ nâng cấp PC', 1, NULL, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10010, 'Màn hình', 'demo-man-hinh', 'Danh mục mẫu: Màn hình', 1, NULL, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00');

-- brands: 10 records
INSERT INTO brands (id, name, slug, description, status, is_deleted, created_by, created_date, last_modified_by, last_modified_date) VALUES
    (10001, 'Intel', 'demo-intel', 'Thương hiệu Intel trong dữ liệu demo', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10002, 'MSI', 'demo-msi', 'Thương hiệu MSI trong dữ liệu demo', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10003, 'Kingston', 'demo-kingston', 'Thương hiệu Kingston trong dữ liệu demo', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10004, 'Samsung', 'demo-samsung', 'Thương hiệu Samsung trong dữ liệu demo', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10005, 'ASUS', 'demo-asus', 'Thương hiệu ASUS trong dữ liệu demo', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10006, 'Corsair', 'demo-corsair', 'Thương hiệu Corsair trong dữ liệu demo', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10007, 'Logitech', 'demo-logitech', 'Thương hiệu Logitech trong dữ liệu demo', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10008, 'Lenovo', 'demo-lenovo', 'Thương hiệu Lenovo trong dữ liệu demo', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10009, 'Dell', 'demo-dell', 'Thương hiệu Dell trong dữ liệu demo', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10010, 'HP', 'demo-hp', 'Thương hiệu HP trong dữ liệu demo', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00');

-- products: 20 records
INSERT INTO products (id, name, code, description, specification, product_type, status, slug, image_url, brand_id, is_deleted, created_by, created_date, last_modified_by, last_modified_date, price) VALUES
    (10001, 'CPU Intel Core i5 Demo', 'DEMO-SP-001', 'Sản phẩm minh họa, giá và cấu hình dùng để kiểm thử.', 'CPU; socket LGA1700', 0, 2, 'demo-cpu-intel-i5', 'https://example.com/demo/products/cpu-intel-i5.jpg', 10001, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00', 4800000),
    (10002, 'Mainboard MSI B760 Demo', 'DEMO-SP-002', 'Sản phẩm minh họa, giá và cấu hình dùng để kiểm thử.', 'Mainboard; socket LGA1700; DDR4', 0, 2, 'demo-mainboard-msi-b760', 'https://example.com/demo/products/mainboard-msi-b760.jpg', 10002, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00', 3000000),
    (10003, 'RAM Kingston 16GB DDR4 Demo', 'DEMO-SP-003', 'Sản phẩm minh họa, giá và cấu hình dùng để kiểm thử.', 'RAM 16GB; DDR4; 3200MHz', 0, 2, 'demo-ram-kingston-16gb', 'https://example.com/demo/products/ram-kingston-16gb.jpg', 10003, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00', 720000),
    (10004, 'SSD Samsung 1TB Demo', 'DEMO-SP-004', 'Sản phẩm minh họa, giá và cấu hình dùng để kiểm thử.', 'SSD NVMe; dung lượng 1TB', 0, 2, 'demo-ssd-samsung-1tb', 'https://example.com/demo/products/ssd-samsung-1tb.jpg', 10004, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00', 1680000),
    (10005, 'GPU ASUS RTX 4060 Demo', 'DEMO-SP-005', 'Sản phẩm minh họa, giá và cấu hình dùng để kiểm thử.', 'GPU; bộ nhớ 8GB', 0, 2, 'demo-gpu-asus-rtx4060', 'https://example.com/demo/products/gpu-asus-rtx4060.jpg', 10005, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00', 9600000),
    (10006, 'Nguồn Corsair 650W Demo', 'DEMO-SP-006', 'Sản phẩm minh họa, giá và cấu hình dùng để kiểm thử.', 'Nguồn 650W; chuẩn ATX', 0, 2, 'demo-psu-corsair-650w', 'https://example.com/demo/products/psu-corsair-650w.jpg', 10006, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00', 1440000),
    (10007, 'Case Corsair ATX Demo', 'DEMO-SP-007', 'Sản phẩm minh họa, giá và cấu hình dùng để kiểm thử.', 'Vỏ case ATX', 0, 2, 'demo-case-corsair-atx', 'https://example.com/demo/products/case-corsair-atx.jpg', 10006, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00', 1200000),
    (10008, 'Tản nhiệt Corsair Demo', 'DEMO-SP-008', 'Sản phẩm minh họa, giá và cấu hình dùng để kiểm thử.', 'Tản nhiệt tương thích LGA1700', 0, 2, 'demo-cooler-corsair', 'https://example.com/demo/products/cooler-corsair.jpg', 10006, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00', 840000),
    (10009, 'Màn hình Dell 24 inch Demo', 'DEMO-SP-009', 'Sản phẩm minh họa, giá và cấu hình dùng để kiểm thử.', 'Màn hình IPS; 24 inch', 0, 2, 'demo-monitor-dell-24', 'https://example.com/demo/products/monitor-dell-24.jpg', 10009, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00', 3000000),
    (10010, 'Bàn phím Logitech Demo', 'DEMO-SP-010', 'Sản phẩm minh họa, giá và cấu hình dùng để kiểm thử.', 'Bàn phím USB', 0, 2, 'demo-keyboard-logitech', 'https://example.com/demo/products/keyboard-logitech.jpg', 10007, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00', 480000),
    (10011, 'Laptop ASUS Demo 01', 'DEMO-SP-011', 'Sản phẩm minh họa, giá và cấu hình dùng để kiểm thử.', 'Laptop; RAM 16GB; SSD 512GB', 0, 2, 'demo-laptop-01', 'https://example.com/demo/products/laptop-01.jpg', 10005, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00', 15600000),
    (10012, 'Laptop MSI Demo 02', 'DEMO-SP-012', 'Sản phẩm minh họa, giá và cấu hình dùng để kiểm thử.', 'Laptop; RAM 16GB; SSD 512GB', 0, 2, 'demo-laptop-02', 'https://example.com/demo/products/laptop-02.jpg', 10002, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00', 16800000),
    (10013, 'Laptop Lenovo Demo 03', 'DEMO-SP-013', 'Sản phẩm minh họa, giá và cấu hình dùng để kiểm thử.', 'Laptop; RAM 16GB; SSD 512GB', 0, 2, 'demo-laptop-03', 'https://example.com/demo/products/laptop-03.jpg', 10008, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00', 18000000),
    (10014, 'Laptop Dell Demo 04', 'DEMO-SP-014', 'Sản phẩm minh họa, giá và cấu hình dùng để kiểm thử.', 'Laptop; RAM 16GB; SSD 512GB', 0, 2, 'demo-laptop-04', 'https://example.com/demo/products/laptop-04.jpg', 10009, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00', 19200000),
    (10015, 'Laptop HP Demo 05', 'DEMO-SP-015', 'Sản phẩm minh họa, giá và cấu hình dùng để kiểm thử.', 'Laptop; RAM 16GB; SSD 512GB', 0, 2, 'demo-laptop-05', 'https://example.com/demo/products/laptop-05.jpg', 10010, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00', 20400000),
    (10016, 'Bộ nâng cấp PC CPU và Main Demo 01', 'DEMO-SP-016', 'Sản phẩm minh họa, giá và cấu hình dùng để kiểm thử.', 'Bộ nâng cấp gồm CPU và mainboard; không phải máy hoàn chỉnh', 1, 2, 'demo-pc-upgrade-01', 'https://example.com/demo/products/pc-upgrade-01.jpg', 10002, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00', 7800000),
    (10017, 'Bộ nâng cấp PC CPU và Main Demo 02', 'DEMO-SP-017', 'Sản phẩm minh họa, giá và cấu hình dùng để kiểm thử.', 'Bộ nâng cấp gồm CPU và mainboard; không phải máy hoàn chỉnh', 1, 2, 'demo-pc-upgrade-02', 'https://example.com/demo/products/pc-upgrade-02.jpg', 10002, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00', 7800000),
    (10018, 'Bộ nâng cấp PC CPU và Main Demo 03', 'DEMO-SP-018', 'Sản phẩm minh họa, giá và cấu hình dùng để kiểm thử.', 'Bộ nâng cấp gồm CPU và mainboard; không phải máy hoàn chỉnh', 1, 2, 'demo-pc-upgrade-03', 'https://example.com/demo/products/pc-upgrade-03.jpg', 10002, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00', 7800000),
    (10019, 'Bộ nâng cấp PC CPU và Main Demo 04', 'DEMO-SP-019', 'Sản phẩm minh họa, giá và cấu hình dùng để kiểm thử.', 'Bộ nâng cấp gồm CPU và mainboard; không phải máy hoàn chỉnh', 1, 2, 'demo-pc-upgrade-04', 'https://example.com/demo/products/pc-upgrade-04.jpg', 10002, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00', 7800000),
    (10020, 'Bộ nâng cấp PC CPU và Main Demo 05', 'DEMO-SP-020', 'Sản phẩm minh họa, giá và cấu hình dùng để kiểm thử.', 'Bộ nâng cấp gồm CPU và mainboard; không phải máy hoàn chỉnh', 1, 2, 'demo-pc-upgrade-05', 'https://example.com/demo/products/pc-upgrade-05.jpg', 10002, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00', 7800000);

-- category_products: 20 records
INSERT INTO category_products (id, product_id, category_id, is_primary) VALUES
    (10001, 10001, 10002, 1),
    (10002, 10002, 10003, 1),
    (10003, 10003, 10004, 1),
    (10004, 10004, 10005, 1),
    (10005, 10005, 10006, 1),
    (10006, 10006, 10001, 1),
    (10007, 10007, 10001, 1),
    (10008, 10008, 10007, 1),
    (10009, 10009, 10010, 1),
    (10010, 10010, 10007, 1),
    (10011, 10011, 10008, 1),
    (10012, 10012, 10008, 1),
    (10013, 10013, 10008, 1),
    (10014, 10014, 10008, 1),
    (10015, 10015, 10008, 1),
    (10016, 10016, 10009, 1),
    (10017, 10017, 10009, 1),
    (10018, 10018, 10009, 1),
    (10019, 10019, 10009, 1),
    (10020, 10020, 10009, 1);

-- building_pc: 10 records
INSERT INTO building_pc (id, pc_id, product_id, quantity) VALUES
    (10001, 10016, 10001, 1),
    (10002, 10016, 10002, 1),
    (10003, 10017, 10001, 1),
    (10004, 10017, 10002, 1),
    (10005, 10018, 10001, 1),
    (10006, 10018, 10002, 1),
    (10007, 10019, 10001, 1),
    (10008, 10019, 10002, 1),
    (10009, 10020, 10001, 1),
    (10010, 10020, 10002, 1);


-- ---------------------------------------------------------------------------
-- 04. PURCHASING & INVENTORY
-- ---------------------------------------------------------------------------

-- purchase_orders: 10 records
INSERT INTO purchase_orders (id, code, name, supplier_id, order_date, expected_date, status, description, total_amount, is_deleted, created_by, created_date, last_modified_by, last_modified_date) VALUES
    (10001, 'DEMO-PO-001', 'Đơn nhập linh kiện Demo 01', 10001, '2026-08-01 08:00:00', '2026-08-05 08:00:00', 3, 'Đơn mẫu: đã nhận / nhận một phần / nháp / chờ duyệt theo trạng thái.', 6500000, 0, 10002, '2026-08-01 08:00:00', 10002, '2026-08-01 08:00:00'),
    (10002, 'DEMO-PO-002', 'Đơn nhập linh kiện Demo 02', 10002, '2026-08-02 08:00:00', '2026-08-06 08:00:00', 3, 'Đơn mẫu: đã nhận / nhận một phần / nháp / chờ duyệt theo trạng thái.', 2000000, 0, 10002, '2026-08-02 08:00:00', 10002, '2026-08-02 08:00:00'),
    (10003, 'DEMO-PO-003', 'Đơn nhập linh kiện Demo 03', 10003, '2026-08-03 08:00:00', '2026-08-07 08:00:00', 3, 'Đơn mẫu: đã nhận / nhận một phần / nháp / chờ duyệt theo trạng thái.', 2000000, 0, 10002, '2026-08-03 08:00:00', 10002, '2026-08-03 08:00:00'),
    (10004, 'DEMO-PO-004', 'Đơn nhập linh kiện Demo 04', 10004, '2026-08-04 08:00:00', '2026-08-08 08:00:00', 3, 'Đơn mẫu: đã nhận / nhận một phần / nháp / chờ duyệt theo trạng thái.', 9200000, 0, 10002, '2026-08-04 08:00:00', 10002, '2026-08-04 08:00:00'),
    (10005, 'DEMO-PO-005', 'Đơn nhập linh kiện Demo 05', 10005, '2026-08-05 08:00:00', '2026-08-09 08:00:00', 3, 'Đơn mẫu: đã nhận / nhận một phần / nháp / chờ duyệt theo trạng thái.', 9200000, 0, 10002, '2026-08-05 08:00:00', 10002, '2026-08-05 08:00:00'),
    (10006, 'DEMO-PO-006', 'Đơn nhập linh kiện Demo 06', 10006, '2026-08-06 08:00:00', '2026-08-10 08:00:00', 3, 'Đơn mẫu: đã nhận / nhận một phần / nháp / chờ duyệt theo trạng thái.', 6500000, 0, 10002, '2026-08-06 08:00:00', 10002, '2026-08-06 08:00:00'),
    (10007, 'DEMO-PO-007', 'Đơn nhập linh kiện Demo 07', 10007, '2026-09-01 08:00:00', '2026-09-05 08:00:00', 2, 'Đơn mẫu: đã nhận / nhận một phần / nháp / chờ duyệt theo trạng thái.', 6000000, 0, 10002, '2026-09-01 08:00:00', 10002, '2026-09-01 08:00:00'),
    (10008, 'DEMO-PO-008', 'Đơn nhập linh kiện Demo 08', 10008, '2026-09-02 08:00:00', '2026-09-06 08:00:00', 2, 'Đơn mẫu: đã nhận / nhận một phần / nháp / chờ duyệt theo trạng thái.', 27600000, 0, 10002, '2026-09-02 08:00:00', 10002, '2026-09-02 08:00:00'),
    (10009, 'DEMO-PO-009', 'Đơn nhập linh kiện Demo 09', 10009, '2026-10-19 08:00:00', '2026-10-23 08:00:00', 0, 'Đơn mẫu: đã nhận / nhận một phần / nháp / chờ duyệt theo trạng thái.', 3400000, 0, 10002, '2026-10-19 08:00:00', 10002, '2026-10-19 08:00:00'),
    (10010, 'DEMO-PO-010', 'Đơn nhập linh kiện Demo 10', 10010, '2026-10-20 08:00:00', '2026-10-24 08:00:00', 1, 'Đơn mẫu: đã nhận / nhận một phần / nháp / chờ duyệt theo trạng thái.', 5800000, 0, 10002, '2026-10-20 08:00:00', 10002, '2026-10-20 08:00:00');

-- purchase_order_items: 20 records
INSERT INTO purchase_order_items (id, purchase_order_id, product_id, quantity_order, unit_cost, quantity_received, subtotal, created_by, created_date, last_modified_by, last_modified_date) VALUES
    (10001, 10001, 10001, 1, 4000000, 1, 4000000, 10002, '2026-08-01 08:00:00', 10002, '2026-08-01 08:00:00'),
    (10002, 10001, 10002, 1, 2500000, 1, 2500000, 10002, '2026-08-01 08:00:00', 10002, '2026-08-01 08:00:00'),
    (10003, 10002, 10003, 1, 600000, 1, 600000, 10002, '2026-08-02 08:00:00', 10002, '2026-08-02 08:00:00'),
    (10004, 10002, 10004, 1, 1400000, 1, 1400000, 10002, '2026-08-02 08:00:00', 10002, '2026-08-02 08:00:00'),
    (10005, 10003, 10003, 1, 600000, 1, 600000, 10002, '2026-08-03 08:00:00', 10002, '2026-08-03 08:00:00'),
    (10006, 10003, 10004, 1, 1400000, 1, 1400000, 10002, '2026-08-03 08:00:00', 10002, '2026-08-03 08:00:00'),
    (10007, 10004, 10005, 1, 8000000, 1, 8000000, 10002, '2026-08-04 08:00:00', 10002, '2026-08-04 08:00:00'),
    (10008, 10004, 10006, 1, 1200000, 1, 1200000, 10002, '2026-08-04 08:00:00', 10002, '2026-08-04 08:00:00'),
    (10009, 10005, 10005, 1, 8000000, 1, 8000000, 10002, '2026-08-05 08:00:00', 10002, '2026-08-05 08:00:00'),
    (10010, 10005, 10006, 1, 1200000, 1, 1200000, 10002, '2026-08-05 08:00:00', 10002, '2026-08-05 08:00:00'),
    (10011, 10006, 10001, 1, 4000000, 1, 4000000, 10002, '2026-08-06 08:00:00', 10002, '2026-08-06 08:00:00'),
    (10012, 10006, 10002, 1, 2500000, 1, 2500000, 10002, '2026-08-06 08:00:00', 10002, '2026-08-06 08:00:00'),
    (10013, 10007, 10003, 3, 600000, 2, 1800000, 10002, '2026-09-01 08:00:00', 10002, '2026-09-01 08:00:00'),
    (10014, 10007, 10004, 3, 1400000, 2, 4200000, 10002, '2026-09-01 08:00:00', 10002, '2026-09-01 08:00:00'),
    (10015, 10008, 10005, 3, 8000000, 2, 24000000, 10002, '2026-09-02 08:00:00', 10002, '2026-09-02 08:00:00'),
    (10016, 10008, 10006, 3, 1200000, 2, 3600000, 10002, '2026-09-02 08:00:00', 10002, '2026-09-02 08:00:00'),
    (10017, 10009, 10007, 2, 1000000, 0, 2000000, 10002, '2026-10-19 08:00:00', 10002, '2026-10-19 08:00:00'),
    (10018, 10009, 10008, 2, 700000, 0, 1400000, 10002, '2026-10-19 08:00:00', 10002, '2026-10-19 08:00:00'),
    (10019, 10010, 10009, 2, 2500000, 0, 5000000, 10002, '2026-10-20 08:00:00', 10002, '2026-10-20 08:00:00'),
    (10020, 10010, 10010, 2, 400000, 0, 800000, 10002, '2026-10-20 08:00:00', 10002, '2026-10-20 08:00:00');

-- goods_receipts: 10 records
INSERT INTO goods_receipts (id, code, name, purchase_order_id, received_at, received_by, status, created_by, created_date, last_modified_by, last_modified_date) VALUES
    (10001, 'DEMO-GR-001', 'Phiếu nhận hàng Demo 01', 10001, '2026-08-05 08:00:00', 10004, 1, 10004, '2026-08-05 08:00:00', 10004, '2026-08-05 08:00:00'),
    (10002, 'DEMO-GR-002', 'Phiếu nhận hàng Demo 02', 10002, '2026-08-06 08:00:00', 10003, 1, 10003, '2026-08-06 08:00:00', 10003, '2026-08-06 08:00:00'),
    (10003, 'DEMO-GR-003', 'Phiếu nhận hàng Demo 03', 10003, '2026-08-07 08:00:00', 10004, 1, 10004, '2026-08-07 08:00:00', 10004, '2026-08-07 08:00:00'),
    (10004, 'DEMO-GR-004', 'Phiếu nhận hàng Demo 04', 10004, '2026-08-08 08:00:00', 10003, 1, 10003, '2026-08-08 08:00:00', 10003, '2026-08-08 08:00:00'),
    (10005, 'DEMO-GR-005', 'Phiếu nhận hàng Demo 05', 10005, '2026-08-09 08:00:00', 10004, 1, 10004, '2026-08-09 08:00:00', 10004, '2026-08-09 08:00:00'),
    (10006, 'DEMO-GR-006', 'Phiếu nhận hàng Demo 06', 10006, '2026-08-10 08:00:00', 10003, 1, 10003, '2026-08-10 08:00:00', 10003, '2026-08-10 08:00:00'),
    (10007, 'DEMO-GR-007', 'Phiếu nhận hàng Demo 07', 10007, '2026-09-05 08:00:00', 10004, 1, 10004, '2026-09-05 08:00:00', 10004, '2026-09-05 08:00:00'),
    (10008, 'DEMO-GR-008', 'Phiếu nhận hàng Demo 08', 10007, '2026-09-06 08:00:00', 10003, 1, 10003, '2026-09-06 08:00:00', 10003, '2026-09-06 08:00:00'),
    (10009, 'DEMO-GR-009', 'Phiếu nhận hàng Demo 09', 10008, '2026-09-07 08:00:00', 10004, 1, 10004, '2026-09-07 08:00:00', 10004, '2026-09-07 08:00:00'),
    (10010, 'DEMO-GR-010', 'Phiếu nhận hàng Demo 10', 10008, '2026-09-08 08:00:00', 10003, 1, 10003, '2026-09-08 08:00:00', 10003, '2026-09-08 08:00:00');

-- receipt_items: 20 records
INSERT INTO receipt_items (id, goods_receipt_id, purchase_order_item_id, actual_unit_cost, quantity_received, quantity_accepted, quantity_defective, created_by, created_date, last_modified_by, last_modified_date) VALUES
    (10001, 10001, 10001, 4000000, 1, 1, 0, 10004, '2026-08-05 08:00:00', 10004, '2026-08-05 08:00:00'),
    (10002, 10001, 10002, 2500000, 1, 1, 0, 10004, '2026-08-05 08:00:00', 10004, '2026-08-05 08:00:00'),
    (10003, 10002, 10003, 600000, 1, 1, 0, 10003, '2026-08-06 08:00:00', 10003, '2026-08-06 08:00:00'),
    (10004, 10002, 10004, 1400000, 1, 1, 0, 10003, '2026-08-06 08:00:00', 10003, '2026-08-06 08:00:00'),
    (10005, 10003, 10005, 600000, 1, 1, 0, 10004, '2026-08-07 08:00:00', 10004, '2026-08-07 08:00:00'),
    (10006, 10003, 10006, 1400000, 1, 1, 0, 10004, '2026-08-07 08:00:00', 10004, '2026-08-07 08:00:00'),
    (10007, 10004, 10007, 8000000, 1, 1, 0, 10003, '2026-08-08 08:00:00', 10003, '2026-08-08 08:00:00'),
    (10008, 10004, 10008, 1200000, 1, 1, 0, 10003, '2026-08-08 08:00:00', 10003, '2026-08-08 08:00:00'),
    (10009, 10005, 10009, 8000000, 1, 1, 0, 10004, '2026-08-09 08:00:00', 10004, '2026-08-09 08:00:00'),
    (10010, 10005, 10010, 1200000, 1, 1, 0, 10004, '2026-08-09 08:00:00', 10004, '2026-08-09 08:00:00'),
    (10011, 10006, 10011, 4000000, 1, 1, 0, 10003, '2026-08-10 08:00:00', 10003, '2026-08-10 08:00:00'),
    (10012, 10006, 10012, 2500000, 1, 1, 0, 10003, '2026-08-10 08:00:00', 10003, '2026-08-10 08:00:00'),
    (10013, 10007, 10013, 600000, 1, 1, 0, 10004, '2026-09-05 08:00:00', 10004, '2026-09-05 08:00:00'),
    (10014, 10007, 10014, 1400000, 1, 1, 0, 10004, '2026-09-05 08:00:00', 10004, '2026-09-05 08:00:00'),
    (10015, 10008, 10013, 600000, 1, 1, 0, 10003, '2026-09-06 08:00:00', 10003, '2026-09-06 08:00:00'),
    (10016, 10008, 10014, 1400000, 1, 1, 0, 10003, '2026-09-06 08:00:00', 10003, '2026-09-06 08:00:00'),
    (10017, 10009, 10015, 8000000, 1, 1, 0, 10004, '2026-09-07 08:00:00', 10004, '2026-09-07 08:00:00'),
    (10018, 10009, 10016, 1200000, 1, 1, 0, 10004, '2026-09-07 08:00:00', 10004, '2026-09-07 08:00:00'),
    (10019, 10010, 10015, 8000000, 1, 1, 0, 10003, '2026-09-08 08:00:00', 10003, '2026-09-08 08:00:00'),
    (10020, 10010, 10016, 1200000, 1, 1, 0, 10003, '2026-09-08 08:00:00', 10003, '2026-09-08 08:00:00');

-- product_serials: 20 records
INSERT INTO product_serials (id, product_id, receipt_item_id, serial_number, status, created_by, created_date, last_modified_by, last_modified_date) VALUES
    (10001, 10001, 10001, 'DEMO-SN-00001', 5, 10004, '2026-08-05 08:00:00', 10004, '2026-08-05 08:00:00'),
    (10002, 10002, 10002, 'DEMO-SN-00002', 5, 10004, '2026-08-05 08:00:00', 10004, '2026-08-05 08:00:00'),
    (10003, 10003, 10003, 'DEMO-SN-00003', 5, 10003, '2026-08-06 08:00:00', 10003, '2026-08-06 08:00:00'),
    (10004, 10004, 10004, 'DEMO-SN-00004', 5, 10003, '2026-08-06 08:00:00', 10003, '2026-08-06 08:00:00'),
    (10005, 10003, 10005, 'DEMO-SN-00005', 5, 10004, '2026-08-07 08:00:00', 10004, '2026-08-07 08:00:00'),
    (10006, 10004, 10006, 'DEMO-SN-00006', 5, 10004, '2026-08-07 08:00:00', 10004, '2026-08-07 08:00:00'),
    (10007, 10005, 10007, 'DEMO-SN-00007', 5, 10003, '2026-08-08 08:00:00', 10003, '2026-08-08 08:00:00'),
    (10008, 10006, 10008, 'DEMO-SN-00008', 5, 10003, '2026-08-08 08:00:00', 10003, '2026-08-08 08:00:00'),
    (10009, 10005, 10009, 'DEMO-SN-00009', 5, 10004, '2026-08-09 08:00:00', 10004, '2026-08-09 08:00:00'),
    (10010, 10006, 10010, 'DEMO-SN-00010', 5, 10004, '2026-08-09 08:00:00', 10004, '2026-08-09 08:00:00'),
    (10011, 10001, 10011, 'DEMO-SN-00011', 8, 10003, '2026-08-10 08:00:00', 10003, '2026-08-10 08:00:00'),
    (10012, 10002, 10012, 'DEMO-SN-00012', 8, 10003, '2026-08-10 08:00:00', 10003, '2026-08-10 08:00:00'),
    (10013, 10003, 10013, 'DEMO-SN-00013', 8, 10004, '2026-09-05 08:00:00', 10004, '2026-09-05 08:00:00'),
    (10014, 10004, 10014, 'DEMO-SN-00014', 8, 10004, '2026-09-05 08:00:00', 10004, '2026-09-05 08:00:00'),
    (10015, 10003, 10015, 'DEMO-SN-00015', 8, 10003, '2026-09-06 08:00:00', 10003, '2026-09-06 08:00:00'),
    (10016, 10004, 10016, 'DEMO-SN-00016', 8, 10003, '2026-09-06 08:00:00', 10003, '2026-09-06 08:00:00'),
    (10017, 10005, 10017, 'DEMO-SN-00017', 8, 10004, '2026-09-07 08:00:00', 10004, '2026-09-07 08:00:00'),
    (10018, 10006, 10018, 'DEMO-SN-00018', 8, 10004, '2026-09-07 08:00:00', 10004, '2026-09-07 08:00:00'),
    (10019, 10005, 10019, 'DEMO-SN-00019', 8, 10003, '2026-09-08 08:00:00', 10003, '2026-09-08 08:00:00'),
    (10020, 10006, 10020, 'DEMO-SN-00020', 8, 10003, '2026-09-08 08:00:00', 10003, '2026-09-08 08:00:00');

-- purchase_returns: 10 records
INSERT INTO purchase_returns (id, code, name, supplier_id, reason, description, return_date, total_amount, status, created_by, created_date, last_modified_by, last_modified_date) VALUES
    (10001, 'DEMO-PR-001', 'Hoàn serial bảo hành Demo 01', 10001, 'Lỗi phát sinh sau bán, đã thay thế bảo hành cho khách.', 'Serial cũ đã được nhà cung cấp tiếp nhận.', '2026-10-04 08:00:00', 4000000, 2, 10002, '2026-10-04 08:00:00', 10002, '2026-10-04 08:00:00'),
    (10002, 'DEMO-PR-002', 'Hoàn serial bảo hành Demo 02', 10001, 'Lỗi phát sinh sau bán, đã thay thế bảo hành cho khách.', 'Serial cũ đã được nhà cung cấp tiếp nhận.', '2026-10-05 08:00:00', 2500000, 2, 10002, '2026-10-05 08:00:00', 10002, '2026-10-05 08:00:00'),
    (10003, 'DEMO-PR-003', 'Hoàn serial bảo hành Demo 03', 10002, 'Lỗi phát sinh sau bán, đã thay thế bảo hành cho khách.', 'Serial cũ đã được nhà cung cấp tiếp nhận.', '2026-10-06 08:00:00', 600000, 2, 10002, '2026-10-06 08:00:00', 10002, '2026-10-06 08:00:00'),
    (10004, 'DEMO-PR-004', 'Hoàn serial bảo hành Demo 04', 10002, 'Lỗi phát sinh sau bán, đã thay thế bảo hành cho khách.', 'Serial cũ đã được nhà cung cấp tiếp nhận.', '2026-10-07 08:00:00', 1400000, 2, 10002, '2026-10-07 08:00:00', 10002, '2026-10-07 08:00:00'),
    (10005, 'DEMO-PR-005', 'Hoàn serial bảo hành Demo 05', 10003, 'Lỗi phát sinh sau bán, đã thay thế bảo hành cho khách.', 'Serial cũ đã được nhà cung cấp tiếp nhận.', '2026-10-08 08:00:00', 600000, 2, 10002, '2026-10-08 08:00:00', 10002, '2026-10-08 08:00:00'),
    (10006, 'DEMO-PR-006', 'Hoàn serial bảo hành Demo 06', 10003, 'Lỗi phát sinh sau bán, đã thay thế bảo hành cho khách.', 'Serial cũ đã được nhà cung cấp tiếp nhận.', '2026-10-09 08:00:00', 1400000, 2, 10002, '2026-10-09 08:00:00', 10002, '2026-10-09 08:00:00'),
    (10007, 'DEMO-PR-007', 'Hoàn serial bảo hành Demo 07', 10004, 'Lỗi phát sinh sau bán, đã thay thế bảo hành cho khách.', 'Serial cũ đã được nhà cung cấp tiếp nhận.', '2026-10-10 08:00:00', 8000000, 2, 10002, '2026-10-10 08:00:00', 10002, '2026-10-10 08:00:00'),
    (10008, 'DEMO-PR-008', 'Hoàn serial bảo hành Demo 08', 10004, 'Lỗi phát sinh sau bán, đã thay thế bảo hành cho khách.', 'Serial cũ đã được nhà cung cấp tiếp nhận.', '2026-10-11 08:00:00', 1200000, 2, 10002, '2026-10-11 08:00:00', 10002, '2026-10-11 08:00:00'),
    (10009, 'DEMO-PR-009', 'Hoàn serial bảo hành Demo 09', 10005, 'Lỗi phát sinh sau bán, đã thay thế bảo hành cho khách.', 'Serial cũ đã được nhà cung cấp tiếp nhận.', '2026-10-12 08:00:00', 8000000, 2, 10002, '2026-10-12 08:00:00', 10002, '2026-10-12 08:00:00'),
    (10010, 'DEMO-PR-010', 'Hoàn serial bảo hành Demo 10', 10005, 'Lỗi phát sinh sau bán, đã thay thế bảo hành cho khách.', 'Serial cũ đã được nhà cung cấp tiếp nhận.', '2026-10-13 08:00:00', 1200000, 2, 10002, '2026-10-13 08:00:00', 10002, '2026-10-13 08:00:00');

-- purchase_return_items: 10 records
INSERT INTO purchase_return_items (id, purchase_return_id, product_serial_id, unit_cost, created_by, created_date, last_modified_by, last_modified_date) VALUES
    (10001, 10001, 10001, 4000000, 10002, '2026-10-04 08:00:00', 10002, '2026-10-04 08:00:00'),
    (10002, 10002, 10002, 2500000, 10002, '2026-10-05 08:00:00', 10002, '2026-10-05 08:00:00'),
    (10003, 10003, 10003, 600000, 10002, '2026-10-06 08:00:00', 10002, '2026-10-06 08:00:00'),
    (10004, 10004, 10004, 1400000, 10002, '2026-10-07 08:00:00', 10002, '2026-10-07 08:00:00'),
    (10005, 10005, 10005, 600000, 10002, '2026-10-08 08:00:00', 10002, '2026-10-08 08:00:00'),
    (10006, 10006, 10006, 1400000, 10002, '2026-10-09 08:00:00', 10002, '2026-10-09 08:00:00'),
    (10007, 10007, 10007, 8000000, 10002, '2026-10-10 08:00:00', 10002, '2026-10-10 08:00:00'),
    (10008, 10008, 10008, 1200000, 10002, '2026-10-11 08:00:00', 10002, '2026-10-11 08:00:00'),
    (10009, 10009, 10009, 8000000, 10002, '2026-10-12 08:00:00', 10002, '2026-10-12 08:00:00'),
    (10010, 10010, 10010, 1200000, 10002, '2026-10-13 08:00:00', 10002, '2026-10-13 08:00:00');

-- ---------------------------------------------------------------------------
-- 05. SALES
-- ---------------------------------------------------------------------------

-- carts: 10 records
INSERT INTO carts (id, customer_id, quantity) VALUES
    (10001, 10011, 1),
    (10002, 10012, 1),
    (10003, 10013, 1),
    (10004, 10014, 1),
    (10005, 10015, 1),
    (10006, 10016, 1),
    (10007, 10017, 1),
    (10008, 10018, 1),
    (10009, 10019, 1),
    (10010, 10020, 1);

-- cart_items: 10 records
INSERT INTO cart_items (id, cart_id, product_id, quantity, unit_price, created_date, last_modified_date) VALUES
    (10001, 10001, 10011, 1, 15600000, '2026-10-20 08:00:00', '2026-10-20 08:00:00'),
    (10002, 10002, 10012, 1, 16800000, '2026-10-20 08:00:00', '2026-10-20 08:00:00'),
    (10003, 10003, 10013, 1, 18000000, '2026-10-20 08:00:00', '2026-10-20 08:00:00'),
    (10004, 10004, 10014, 1, 19200000, '2026-10-20 08:00:00', '2026-10-20 08:00:00'),
    (10005, 10005, 10015, 1, 20400000, '2026-10-20 08:00:00', '2026-10-20 08:00:00'),
    (10006, 10006, 10016, 1, 7800000, '2026-10-20 08:00:00', '2026-10-20 08:00:00'),
    (10007, 10007, 10017, 1, 7800000, '2026-10-20 08:00:00', '2026-10-20 08:00:00'),
    (10008, 10008, 10018, 1, 7800000, '2026-10-20 08:00:00', '2026-10-20 08:00:00'),
    (10009, 10009, 10019, 1, 7800000, '2026-10-20 08:00:00', '2026-10-20 08:00:00'),
    (10010, 10010, 10020, 1, 7800000, '2026-10-20 08:00:00', '2026-10-20 08:00:00');

-- orders: 10 records
INSERT INTO orders (id, code, user_id, type, total_amount, discount_amount, final_amount, status, address, created_by, created_date, last_modified_by, last_modified_date) VALUES
    (10001, 'DEMO-DH-001', 10011, 1, 4800000, 10000, 4790000, 4, '1 Đường Demo, Hà Nội', 10006, '2026-09-10 08:00:00', 10006, '2026-09-10 08:00:00'),
    (10002, 'DEMO-DH-002', 10012, 1, 3000000, 10000, 2990000, 4, '2 Đường Demo, Hà Nội', 10005, '2026-09-11 08:00:00', 10005, '2026-09-11 08:00:00'),
    (10003, 'DEMO-DH-003', 10013, 1, 720000, 10000, 710000, 4, '3 Đường Demo, Hà Nội', 10006, '2026-09-12 08:00:00', 10006, '2026-09-12 08:00:00'),
    (10004, 'DEMO-DH-004', 10014, 1, 1680000, 10000, 1670000, 4, '4 Đường Demo, Hà Nội', 10005, '2026-09-13 08:00:00', 10005, '2026-09-13 08:00:00'),
    (10005, 'DEMO-DH-005', 10015, 1, 720000, 10000, 710000, 4, '5 Đường Demo, Hà Nội', 10006, '2026-09-14 08:00:00', 10006, '2026-09-14 08:00:00'),
    (10006, 'DEMO-DH-006', 10016, 1, 1680000, 10000, 1670000, 4, '6 Đường Demo, Hà Nội', 10005, '2026-09-15 08:00:00', 10005, '2026-09-15 08:00:00'),
    (10007, 'DEMO-DH-007', 10017, 1, 9600000, 10000, 9590000, 4, '7 Đường Demo, Hà Nội', 10006, '2026-09-16 08:00:00', 10006, '2026-09-16 08:00:00'),
    (10008, 'DEMO-DH-008', 10018, 1, 1440000, 10000, 1430000, 4, '8 Đường Demo, Hà Nội', 10005, '2026-09-17 08:00:00', 10005, '2026-09-17 08:00:00'),
    (10009, 'DEMO-DH-009', 10019, 1, 9600000, 10000, 9590000, 4, '9 Đường Demo, Hà Nội', 10006, '2026-09-18 08:00:00', 10006, '2026-09-18 08:00:00'),
    (10010, 'DEMO-DH-010', 10020, 1, 1440000, 10000, 1430000, 4, '10 Đường Demo, Hà Nội', 10005, '2026-09-19 08:00:00', 10005, '2026-09-19 08:00:00');

-- order_items: 10 records
INSERT INTO order_items (id, order_id, product_id, quantity, unit_price, discount_amount, subtotal, created_date, last_modified_date) VALUES
    (10001, 10001, 10001, 1, 4800000, 10000, 4790000, '2026-09-10 08:00:00', '2026-09-10 08:00:00'),
    (10002, 10002, 10002, 1, 3000000, 10000, 2990000, '2026-09-11 08:00:00', '2026-09-11 08:00:00'),
    (10003, 10003, 10003, 1, 720000, 10000, 710000, '2026-09-12 08:00:00', '2026-09-12 08:00:00'),
    (10004, 10004, 10004, 1, 1680000, 10000, 1670000, '2026-09-13 08:00:00', '2026-09-13 08:00:00'),
    (10005, 10005, 10003, 1, 720000, 10000, 710000, '2026-09-14 08:00:00', '2026-09-14 08:00:00'),
    (10006, 10006, 10004, 1, 1680000, 10000, 1670000, '2026-09-15 08:00:00', '2026-09-15 08:00:00'),
    (10007, 10007, 10005, 1, 9600000, 10000, 9590000, '2026-09-16 08:00:00', '2026-09-16 08:00:00'),
    (10008, 10008, 10006, 1, 1440000, 10000, 1430000, '2026-09-17 08:00:00', '2026-09-17 08:00:00'),
    (10009, 10009, 10005, 1, 9600000, 10000, 9590000, '2026-09-18 08:00:00', '2026-09-18 08:00:00'),
    (10010, 10010, 10006, 1, 1440000, 10000, 1430000, '2026-09-19 08:00:00', '2026-09-19 08:00:00');

-- order_item_serial: 10 records
INSERT INTO order_item_serial (id, order_item_id, product_serial_id, created_date) VALUES
    (10001, 10001, 10001, '2026-09-11 08:00:00'),
    (10002, 10002, 10002, '2026-09-12 08:00:00'),
    (10003, 10003, 10003, '2026-09-13 08:00:00'),
    (10004, 10004, 10004, '2026-09-14 08:00:00'),
    (10005, 10005, 10005, '2026-09-15 08:00:00'),
    (10006, 10006, 10006, '2026-09-16 08:00:00'),
    (10007, 10007, 10007, '2026-09-17 08:00:00'),
    (10008, 10008, 10008, '2026-09-18 08:00:00'),
    (10009, 10009, 10009, '2026-09-19 08:00:00'),
    (10010, 10010, 10010, '2026-09-20 08:00:00');

-- ---------------------------------------------------------------------------
-- 06. BILLING & PAYMENTS
-- ---------------------------------------------------------------------------

-- payment_methods: 10 records
INSERT INTO payment_methods (id, code, name, type, bank_name, account_number, status, is_deleted, created_by, created_date, last_modified_by, last_modified_date) VALUES
    (10001, 'DEMO-CASH', 'Tiền mặt Demo', 0, NULL, NULL, 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10002, 'DEMO-BANK-02', 'Chuyển khoản Ngân hàng Demo 02', 1, 'Ngân hàng Demo 02', '000000000002', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10003, 'DEMO-BANK-03', 'Chuyển khoản Ngân hàng Demo 03', 1, 'Ngân hàng Demo 03', '000000000003', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10004, 'DEMO-BANK-04', 'Chuyển khoản Ngân hàng Demo 04', 1, 'Ngân hàng Demo 04', '000000000004', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10005, 'DEMO-BANK-05', 'Chuyển khoản Ngân hàng Demo 05', 1, 'Ngân hàng Demo 05', '000000000005', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10006, 'DEMO-BANK-06', 'Chuyển khoản Ngân hàng Demo 06', 1, 'Ngân hàng Demo 06', '000000000006', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10007, 'DEMO-BANK-07', 'Chuyển khoản Ngân hàng Demo 07', 1, 'Ngân hàng Demo 07', '000000000007', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10008, 'DEMO-BANK-08', 'Chuyển khoản Ngân hàng Demo 08', 1, 'Ngân hàng Demo 08', '000000000008', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10009, 'DEMO-BANK-09', 'Chuyển khoản Ngân hàng Demo 09', 1, 'Ngân hàng Demo 09', '000000000009', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10010, 'DEMO-BANK-10', 'Chuyển khoản Ngân hàng Demo 10', 1, 'Ngân hàng Demo 10', '000000000010', 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00');

-- invoices: 10 records
INSERT INTO invoices (id, code, order_id, invoice_date, total_amount, discount_amount, tax_amount, amount, status, created_by, created_date, last_modified_by, last_modified_date) VALUES
    (10001, 'DEMO-HD-001', 10001, '2026-09-10 09:00:00', 4800000, 10000, 0, 4790000, 2, 10006, '2026-09-10 08:00:00', 10006, '2026-09-10 08:00:00'),
    (10002, 'DEMO-HD-002', 10002, '2026-09-11 09:00:00', 3000000, 10000, 0, 2990000, 2, 10005, '2026-09-11 08:00:00', 10005, '2026-09-11 08:00:00'),
    (10003, 'DEMO-HD-003', 10003, '2026-09-12 09:00:00', 720000, 10000, 0, 710000, 2, 10006, '2026-09-12 08:00:00', 10006, '2026-09-12 08:00:00'),
    (10004, 'DEMO-HD-004', 10004, '2026-09-13 09:00:00', 1680000, 10000, 0, 1670000, 2, 10005, '2026-09-13 08:00:00', 10005, '2026-09-13 08:00:00'),
    (10005, 'DEMO-HD-005', 10005, '2026-09-14 09:00:00', 720000, 10000, 0, 710000, 2, 10006, '2026-09-14 08:00:00', 10006, '2026-09-14 08:00:00'),
    (10006, 'DEMO-HD-006', 10006, '2026-09-15 09:00:00', 1680000, 10000, 0, 1670000, 2, 10005, '2026-09-15 08:00:00', 10005, '2026-09-15 08:00:00'),
    (10007, 'DEMO-HD-007', 10007, '2026-09-16 09:00:00', 9600000, 10000, 0, 9590000, 2, 10006, '2026-09-16 08:00:00', 10006, '2026-09-16 08:00:00'),
    (10008, 'DEMO-HD-008', 10008, '2026-09-17 09:00:00', 1440000, 10000, 0, 1430000, 2, 10005, '2026-09-17 08:00:00', 10005, '2026-09-17 08:00:00'),
    (10009, 'DEMO-HD-009', 10009, '2026-09-18 09:00:00', 9600000, 10000, 0, 9590000, 2, 10006, '2026-09-18 08:00:00', 10006, '2026-09-18 08:00:00'),
    (10010, 'DEMO-HD-010', 10010, '2026-09-19 09:00:00', 1440000, 10000, 0, 1430000, 2, 10005, '2026-09-19 08:00:00', 10005, '2026-09-19 08:00:00');

-- payment: 10 records
INSERT INTO payment (id, order_id, payment_method_id, amount, transaction_code, status, paid_at, created_date, last_modified_date) VALUES
    (10001, 10001, 10001, 4790000, 'DEMO-TXN-001', 1, '2026-09-10 09:00:00', '2026-09-10 08:00:00', '2026-09-10 09:00:00'),
    (10002, 10002, 10002, 2990000, 'DEMO-TXN-002', 1, '2026-09-11 09:00:00', '2026-09-11 08:00:00', '2026-09-11 09:00:00'),
    (10003, 10003, 10003, 710000, 'DEMO-TXN-003', 1, '2026-09-12 09:00:00', '2026-09-12 08:00:00', '2026-09-12 09:00:00'),
    (10004, 10004, 10004, 1670000, 'DEMO-TXN-004', 1, '2026-09-13 09:00:00', '2026-09-13 08:00:00', '2026-09-13 09:00:00'),
    (10005, 10005, 10005, 710000, 'DEMO-TXN-005', 1, '2026-09-14 09:00:00', '2026-09-14 08:00:00', '2026-09-14 09:00:00'),
    (10006, 10006, 10006, 1670000, 'DEMO-TXN-006', 1, '2026-09-15 09:00:00', '2026-09-15 08:00:00', '2026-09-15 09:00:00'),
    (10007, 10007, 10007, 9590000, 'DEMO-TXN-007', 1, '2026-09-16 09:00:00', '2026-09-16 08:00:00', '2026-09-16 09:00:00'),
    (10008, 10008, 10008, 1430000, 'DEMO-TXN-008', 1, '2026-09-17 09:00:00', '2026-09-17 08:00:00', '2026-09-17 09:00:00'),
    (10009, 10009, 10009, 9590000, 'DEMO-TXN-009', 1, '2026-09-18 09:00:00', '2026-09-18 08:00:00', '2026-09-18 09:00:00'),
    (10010, 10010, 10010, 1430000, 'DEMO-TXN-010', 1, '2026-09-19 09:00:00', '2026-09-19 08:00:00', '2026-09-19 09:00:00');

-- ---------------------------------------------------------------------------
-- 07. WARRANTY & TECHNICAL SERVICE
-- ---------------------------------------------------------------------------

-- warranty_policy: 10 records
INSERT INTO warranty_policy (id, name, duration_days, description, target_type, target_id, status, is_deleted, created_by, created_date, last_modified_by, last_modified_date) VALUES
    (10001, 'Bảo hành Demo sản phẩm 01', 365, 'Bảo hành thay thế trong dữ liệu minh họa.', 1, 10001, 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10002, 'Bảo hành Demo sản phẩm 02', 365, 'Bảo hành thay thế trong dữ liệu minh họa.', 1, 10002, 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10003, 'Bảo hành Demo sản phẩm 03', 365, 'Bảo hành thay thế trong dữ liệu minh họa.', 1, 10003, 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10004, 'Bảo hành Demo sản phẩm 04', 365, 'Bảo hành thay thế trong dữ liệu minh họa.', 1, 10004, 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10005, 'Bảo hành Demo sản phẩm 05', 365, 'Bảo hành thay thế trong dữ liệu minh họa.', 1, 10005, 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10006, 'Bảo hành Demo sản phẩm 06', 365, 'Bảo hành thay thế trong dữ liệu minh họa.', 1, 10006, 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10007, 'Bảo hành Demo sản phẩm 07', 365, 'Bảo hành thay thế trong dữ liệu minh họa.', 1, 10007, 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10008, 'Bảo hành Demo sản phẩm 08', 365, 'Bảo hành thay thế trong dữ liệu minh họa.', 1, 10008, 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10009, 'Bảo hành Demo sản phẩm 09', 365, 'Bảo hành thay thế trong dữ liệu minh họa.', 1, 10009, 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00'),
    (10010, 'Bảo hành Demo sản phẩm 10', 365, 'Bảo hành thay thế trong dữ liệu minh họa.', 1, 10010, 1, 0, 10002, '2026-07-01 08:00:00', 10002, '2026-07-01 08:00:00');

-- warranty: 10 records
INSERT INTO warranty (id, order_item_serial_id, warranty_policy_id, start_date, end_date, status, note, created_by, created_date, last_modified_by, last_modified_date) VALUES
    (10001, 10001, 10001, '2026-09-13 08:00:00', '2027-09-13 08:00:00', 1, 'Serial cũ đã đổi bảo hành, lưu lịch sử mua hàng.', 10006, '2026-09-13 08:00:00', 10006, '2026-09-13 08:00:00'),
    (10002, 10002, 10002, '2026-09-14 08:00:00', '2027-09-14 08:00:00', 1, 'Serial cũ đã đổi bảo hành, lưu lịch sử mua hàng.', 10005, '2026-09-14 08:00:00', 10005, '2026-09-14 08:00:00'),
    (10003, 10003, 10003, '2026-09-15 08:00:00', '2027-09-15 08:00:00', 1, 'Serial cũ đã đổi bảo hành, lưu lịch sử mua hàng.', 10006, '2026-09-15 08:00:00', 10006, '2026-09-15 08:00:00'),
    (10004, 10004, 10004, '2026-09-16 08:00:00', '2027-09-16 08:00:00', 1, 'Serial cũ đã đổi bảo hành, lưu lịch sử mua hàng.', 10005, '2026-09-16 08:00:00', 10005, '2026-09-16 08:00:00'),
    (10005, 10005, 10003, '2026-09-17 08:00:00', '2027-09-17 08:00:00', 1, 'Serial cũ đã đổi bảo hành, lưu lịch sử mua hàng.', 10006, '2026-09-17 08:00:00', 10006, '2026-09-17 08:00:00'),
    (10006, 10006, 10004, '2026-09-18 08:00:00', '2027-09-18 08:00:00', 1, 'Serial cũ đã đổi bảo hành, lưu lịch sử mua hàng.', 10005, '2026-09-18 08:00:00', 10005, '2026-09-18 08:00:00'),
    (10007, 10007, 10005, '2026-09-19 08:00:00', '2027-09-19 08:00:00', 1, 'Serial cũ đã đổi bảo hành, lưu lịch sử mua hàng.', 10006, '2026-09-19 08:00:00', 10006, '2026-09-19 08:00:00'),
    (10008, 10008, 10006, '2026-09-20 08:00:00', '2027-09-20 08:00:00', 1, 'Serial cũ đã đổi bảo hành, lưu lịch sử mua hàng.', 10005, '2026-09-20 08:00:00', 10005, '2026-09-20 08:00:00'),
    (10009, 10009, 10005, '2026-09-21 08:00:00', '2027-09-21 08:00:00', 1, 'Serial cũ đã đổi bảo hành, lưu lịch sử mua hàng.', 10006, '2026-09-21 08:00:00', 10006, '2026-09-21 08:00:00'),
    (10010, 10010, 10006, '2026-09-22 08:00:00', '2027-09-22 08:00:00', 1, 'Serial cũ đã đổi bảo hành, lưu lịch sử mua hàng.', 10005, '2026-09-22 08:00:00', 10005, '2026-09-22 08:00:00');

-- warranty_request: 10 records
INSERT INTO warranty_request (id, request_code, warranty_id, customer_id, technician_id, issue_description, inspection_notes, solution, status, completed_at, created_by, created_date, last_modified_by, last_modified_date) VALUES
    (10001, 'DEMO-BH-001', 10001, 10011, 10007, 'Thiết bị hoạt động không ổn định sau thời gian sử dụng.', 'Đã kiểm tra và xác nhận lỗi phần cứng.', 'Thay bằng serial DEMO-SN-00011, serial cũ hoàn nhà cung cấp.', 3, '2026-10-03 08:00:00', 10007, '2026-10-01 08:00:00', 10007, '2026-10-01 08:00:00'),
    (10002, 'DEMO-BH-002', 10002, 10012, 10008, 'Thiết bị hoạt động không ổn định sau thời gian sử dụng.', 'Đã kiểm tra và xác nhận lỗi phần cứng.', 'Thay bằng serial DEMO-SN-00012, serial cũ hoàn nhà cung cấp.', 3, '2026-10-04 08:00:00', 10008, '2026-10-02 08:00:00', 10008, '2026-10-02 08:00:00'),
    (10003, 'DEMO-BH-003', 10003, 10013, 10010, 'Thiết bị hoạt động không ổn định sau thời gian sử dụng.', 'Đã kiểm tra và xác nhận lỗi phần cứng.', 'Thay bằng serial DEMO-SN-00013, serial cũ hoàn nhà cung cấp.', 3, '2026-10-05 08:00:00', 10010, '2026-10-03 08:00:00', 10010, '2026-10-03 08:00:00'),
    (10004, 'DEMO-BH-004', 10004, 10014, 10007, 'Thiết bị hoạt động không ổn định sau thời gian sử dụng.', 'Đã kiểm tra và xác nhận lỗi phần cứng.', 'Thay bằng serial DEMO-SN-00014, serial cũ hoàn nhà cung cấp.', 3, '2026-10-06 08:00:00', 10007, '2026-10-04 08:00:00', 10007, '2026-10-04 08:00:00'),
    (10005, 'DEMO-BH-005', 10005, 10015, 10008, 'Thiết bị hoạt động không ổn định sau thời gian sử dụng.', 'Đã kiểm tra và xác nhận lỗi phần cứng.', 'Thay bằng serial DEMO-SN-00015, serial cũ hoàn nhà cung cấp.', 3, '2026-10-07 08:00:00', 10008, '2026-10-05 08:00:00', 10008, '2026-10-05 08:00:00'),
    (10006, 'DEMO-BH-006', 10006, 10016, 10010, 'Thiết bị hoạt động không ổn định sau thời gian sử dụng.', 'Đã kiểm tra và xác nhận lỗi phần cứng.', 'Thay bằng serial DEMO-SN-00016, serial cũ hoàn nhà cung cấp.', 3, '2026-10-08 08:00:00', 10010, '2026-10-06 08:00:00', 10010, '2026-10-06 08:00:00'),
    (10007, 'DEMO-BH-007', 10007, 10017, 10007, 'Thiết bị hoạt động không ổn định sau thời gian sử dụng.', 'Đã kiểm tra và xác nhận lỗi phần cứng.', 'Thay bằng serial DEMO-SN-00017, serial cũ hoàn nhà cung cấp.', 3, '2026-10-09 08:00:00', 10007, '2026-10-07 08:00:00', 10007, '2026-10-07 08:00:00'),
    (10008, 'DEMO-BH-008', 10008, 10018, 10008, 'Thiết bị hoạt động không ổn định sau thời gian sử dụng.', 'Đã kiểm tra và xác nhận lỗi phần cứng.', 'Thay bằng serial DEMO-SN-00018, serial cũ hoàn nhà cung cấp.', 3, '2026-10-10 08:00:00', 10008, '2026-10-08 08:00:00', 10008, '2026-10-08 08:00:00'),
    (10009, 'DEMO-BH-009', 10009, 10019, 10010, 'Thiết bị hoạt động không ổn định sau thời gian sử dụng.', 'Đã kiểm tra và xác nhận lỗi phần cứng.', 'Thay bằng serial DEMO-SN-00019, serial cũ hoàn nhà cung cấp.', 3, '2026-10-11 08:00:00', 10010, '2026-10-09 08:00:00', 10010, '2026-10-09 08:00:00'),
    (10010, 'DEMO-BH-010', 10010, 10020, 10007, 'Thiết bị hoạt động không ổn định sau thời gian sử dụng.', 'Đã kiểm tra và xác nhận lỗi phần cứng.', 'Thay bằng serial DEMO-SN-00020, serial cũ hoàn nhà cung cấp.', 3, '2026-10-12 08:00:00', 10007, '2026-10-10 08:00:00', 10007, '2026-10-10 08:00:00');

-- warranty_parts: 10 records
INSERT INTO warranty_parts (id, warranty_request_id, product_id, quantity, unit_cost, status, created_by, created_date, last_modified_by, last_modified_date) VALUES
    (10001, 10001, 10001, 1, 4000000, 2, 10007, '2026-10-03 08:00:00', 10007, '2026-10-03 08:00:00'),
    (10002, 10002, 10002, 1, 2500000, 2, 10008, '2026-10-04 08:00:00', 10008, '2026-10-04 08:00:00'),
    (10003, 10003, 10003, 1, 600000, 2, 10010, '2026-10-05 08:00:00', 10010, '2026-10-05 08:00:00'),
    (10004, 10004, 10004, 1, 1400000, 2, 10007, '2026-10-06 08:00:00', 10007, '2026-10-06 08:00:00'),
    (10005, 10005, 10003, 1, 600000, 2, 10008, '2026-10-07 08:00:00', 10008, '2026-10-07 08:00:00'),
    (10006, 10006, 10004, 1, 1400000, 2, 10010, '2026-10-08 08:00:00', 10010, '2026-10-08 08:00:00'),
    (10007, 10007, 10005, 1, 8000000, 2, 10007, '2026-10-09 08:00:00', 10007, '2026-10-09 08:00:00'),
    (10008, 10008, 10006, 1, 1200000, 2, 10008, '2026-10-10 08:00:00', 10008, '2026-10-10 08:00:00'),
    (10009, 10009, 10005, 1, 8000000, 2, 10010, '2026-10-11 08:00:00', 10010, '2026-10-11 08:00:00'),
    (10010, 10010, 10006, 1, 1200000, 2, 10007, '2026-10-12 08:00:00', 10007, '2026-10-12 08:00:00');

-- warranty_part_serial: 10 records
INSERT INTO warranty_part_serial (id, warranty_part_id, product_serial_id) VALUES
    (10001, 10001, 10011),
    (10002, 10002, 10012),
    (10003, 10003, 10013),
    (10004, 10004, 10014),
    (10005, 10005, 10015),
    (10006, 10006, 10016),
    (10007, 10007, 10017),
    (10008, 10008, 10018),
    (10009, 10009, 10019),
    (10010, 10010, 10020);

-- ---------------------------------------------------------------------------
-- 08. DELIVERY, REVIEWS & FAVORITES
-- ---------------------------------------------------------------------------

-- delivery: 20 records
INSERT INTO delivery (id, resource_type, resource_id, tracking_code, carrier_name, shipping_fee, status, shipped_date, shipped_by, delivered_date) VALUES
    (10001, 0, 10001, 'DEMO-SHIP-001', 'Vận chuyển Demo', 0, 2, '2026-09-11 08:00:00', 10004, '2026-09-13 08:00:00'),
    (10011, 1, 10001, 'DEMO-RETURN-001', 'Vận chuyển Demo', 0, 2, '2026-10-04 08:00:00', 10004, '2026-10-05 08:00:00'),
    (10002, 0, 10002, 'DEMO-SHIP-002', 'Vận chuyển Demo', 0, 2, '2026-09-12 08:00:00', 10003, '2026-09-14 08:00:00'),
    (10012, 1, 10002, 'DEMO-RETURN-002', 'Vận chuyển Demo', 0, 2, '2026-10-05 08:00:00', 10003, '2026-10-06 08:00:00'),
    (10003, 0, 10003, 'DEMO-SHIP-003', 'Vận chuyển Demo', 0, 2, '2026-09-13 08:00:00', 10004, '2026-09-15 08:00:00'),
    (10013, 1, 10003, 'DEMO-RETURN-003', 'Vận chuyển Demo', 0, 2, '2026-10-06 08:00:00', 10004, '2026-10-07 08:00:00'),
    (10004, 0, 10004, 'DEMO-SHIP-004', 'Vận chuyển Demo', 0, 2, '2026-09-14 08:00:00', 10003, '2026-09-16 08:00:00'),
    (10014, 1, 10004, 'DEMO-RETURN-004', 'Vận chuyển Demo', 0, 2, '2026-10-07 08:00:00', 10003, '2026-10-08 08:00:00'),
    (10005, 0, 10005, 'DEMO-SHIP-005', 'Vận chuyển Demo', 0, 2, '2026-09-15 08:00:00', 10004, '2026-09-17 08:00:00'),
    (10015, 1, 10005, 'DEMO-RETURN-005', 'Vận chuyển Demo', 0, 2, '2026-10-08 08:00:00', 10004, '2026-10-09 08:00:00'),
    (10006, 0, 10006, 'DEMO-SHIP-006', 'Vận chuyển Demo', 0, 2, '2026-09-16 08:00:00', 10003, '2026-09-18 08:00:00'),
    (10016, 1, 10006, 'DEMO-RETURN-006', 'Vận chuyển Demo', 0, 2, '2026-10-09 08:00:00', 10003, '2026-10-10 08:00:00'),
    (10007, 0, 10007, 'DEMO-SHIP-007', 'Vận chuyển Demo', 0, 2, '2026-09-17 08:00:00', 10004, '2026-09-19 08:00:00'),
    (10017, 1, 10007, 'DEMO-RETURN-007', 'Vận chuyển Demo', 0, 2, '2026-10-10 08:00:00', 10004, '2026-10-11 08:00:00'),
    (10008, 0, 10008, 'DEMO-SHIP-008', 'Vận chuyển Demo', 0, 2, '2026-09-18 08:00:00', 10003, '2026-09-20 08:00:00'),
    (10018, 1, 10008, 'DEMO-RETURN-008', 'Vận chuyển Demo', 0, 2, '2026-10-11 08:00:00', 10003, '2026-10-12 08:00:00'),
    (10009, 0, 10009, 'DEMO-SHIP-009', 'Vận chuyển Demo', 0, 2, '2026-09-19 08:00:00', 10004, '2026-09-21 08:00:00'),
    (10019, 1, 10009, 'DEMO-RETURN-009', 'Vận chuyển Demo', 0, 2, '2026-10-12 08:00:00', 10004, '2026-10-13 08:00:00'),
    (10010, 0, 10010, 'DEMO-SHIP-010', 'Vận chuyển Demo', 0, 2, '2026-09-20 08:00:00', 10003, '2026-09-22 08:00:00'),
    (10020, 1, 10010, 'DEMO-RETURN-010', 'Vận chuyển Demo', 0, 2, '2026-10-13 08:00:00', 10003, '2026-10-14 08:00:00');

-- product_reviews: 10 records
INSERT INTO product_reviews (id, product_id, customer_id, rating, title, content, status, created_at, updated_at) VALUES
    (10001, 10001, 10011, 4, 'Đánh giá mua hàng Demo', 'Cửa hàng đã hỗ trợ đổi bảo hành. Nội dung này là dữ liệu kiểm thử.', 1, '2026-10-04 08:00:00', '2026-10-04 08:00:00'),
    (10002, 10002, 10012, 5, 'Đánh giá mua hàng Demo', 'Cửa hàng đã hỗ trợ đổi bảo hành. Nội dung này là dữ liệu kiểm thử.', 1, '2026-10-05 08:00:00', '2026-10-05 08:00:00'),
    (10003, 10003, 10013, 3, 'Đánh giá mua hàng Demo', 'Cửa hàng đã hỗ trợ đổi bảo hành. Nội dung này là dữ liệu kiểm thử.', 1, '2026-10-06 08:00:00', '2026-10-06 08:00:00'),
    (10004, 10004, 10014, 4, 'Đánh giá mua hàng Demo', 'Cửa hàng đã hỗ trợ đổi bảo hành. Nội dung này là dữ liệu kiểm thử.', 1, '2026-10-07 08:00:00', '2026-10-07 08:00:00'),
    (10005, 10003, 10015, 5, 'Đánh giá mua hàng Demo', 'Cửa hàng đã hỗ trợ đổi bảo hành. Nội dung này là dữ liệu kiểm thử.', 1, '2026-10-08 08:00:00', '2026-10-08 08:00:00'),
    (10006, 10004, 10016, 3, 'Đánh giá mua hàng Demo', 'Cửa hàng đã hỗ trợ đổi bảo hành. Nội dung này là dữ liệu kiểm thử.', 1, '2026-10-09 08:00:00', '2026-10-09 08:00:00'),
    (10007, 10005, 10017, 4, 'Đánh giá mua hàng Demo', 'Cửa hàng đã hỗ trợ đổi bảo hành. Nội dung này là dữ liệu kiểm thử.', 1, '2026-10-10 08:00:00', '2026-10-10 08:00:00'),
    (10008, 10006, 10018, 5, 'Đánh giá mua hàng Demo', 'Cửa hàng đã hỗ trợ đổi bảo hành. Nội dung này là dữ liệu kiểm thử.', 1, '2026-10-11 08:00:00', '2026-10-11 08:00:00'),
    (10009, 10005, 10019, 3, 'Đánh giá mua hàng Demo', 'Cửa hàng đã hỗ trợ đổi bảo hành. Nội dung này là dữ liệu kiểm thử.', 1, '2026-10-12 08:00:00', '2026-10-12 08:00:00'),
    (10010, 10006, 10020, 4, 'Đánh giá mua hàng Demo', 'Cửa hàng đã hỗ trợ đổi bảo hành. Nội dung này là dữ liệu kiểm thử.', 1, '2026-10-13 08:00:00', '2026-10-13 08:00:00');

-- product_favorites: 10 records
INSERT INTO product_favorites (id, customer_id, product_id, created_at) VALUES
    (10001, 10011, 10011, '2026-10-20 08:00:00'),
    (10002, 10012, 10012, '2026-10-20 08:00:00'),
    (10003, 10013, 10013, '2026-10-20 08:00:00'),
    (10004, 10014, 10014, '2026-10-20 08:00:00'),
    (10005, 10015, 10015, '2026-10-20 08:00:00'),
    (10006, 10016, 10016, '2026-10-20 08:00:00'),
    (10007, 10017, 10017, '2026-10-20 08:00:00'),
    (10008, 10018, 10018, '2026-10-20 08:00:00'),
    (10009, 10019, 10019, '2026-10-20 08:00:00'),
    (10010, 10020, 10020, '2026-10-20 08:00:00');
