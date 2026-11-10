-- Additional fixtures for warranty activation. V2 has already-completed orders only.
-- Reserved IDs 21001-21009. Run by Flyway once, after V2.
INSERT INTO brands (id, name, slug, status, is_deleted, created_date, last_modified_date)
VALUES (21001, 'Warranty Test Brand', 'warranty-test-brand', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, slug, status, is_deleted, created_date, last_modified_date)
VALUES (21001, 'Warranty Test Category', 'warranty-test-category', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO products (id, name, code, price, product_type, status, brand_id, is_deleted, created_date, last_modified_date)
VALUES (21001, 'Warranty Test Product', 'DEMO-BH-PRODUCT', 1000000, 0, 1, 21001, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO category_products (id, product_id, category_id, is_primary)
VALUES (21001, 21001, 21001, 1);

INSERT INTO warranty_policy (id, name, duration_days, target_type, target_id, status, is_deleted, created_date, last_modified_date) VALUES
(21001, 'DEMO BH chung 30 ngay', 30, NULL, NULL, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21002, 'DEMO BH danh muc 90 ngay', 90, 0, 21001, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21003, 'DEMO BH san pham 180 ngay', 180, 1, 21001, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21004, 'DEMO BH thuong hieu 365 ngay', 365, 2, 21001, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21005, 'DEMO BH inactive khong duoc cap', 7, 1, 21001, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21006, 'DEMO BH deleted khong duoc cap', 7, 1, 21001, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21007, 'DEMO BH khong khop san pham', 7, 1, 10001, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO product_serials (id, product_id, serial_number, status, is_terminated, created_date, last_modified_date) VALUES
(21001, 21001, 'DEMO-BH-DIRECT-A', 6, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21002, 21001, 'DEMO-BH-DIRECT-B', 6, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21003, 21001, 'DEMO-BH-SPARE', 2, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21004, 21001, 'DEMO-BH-CREATE', 2, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21005, 21001, 'DEMO-BH-DELIVERY', 6, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO orders (id, code, user_id, type, total_amount, discount_amount, final_amount, status, created_date, last_modified_date) VALUES
(21001, 'DEMO-BH-DIRECT', 10011, 0, 2000000, 0, 2000000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21002, 'DEMO-BH-DELIVERY', 10011, 1, 1000000, 0, 1000000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO order_items (id, order_id, product_id, quantity, unit_price, discount_amount, subtotal, created_date, last_modified_date) VALUES
(21001, 21001, 21001, 2, 1000000, 0, 2000000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21002, 21002, 21001, 1, 1000000, 0, 1000000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO order_item_serial (id, order_item_id, product_serial_id, created_date) VALUES
(21001, 21001, 21001, CURRENT_TIMESTAMP),
(21002, 21001, 21002, CURRENT_TIMESTAMP),
(21003, 21002, 21005, CURRENT_TIMESTAMP);
