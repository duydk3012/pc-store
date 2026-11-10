-- PC Store: initial schema (fresh databases only).
-- Contents: 01 Identity | 02 Suppliers | 03 Catalog | 04 Inventory
--           05 Sales | 06 Billing | 07 Warranty | 08 Delivery & Reviews
-- Schema sections follow foreign-key dependencies; indexes stay beside their tables.
-- Tables use the final schema, with no historical ALTER/backfill steps.
-- Numeric codes and index decisions are documented in readme/DATABASE_TYPES_AND_INDEXES.md.
-- Demo data is loaded separately by V2__seed_demo_data.sql. Future changes start at V3.

-- ============================================================================
-- 01. IDENTITY & AUTHENTICATION: users, refresh tokens, account challenges
-- ============================================================================

CREATE TABLE users (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    username                     VARCHAR(50)       NOT NULL,
    password_hash                VARCHAR(255)      NOT NULL,
    full_name                    VARCHAR(100)      NOT NULL,
    email                        VARCHAR(255)      NOT NULL,
    phone_number                 VARCHAR(15)       NULL,
    address                      VARCHAR(500)      NULL,
    citizen_identification_card  VARCHAR(20)       NULL,
    role                         TINYINT UNSIGNED  NOT NULL COMMENT '0: admin, 1: manager, 2: customer, 3: staff_warehouse, 4: staff_sales, 5: staff_tech',
    status                       TINYINT UNSIGNED  NOT NULL DEFAULT 1 COMMENT '0: inactive, 1: active, 2: banned',
    token_version                INT               NOT NULL DEFAULT 0,
    is_deleted                   TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '0: false, 1: true',
    created_by                   BIGINT            NULL,
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_by             BIGINT            NULL,
    last_modified_date           DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    email_verified               TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '0: false, 1: true',
    PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email    UNIQUE (email),
    CONSTRAINT chk_users_role    CHECK (role IN (0, 1, 2, 3, 4, 5)),
    CONSTRAINT chk_users_status  CHECK (status IN (0, 1, 2)),
    CONSTRAINT chk_users_is_deleted CHECK (is_deleted IN (0, 1)),
    CONSTRAINT chk_users_email_verified CHECK (email_verified IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_users_deleted_role_status ON users (is_deleted, role, status);
CREATE INDEX idx_users_phone ON users (phone_number);

CREATE TABLE refresh_tokens (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    user_id                      BIGINT            NOT NULL,
    token_hash                   VARCHAR(64)       NOT NULL,
    expires_at                   DATETIME(6)       NOT NULL,
    revoked                      TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '0: false, 1: true',
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_refresh_tokens_revoked CHECK (revoked IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_refresh_tokens_user_revoked ON refresh_tokens (user_id, revoked);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens (expires_at);

CREATE TABLE account_challenges (
    id                           BIGINT            NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id                      BIGINT            NOT NULL,
    purpose                      TINYINT UNSIGNED  NOT NULL COMMENT '0: verify_email, 1: reset_password, 2: change_password',
    destination                  VARCHAR(255)      NOT NULL,
    code_hash                    VARCHAR(255)      NOT NULL,
    created_at                   TIMESTAMP(6)      NOT NULL,
    expires_at                   TIMESTAMP(6)      NOT NULL,
    attempts                     INT               NOT NULL DEFAULT 0,
    consumed                     TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '0: false, 1: true',
    CONSTRAINT fk_challenge_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_challenge_user_purpose UNIQUE (user_id, purpose),
    CONSTRAINT chk_account_challenges_purpose CHECK (purpose BETWEEN 0 AND 2),
    CONSTRAINT chk_account_challenges_consumed CHECK (consumed IN (0, 1))
);

-- ============================================================================
-- 02. SUPPLIERS
-- ============================================================================

CREATE TABLE suppliers (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    code                         VARCHAR(50)       NOT NULL,
    name                         VARCHAR(100)      NOT NULL,
    contact_person               VARCHAR(255)      NULL,
    email                        VARCHAR(100)      NOT NULL,
    phone_number                 VARCHAR(20)       NULL,
    address                      VARCHAR(500)      NULL,
    tax_code                     VARCHAR(50)       NULL,
    status                       TINYINT UNSIGNED  NOT NULL DEFAULT 1 COMMENT '0: inactive, 1: active',
    is_deleted                   TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '0: false, 1: true',
    created_by                   BIGINT            NULL,
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_by             BIGINT            NULL,
    last_modified_date           DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_suppliers_code UNIQUE (code),
    CONSTRAINT uk_suppliers_email UNIQUE (email),
    CONSTRAINT chk_suppliers_status CHECK (status IN (0, 1)),
    CONSTRAINT chk_suppliers_is_deleted CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_suppliers_deleted_status ON suppliers (is_deleted, status);

-- ============================================================================
-- 03. PRODUCT CATALOG: categories, brands, products
-- ============================================================================

-- Module Catalog: categories, brands, products, category_products, building_pc

CREATE TABLE categories (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    name                         VARCHAR(100)      NOT NULL,
    slug                         VARCHAR(100)      NOT NULL,
    description                  VARCHAR(500)      NULL,
    status                       TINYINT UNSIGNED  NOT NULL DEFAULT 1 COMMENT '0: inactive, 1: active',
    parent_id                    BIGINT            NULL,
    is_deleted                   TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '0: false, 1: true',
    created_by                   BIGINT            NULL,
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_by             BIGINT            NULL,
    last_modified_date           DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_categories_slug UNIQUE (slug),
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories (id) ON DELETE SET NULL,
    CONSTRAINT chk_categories_status CHECK (status BETWEEN 0 AND 1),
    CONSTRAINT chk_categories_is_deleted CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_categories_parent_deleted_status ON categories (parent_id, is_deleted, status);
CREATE INDEX idx_categories_deleted_status ON categories (is_deleted, status);

CREATE TABLE brands (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    name                         VARCHAR(100)      NOT NULL,
    slug                         VARCHAR(100)      NOT NULL,
    description                  VARCHAR(500)      NULL,
    status                       TINYINT UNSIGNED  NOT NULL DEFAULT 1 COMMENT '0: inactive, 1: active',
    is_deleted                   TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '0: false, 1: true',
    created_by                   BIGINT            NULL,
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_by             BIGINT            NULL,
    last_modified_date           DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_brands_slug UNIQUE (slug),
    CONSTRAINT chk_brands_status CHECK (status BETWEEN 0 AND 1),
    CONSTRAINT chk_brands_is_deleted CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_brands_deleted_status ON brands (is_deleted, status);

CREATE TABLE products (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    name                         VARCHAR(255)      NOT NULL,
    code                         VARCHAR(50)       NOT NULL,
    price                        BIGINT            NULL,
    description                  TEXT              NULL,
    specification                TEXT              NULL,
    product_type                 TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '0: other, 1: pc',
    status                       TINYINT UNSIGNED  NOT NULL DEFAULT 1 COMMENT '0: inactive, 1: active, 2: out_of_stock',
    slug                         VARCHAR(255)      NULL,
    image_url                    LONGTEXT          NULL,
    brand_id                     BIGINT            NULL,
    is_deleted                   TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '0: false, 1: true',
    created_by                   BIGINT            NULL,
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_by             BIGINT            NULL,
    last_modified_date           DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_products_code UNIQUE (code),
    CONSTRAINT uk_products_slug UNIQUE (slug),
    CONSTRAINT fk_products_brand FOREIGN KEY (brand_id) REFERENCES brands (id) ON DELETE SET NULL,
    CONSTRAINT chk_products_product_type CHECK (product_type IN (0, 1)),
    CONSTRAINT chk_products_status CHECK (status BETWEEN 0 AND 2),
    CONSTRAINT chk_products_is_deleted CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_products_brand_deleted_status ON products (brand_id, is_deleted, status);
CREATE INDEX idx_products_deleted_status_type ON products (is_deleted, status, product_type);

CREATE TABLE category_products (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    product_id                   BIGINT            NOT NULL,
    category_id                  BIGINT            NOT NULL,
    is_primary                   TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '0: false, 1: true',
    PRIMARY KEY (id),
    CONSTRAINT uk_category_products UNIQUE (product_id, category_id),
    CONSTRAINT fk_cp_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_cp_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE,
    CONSTRAINT chk_category_products_is_primary CHECK (is_primary IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_cp_category ON category_products (category_id);

CREATE TABLE building_pc (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    pc_id                        BIGINT            NOT NULL,
    product_id                   BIGINT            NOT NULL,
    quantity                     INT               NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    CONSTRAINT fk_bpc_pc FOREIGN KEY (pc_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_bpc_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_bpc_pc ON building_pc (pc_id);
CREATE INDEX idx_bpc_product ON building_pc (product_id);

-- ============================================================================
-- 04. PURCHASING & INVENTORY: purchase orders, receipts, serials, returns
-- ============================================================================

-- Module Inventory & Purchasing: purchase_orders, purchase_order_items, goods_receipts, receipt_items, product_serials, purchase_returns, purchase_return_items

CREATE TABLE purchase_orders (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    code                         VARCHAR(50)       NOT NULL,
    name                         VARCHAR(100)      NULL,
    supplier_id                  BIGINT            NOT NULL,
    order_date                   DATETIME(6)       NULL,
    expected_date                DATETIME(6)       NULL,
    status                       TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '0: draft, 1: pending, 2: approved, 3: received, 4: cancelled',
    description                  TEXT              NULL,
    total_amount                 BIGINT            NOT NULL DEFAULT 0,
    is_deleted                   TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '0: false, 1: true',
    created_by                   BIGINT            NULL,
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_by             BIGINT            NULL,
    last_modified_date           DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_po_code UNIQUE (code),
    CONSTRAINT fk_po_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (id),
    CONSTRAINT chk_purchase_orders_status CHECK (status BETWEEN 0 AND 4),
    CONSTRAINT chk_purchase_orders_is_deleted CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_po_supplier_deleted_date ON purchase_orders (supplier_id, is_deleted, order_date);
CREATE INDEX idx_po_deleted_status_date ON purchase_orders (is_deleted, status, order_date);

CREATE TABLE purchase_order_items (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    purchase_order_id            BIGINT            NOT NULL,
    product_id                   BIGINT            NOT NULL,
    quantity_order               INT               NOT NULL DEFAULT 0,
    unit_cost                    BIGINT            NOT NULL DEFAULT 0,
    quantity_received            INT               NOT NULL DEFAULT 0,
    subtotal                     BIGINT            NOT NULL DEFAULT 0,
    created_by                   BIGINT            NULL,
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_by             BIGINT            NULL,
    last_modified_date           DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_poi_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_poi_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_poi_order ON purchase_order_items (purchase_order_id);
CREATE INDEX idx_poi_product ON purchase_order_items (product_id);

CREATE TABLE goods_receipts (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    code                         VARCHAR(50)       NOT NULL,
    name                         VARCHAR(100)      NULL,
    purchase_order_id            BIGINT            NOT NULL,
    received_at                  DATETIME(6)       NULL,
    received_by                  BIGINT            NULL,
    status                       TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '0: pending, 1: confirmed, 2: rejected, 3: cancelled, 4: draft',
    created_by                   BIGINT            NULL,
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_by             BIGINT            NULL,
    last_modified_date           DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_gr_code UNIQUE (code),
    CONSTRAINT fk_gr_purchase_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders (id),
    CONSTRAINT fk_gr_received_by FOREIGN KEY (received_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT chk_goods_receipts_status CHECK (status BETWEEN 0 AND 4)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_gr_order_status ON goods_receipts (purchase_order_id, status);
CREATE INDEX idx_gr_received_by ON goods_receipts (received_by);
CREATE INDEX idx_gr_status_received ON goods_receipts (status, received_at);
CREATE INDEX idx_gr_creator_received ON goods_receipts (created_by, received_at);

CREATE TABLE receipt_items (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    goods_receipt_id             BIGINT            NOT NULL,
    purchase_order_item_id       BIGINT            NOT NULL,
    actual_unit_cost             BIGINT            NOT NULL DEFAULT 0,
    quantity_received            INT               NOT NULL DEFAULT 0,
    quantity_accepted            INT               NOT NULL DEFAULT 0,
    quantity_defective           INT               NOT NULL DEFAULT 0,
    created_by                   BIGINT            NULL,
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_by             BIGINT            NULL,
    last_modified_date           DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_ri_receipt FOREIGN KEY (goods_receipt_id) REFERENCES goods_receipts (id) ON DELETE CASCADE,
    CONSTRAINT fk_ri_poi FOREIGN KEY (purchase_order_item_id) REFERENCES purchase_order_items (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_ri_receipt ON receipt_items (goods_receipt_id);
CREATE INDEX idx_ri_poi ON receipt_items (purchase_order_item_id);

CREATE TABLE product_serials (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    product_id                   BIGINT            NOT NULL,
    receipt_item_id              BIGINT            NULL,
    serial_number                VARCHAR(100)      NOT NULL,
    status                       TINYINT UNSIGNED  NOT NULL DEFAULT 2 COMMENT '0: good_condition, 1: defective, 2: in_stock, 3: pending_return, 4: returning, 5: returned, 6: packing, 7: delivering, 8: sold, 9: using_by_technical',
    created_by                   BIGINT            NULL,
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_by             BIGINT            NULL,
    last_modified_date           DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    is_terminated                TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '0: false, 1: true',
    PRIMARY KEY (id),
    CONSTRAINT fk_ps_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_ps_receipt_item FOREIGN KEY (receipt_item_id) REFERENCES receipt_items (id) ON DELETE SET NULL,
    CONSTRAINT chk_product_serials_status CHECK (status BETWEEN 0 AND 9),
    CONSTRAINT chk_product_serials_is_terminated CHECK (is_terminated IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_ps_product_terminated_status ON product_serials (product_id, is_terminated, status);
CREATE INDEX idx_ps_receipt_item ON product_serials (receipt_item_id);

CREATE INDEX idx_ps_serial_terminated ON product_serials (serial_number, is_terminated);
CREATE INDEX idx_ps_serial_created ON product_serials (serial_number, created_date);

CREATE TABLE purchase_returns (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    code                         VARCHAR(50)       NOT NULL,
    name                         VARCHAR(255)      NULL,
    supplier_id                  BIGINT            NOT NULL,
    reason                       VARCHAR(255)      NULL,
    description                  VARCHAR(500)      NULL,
    return_date                  DATETIME(6)       NULL,
    total_amount                 BIGINT            NOT NULL DEFAULT 0,
    status                       TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '0: pending, 1: delivering, 2: returned, 3: cancelled, 4: draft',
    created_by                   BIGINT            NULL,
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_by             BIGINT            NULL,
    last_modified_date           DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_pr_code UNIQUE (code),
    CONSTRAINT fk_pr_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (id),
    CONSTRAINT chk_purchase_returns_status CHECK (status BETWEEN 0 AND 4)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_pr_supplier_status ON purchase_returns (supplier_id, status);
CREATE INDEX idx_pr_status_created ON purchase_returns (status, created_date);
CREATE INDEX idx_pr_creator_status ON purchase_returns (created_by, status);

CREATE TABLE purchase_return_items (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    purchase_return_id           BIGINT            NOT NULL,
    product_serial_id            BIGINT            NULL,
    unit_cost                    BIGINT            NOT NULL DEFAULT 0,
    created_by                   BIGINT            NULL,
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_by             BIGINT            NULL,
    last_modified_date           DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_pri_return FOREIGN KEY (purchase_return_id) REFERENCES purchase_returns (id) ON DELETE CASCADE,
    CONSTRAINT fk_pri_serial FOREIGN KEY (product_serial_id) REFERENCES product_serials (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_pri_return ON purchase_return_items (purchase_return_id);
CREATE INDEX idx_pri_serial ON purchase_return_items (product_serial_id);

-- ============================================================================
-- 05. SALES: carts, orders, order items, allocated serials
-- ============================================================================

-- Module Sales: carts, cart_items, orders, order_items, order_item_serial

CREATE TABLE carts (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    customer_id                  BIGINT            NOT NULL,
    quantity                     INT               NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_carts_customer UNIQUE (customer_id),
    CONSTRAINT fk_carts_customer FOREIGN KEY (customer_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE cart_items (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    cart_id                      BIGINT            NOT NULL,
    product_id                   BIGINT            NOT NULL,
    quantity                     INT               NOT NULL DEFAULT 1,
    unit_price                   BIGINT            NOT NULL DEFAULT 0,
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_date           DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_cart_items_cart_product UNIQUE (cart_id, product_id),
    CONSTRAINT fk_ci_cart FOREIGN KEY (cart_id) REFERENCES carts (id) ON DELETE CASCADE,
    CONSTRAINT fk_ci_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_ci_product ON cart_items (product_id);

CREATE TABLE orders (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    code                         VARCHAR(50)       NOT NULL,
    user_id                      BIGINT            NOT NULL,
    type                         TINYINT UNSIGNED  NOT NULL DEFAULT 1 COMMENT '0: in_store, 1: online',
    total_amount                 BIGINT            NOT NULL DEFAULT 0,
    discount_amount              BIGINT            NOT NULL DEFAULT 0,
    final_amount                 BIGINT            NOT NULL DEFAULT 0,
    status                       TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '0: pending, 1: confirmed, 2: rejected, 3: processing, 4: completed, 5: cancelled, 6: returned',
    address                      VARCHAR(500)      NULL,
    created_by                   BIGINT            NULL,
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_by             BIGINT            NULL,
    last_modified_date           DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_orders_code UNIQUE (code),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT chk_orders_type CHECK (type IN (0, 1)),
    CONSTRAINT chk_orders_status CHECK (status BETWEEN 0 AND 6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_orders_user ON orders (user_id);
CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_type ON orders (type);

CREATE TABLE order_items (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    order_id                     BIGINT            NOT NULL,
    product_id                   BIGINT            NOT NULL,
    quantity                     INT               NOT NULL DEFAULT 1,
    unit_price                   BIGINT            NOT NULL DEFAULT 0,
    discount_amount              BIGINT            NOT NULL DEFAULT 0,
    subtotal                     BIGINT            NOT NULL DEFAULT 0,
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_date           DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_oi_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_oi_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_oi_order ON order_items (order_id);
CREATE INDEX idx_oi_product ON order_items (product_id);

CREATE TABLE order_item_serial (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    order_item_id                BIGINT            NOT NULL,
    product_serial_id            BIGINT            NOT NULL,
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_ois_product_serial UNIQUE (product_serial_id),
    CONSTRAINT fk_ois_order_item FOREIGN KEY (order_item_id) REFERENCES order_items (id) ON DELETE CASCADE,
    CONSTRAINT fk_ois_product_serial FOREIGN KEY (product_serial_id) REFERENCES product_serials (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_ois_order_item ON order_item_serial (order_item_id);

-- ============================================================================
-- 06. BILLING & PAYMENTS
-- ============================================================================

-- Module Invoices & Payments: payment_methods, invoices, payment

CREATE TABLE payment_methods (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    code                         VARCHAR(50)       NOT NULL,
    name                         VARCHAR(255)      NOT NULL,
    type                         TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '0: cash, 1: bank_transfer',
    bank_name                    VARCHAR(255)      NULL,
    account_number               VARCHAR(100)      NULL,
    status                       TINYINT UNSIGNED  NOT NULL DEFAULT 1 COMMENT '0: inactive, 1: active',
    is_deleted                   TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '0: false, 1: true',
    created_by                   BIGINT            NULL,
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_by             BIGINT            NULL,
    last_modified_date           DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_pm_code UNIQUE (code),
    CONSTRAINT chk_payment_methods_type CHECK (type IN (0, 1)),
    CONSTRAINT chk_payment_methods_status CHECK (status BETWEEN 0 AND 1),
    CONSTRAINT chk_payment_methods_is_deleted CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_pm_status ON payment_methods (status);

CREATE TABLE invoices (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    code                         VARCHAR(50)       NOT NULL,
    order_id                     BIGINT            NOT NULL,
    invoice_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    total_amount                 BIGINT            NOT NULL DEFAULT 0,
    discount_amount              BIGINT            NOT NULL DEFAULT 0,
    tax_amount                   BIGINT            NOT NULL DEFAULT 0,
    amount                       BIGINT            NOT NULL DEFAULT 0,
    status                       TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '0: pending, 1: canceled, 2: paid',
    created_by                   BIGINT            NULL,
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_by             BIGINT            NULL,
    last_modified_date           DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_invoices_code UNIQUE (code),
    CONSTRAINT fk_invoices_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT chk_invoices_status CHECK (status BETWEEN 0 AND 2)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_invoices_order ON invoices (order_id);
CREATE INDEX idx_invoices_status ON invoices (status);

CREATE TABLE payment (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    order_id                     BIGINT            NOT NULL,
    payment_method_id            BIGINT            NOT NULL,
    amount                       BIGINT            NOT NULL DEFAULT 0,
    transaction_code             VARCHAR(255)      NULL,
    status                       TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '0: fail, 1: success',
    paid_at                      DATETIME(6)       NULL,
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_date           DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_payment_tx_code UNIQUE (transaction_code),
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_payment_method FOREIGN KEY (payment_method_id) REFERENCES payment_methods (id),
    CONSTRAINT chk_payment_status CHECK (status BETWEEN 0 AND 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_payment_order ON payment (order_id);
CREATE INDEX idx_payment_method ON payment (payment_method_id);
CREATE INDEX idx_payment_status ON payment (status);

-- ============================================================================
-- 07. WARRANTY & TECHNICAL SERVICE
-- ============================================================================

-- Module Warranty: warranty_policy, warranty, warranty_request, warranty_parts, warranty_part_serial

CREATE TABLE warranty_policy (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    name                         VARCHAR(255)      NOT NULL,
    duration_days                INT               NOT NULL DEFAULT 365,
    description                  VARCHAR(500)      NULL,
    target_type                  TINYINT UNSIGNED  NULL COMMENT '0: category, 1: product, 2: brand',
    target_id                    BIGINT            NULL,
    status                       TINYINT UNSIGNED  NOT NULL DEFAULT 1 COMMENT '0: inactive, 1: active',
    is_deleted                   TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '0: false, 1: true',
    created_by                   BIGINT            NULL,
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_by             BIGINT            NULL,
    last_modified_date           DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT chk_warranty_policy_target_type CHECK (target_type IN (0, 1, 2)),
    CONSTRAINT chk_warranty_policy_status CHECK (status BETWEEN 0 AND 1),
    CONSTRAINT chk_warranty_policy_is_deleted CHECK (is_deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_wp_target ON warranty_policy (target_type, target_id);
CREATE INDEX idx_wp_status ON warranty_policy (status);

CREATE TABLE warranty (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    order_item_serial_id         BIGINT            NULL,
    warranty_policy_id           BIGINT            NOT NULL,
    start_date                   DATETIME(6)       NOT NULL,
    end_date                     DATETIME(6)       NOT NULL,
    status                       TINYINT UNSIGNED  NOT NULL DEFAULT 1 COMMENT '0: inactive, 1: active',
    note                         VARCHAR(500)      NULL,
    created_by                   BIGINT            NULL,
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_by             BIGINT            NULL,
    last_modified_date           DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_warranty_ois FOREIGN KEY (order_item_serial_id) REFERENCES order_item_serial (id) ON DELETE SET NULL,
    CONSTRAINT fk_warranty_policy FOREIGN KEY (warranty_policy_id) REFERENCES warranty_policy (id),
    CONSTRAINT chk_warranty_status CHECK (status BETWEEN 0 AND 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_warranty_ois ON warranty (order_item_serial_id);
CREATE INDEX idx_warranty_policy ON warranty (warranty_policy_id);
CREATE INDEX idx_warranty_status ON warranty (status);

CREATE TABLE warranty_request (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    request_code                 VARCHAR(50)       NOT NULL,
    warranty_id                  BIGINT            NOT NULL,
    customer_id                  BIGINT            NOT NULL,
    technician_id                BIGINT            NULL,
    issue_description            VARCHAR(500)      NULL,
    inspection_notes             VARCHAR(500)      NULL,
    solution                     VARCHAR(500)      NULL,
    status                       TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '0: pending, 1: in_progress, 2: rejected, 3: completed',
    completed_at                 DATETIME(6)       NULL,
    created_by                   BIGINT            NULL,
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_by             BIGINT            NULL,
    last_modified_date           DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_wr_code UNIQUE (request_code),
    CONSTRAINT fk_wr_warranty FOREIGN KEY (warranty_id) REFERENCES warranty (id),
    CONSTRAINT fk_wr_customer FOREIGN KEY (customer_id) REFERENCES users (id),
    CONSTRAINT fk_wr_technician FOREIGN KEY (technician_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT chk_warranty_request_status CHECK (status BETWEEN 0 AND 3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_wr_warranty ON warranty_request (warranty_id);
CREATE INDEX idx_wr_customer ON warranty_request (customer_id);
CREATE INDEX idx_wr_technician ON warranty_request (technician_id);
CREATE INDEX idx_wr_status ON warranty_request (status);

CREATE TABLE warranty_parts (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    warranty_request_id          BIGINT            NOT NULL,
    product_id                   BIGINT            NOT NULL,
    quantity                     INT               NOT NULL DEFAULT 1,
    unit_cost                    BIGINT            NOT NULL DEFAULT 0,
    status                       TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '0: pending, 1: reserved, 2: installed, 3: returned, 4: cancelled',
    created_by                   BIGINT            NULL,
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_by             BIGINT            NULL,
    last_modified_date           DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_wp_request FOREIGN KEY (warranty_request_id) REFERENCES warranty_request (id) ON DELETE CASCADE,
    CONSTRAINT fk_wp_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT chk_warranty_parts_status CHECK (status BETWEEN 0 AND 4)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_wp_request ON warranty_parts (warranty_request_id);
CREATE INDEX idx_wp_product ON warranty_parts (product_id);
CREATE INDEX idx_wp_status ON warranty_parts (status);

CREATE TABLE warranty_part_serial (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    warranty_part_id             BIGINT            NOT NULL,
    product_serial_id            BIGINT            NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_wps_part FOREIGN KEY (warranty_part_id) REFERENCES warranty_parts (id) ON DELETE CASCADE,
    CONSTRAINT fk_wps_serial FOREIGN KEY (product_serial_id) REFERENCES product_serials (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_wps_part ON warranty_part_serial (warranty_part_id);
CREATE INDEX idx_wps_serial ON warranty_part_serial (product_serial_id);

-- ============================================================================
-- 08. DELIVERY, REVIEWS, COMMENTS & FAVORITES
-- ============================================================================

-- Module Delivery, Product Reviews, Comments & Favorites: delivery, product_reviews, product_comments, product_favorites

CREATE TABLE delivery (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    resource_type                TINYINT UNSIGNED  NOT NULL COMMENT '0: order, 1: purchase_return',
    resource_id                  BIGINT            NOT NULL,
    tracking_code                VARCHAR(100)      NULL,
    carrier_name                 VARCHAR(255)      NULL,
    shipping_fee                 BIGINT            NOT NULL DEFAULT 0,
    status                       TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '0: picked_up, 1: in_transit, 2: delivered, 3: returned',
    created_by                   BIGINT            NULL,
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_by             BIGINT            NULL,
    last_modified_date           DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_delivery_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_delivery_last_modified_by FOREIGN KEY (last_modified_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT chk_delivery_resource_type CHECK (resource_type IN (0, 1)),
    CONSTRAINT chk_delivery_status CHECK (status BETWEEN 0 AND 3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_delivery_resource ON delivery (resource_type, resource_id);
CREATE INDEX idx_delivery_tracking ON delivery (tracking_code);
CREATE INDEX idx_delivery_status ON delivery (status);


CREATE TABLE product_reviews (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    product_id                   BIGINT            NOT NULL,
    customer_id                  BIGINT            NOT NULL,
    rating                       TINYINT UNSIGNED  NOT NULL,
    content                      TEXT              NULL,
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_date           DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_pr_product_customer UNIQUE (product_id, customer_id),
    CONSTRAINT fk_pr_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_pr_customer FOREIGN KEY (customer_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_review_rating CHECK (rating BETWEEN 0 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE product_comments (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    product_id                   BIGINT            NOT NULL,
    created_by                  BIGINT            NOT NULL,
    parent_id                    BIGINT            NULL,
    content                      TEXT              NOT NULL,
    last_modified_by             BIGINT            NULL,
    is_deleted                   TINYINT(1)        NOT NULL DEFAULT 0,
    created_date                 DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_date           DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_pc_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_pc_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_pc_last_modified_by FOREIGN KEY (last_modified_by) REFERENCES users (id),
    CONSTRAINT fk_pc_parent FOREIGN KEY (parent_id) REFERENCES product_comments (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_pc_product_parent_created ON product_comments (product_id, parent_id, created_date);
CREATE INDEX idx_pc_created_by ON product_comments (created_by);
CREATE INDEX idx_pc_parent ON product_comments (parent_id);

CREATE TABLE product_favorites (
    id                           BIGINT            NOT NULL AUTO_INCREMENT,
    customer_id                  BIGINT            NOT NULL,
    product_id                   BIGINT            NOT NULL,
    created_at                   DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_pf_customer_product UNIQUE (customer_id, product_id),
    CONSTRAINT fk_pf_customer FOREIGN KEY (customer_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_pf_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_pf_product ON product_favorites (product_id);
