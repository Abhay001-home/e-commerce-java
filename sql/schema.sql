-- ============================================================
-- E-Commerce Platform — MySQL Schema
-- Version: 1.0.0
-- ============================================================

CREATE DATABASE IF NOT EXISTS ecommerce_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ecommerce_db;

-- ============================================================
-- PHASE 1: Users, Roles, Addresses
-- ============================================================

CREATE TABLE roles (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,    -- ROLE_USER, ROLE_ADMIN
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,      -- BCrypt hashed
    phone           VARCHAR(20),
    profile_image   VARCHAR(500),
    is_active       BOOLEAN DEFAULT TRUE,
    is_verified     BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (email)
) ENGINE=InnoDB;

CREATE TABLE user_roles (
    user_id     BIGINT NOT NULL,
    role_id     BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE addresses (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    address_type    ENUM('HOME','OFFICE','OTHER') DEFAULT 'HOME',
    full_name       VARCHAR(200) NOT NULL,
    phone           VARCHAR(20) NOT NULL,
    street          VARCHAR(500) NOT NULL,
    city            VARCHAR(100) NOT NULL,
    state           VARCHAR(100) NOT NULL,
    zip_code        VARCHAR(20) NOT NULL,
    country         VARCHAR(100) NOT NULL DEFAULT 'India',
    is_default      BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_addresses_user (user_id)
) ENGINE=InnoDB;

-- ============================================================
-- PHASE 2: Categories, Brands, Products, Inventory
-- ============================================================

CREATE TABLE categories (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    slug        VARCHAR(200) NOT NULL UNIQUE,
    description TEXT,
    image_url   VARCHAR(500),
    parent_id   BIGINT,
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_categories_slug (slug),
    INDEX idx_categories_parent (parent_id)
) ENGINE=InnoDB;

CREATE TABLE brands (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(200) NOT NULL UNIQUE,
    slug        VARCHAR(200) NOT NULL UNIQUE,
    description TEXT,
    logo_url    VARCHAR(500),
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE products (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(500) NOT NULL,
    slug            VARCHAR(500) NOT NULL UNIQUE,
    description     TEXT,
    short_desc      VARCHAR(1000),
    sku             VARCHAR(100) UNIQUE,
    price           DECIMAL(10,2) NOT NULL,
    mrp             DECIMAL(10,2),               -- Maximum Retail Price
    discount_pct    DECIMAL(5,2) DEFAULT 0.0,    -- % discount
    category_id     BIGINT,
    brand_id        BIGINT,
    is_active       BOOLEAN DEFAULT TRUE,
    is_featured     BOOLEAN DEFAULT FALSE,
    avg_rating      DECIMAL(3,2) DEFAULT 0.0,
    review_count    INT DEFAULT 0,
    sold_count      INT DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (brand_id) REFERENCES brands(id) ON DELETE SET NULL,
    INDEX idx_products_category (category_id),
    INDEX idx_products_brand (brand_id),
    INDEX idx_products_price (price),
    INDEX idx_products_slug (slug),
    FULLTEXT INDEX ft_products_search (name, description, short_desc)
) ENGINE=InnoDB;

CREATE TABLE product_images (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id  BIGINT NOT NULL,
    image_url   VARCHAR(500) NOT NULL,
    alt_text    VARCHAR(200),
    is_primary  BOOLEAN DEFAULT FALSE,
    sort_order  INT DEFAULT 0,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_product_images_product (product_id)
) ENGINE=InnoDB;

CREATE TABLE product_variants (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id      BIGINT NOT NULL,
    variant_name    VARCHAR(200) NOT NULL,   -- e.g. "Size: L | Color: Red"
    sku             VARCHAR(100) UNIQUE,
    price           DECIMAL(10,2),           -- override product price if set
    mrp             DECIMAL(10,2),
    image_url       VARCHAR(500),
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_variants_product (product_id)
) ENGINE=InnoDB;

CREATE TABLE product_specifications (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id      BIGINT NOT NULL,
    spec_key        VARCHAR(200) NOT NULL,
    spec_value      VARCHAR(500) NOT NULL,
    sort_order      INT DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_specs_product (product_id)
) ENGINE=InnoDB;

CREATE TABLE inventory (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id      BIGINT NOT NULL UNIQUE,
    variant_id      BIGINT UNIQUE,
    quantity        INT NOT NULL DEFAULT 0,
    low_stock_qty   INT DEFAULT 10,          -- alert threshold
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- PHASE 3: Cart, Wishlist, Coupons
-- ============================================================

CREATE TABLE coupons (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    code                VARCHAR(50) NOT NULL UNIQUE,
    description         VARCHAR(500),
    discount_type       ENUM('PERCENTAGE','FIXED') NOT NULL,
    discount_value      DECIMAL(10,2) NOT NULL,
    min_order_amount    DECIMAL(10,2) DEFAULT 0.0,
    max_discount_amount DECIMAL(10,2),          -- cap for % discounts
    usage_limit         INT,                    -- total uses allowed
    used_count          INT DEFAULT 0,
    valid_from          TIMESTAMP NOT NULL,
    valid_until         TIMESTAMP NOT NULL,
    is_active           BOOLEAN DEFAULT TRUE,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_coupons_code (code)
) ENGINE=InnoDB;

CREATE TABLE carts (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL UNIQUE,         -- one cart per user
    coupon_id   BIGINT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE cart_items (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id         BIGINT NOT NULL,
    product_id      BIGINT NOT NULL,
    variant_id      BIGINT,
    quantity        INT NOT NULL DEFAULT 1,
    saved_for_later BOOLEAN DEFAULT FALSE,
    added_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE SET NULL,
    UNIQUE KEY uq_cart_item (cart_id, product_id, variant_id),
    INDEX idx_cart_items_cart (cart_id)
) ENGINE=InnoDB;

CREATE TABLE wishlists (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL UNIQUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE wishlist_items (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    wishlist_id BIGINT NOT NULL,
    product_id  BIGINT NOT NULL,
    added_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (wishlist_id) REFERENCES wishlists(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY uq_wishlist_item (wishlist_id, product_id)
) ENGINE=InnoDB;

-- ============================================================
-- PHASE 4: Orders, Payments, Shipments
-- ============================================================

CREATE TABLE orders (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number        VARCHAR(50) NOT NULL UNIQUE,    -- ORD-20240101-0001
    user_id             BIGINT NOT NULL,
    status              ENUM('PENDING','CONFIRMED','PACKED','SHIPPED',
                             'OUT_FOR_DELIVERY','DELIVERED','CANCELLED','RETURNED')
                        NOT NULL DEFAULT 'PENDING',
    coupon_id           BIGINT,
    sub_total           DECIMAL(10,2) NOT NULL,
    discount_amount     DECIMAL(10,2) DEFAULT 0.0,
    tax_amount          DECIMAL(10,2) DEFAULT 0.0,
    shipping_amount     DECIMAL(10,2) DEFAULT 0.0,
    total_amount        DECIMAL(10,2) NOT NULL,
    billing_address_id  BIGINT,
    shipping_address_id BIGINT,
    notes               TEXT,
    placed_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE SET NULL,
    FOREIGN KEY (billing_address_id) REFERENCES addresses(id) ON DELETE SET NULL,
    FOREIGN KEY (shipping_address_id) REFERENCES addresses(id) ON DELETE SET NULL,
    INDEX idx_orders_user (user_id),
    INDEX idx_orders_status (status),
    INDEX idx_orders_placed (placed_at)
) ENGINE=InnoDB;

CREATE TABLE order_items (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT NOT NULL,
    product_id      BIGINT NOT NULL,
    variant_id      BIGINT,
    product_name    VARCHAR(500) NOT NULL,       -- snapshot at order time
    product_image   VARCHAR(500),
    variant_name    VARCHAR(200),
    unit_price      DECIMAL(10,2) NOT NULL,      -- price at order time
    quantity        INT NOT NULL,
    sub_total       DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    INDEX idx_order_items_order (order_id)
) ENGINE=InnoDB;

CREATE TABLE payments (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT NOT NULL UNIQUE,
    payment_method  ENUM('CASH_ON_DELIVERY','ONLINE') NOT NULL,
    payment_status  ENUM('PENDING','COMPLETED','FAILED','REFUNDED') DEFAULT 'PENDING',
    amount          DECIMAL(10,2) NOT NULL,
    transaction_id  VARCHAR(200),
    payment_date    TIMESTAMP,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE shipments (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id            BIGINT NOT NULL UNIQUE,
    tracking_number     VARCHAR(200),
    carrier             VARCHAR(100),
    estimated_delivery  DATE,
    shipped_at          TIMESTAMP,
    delivered_at        TIMESTAMP,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE order_status_history (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id    BIGINT NOT NULL,
    status      ENUM('PENDING','CONFIRMED','PACKED','SHIPPED',
                     'OUT_FOR_DELIVERY','DELIVERED','CANCELLED','RETURNED') NOT NULL,
    notes       VARCHAR(500),
    changed_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    changed_by  BIGINT,                         -- user_id who changed it
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_history_order (order_id)
) ENGINE=InnoDB;

-- ============================================================
-- PHASE 5: Reviews
-- ============================================================

CREATE TABLE reviews (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id  BIGINT NOT NULL,
    user_id     BIGINT NOT NULL,
    order_id    BIGINT,                 -- must have purchased to review
    rating      TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title       VARCHAR(300),
    body        TEXT,
    is_verified BOOLEAN DEFAULT FALSE,  -- verified purchase
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL,
    UNIQUE KEY uq_review (product_id, user_id),
    INDEX idx_reviews_product (product_id),
    INDEX idx_reviews_rating (rating)
) ENGINE=InnoDB;

-- ============================================================
-- SEED DATA
-- ============================================================

INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');

-- Admin user (password: Admin@123)
INSERT INTO users (first_name, last_name, email, password, is_active, is_verified)
VALUES ('Admin', 'User', 'admin@ecommerce.com',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/Lewc9vODEKbN...',
        TRUE, TRUE);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email='admin@ecommerce.com' AND r.name='ROLE_ADMIN';

-- Sample categories
INSERT INTO categories (name, slug, description) VALUES
    ('Electronics', 'electronics', 'Electronic devices and accessories'),
    ('Fashion', 'fashion', 'Clothing, footwear and accessories'),
    ('Home & Kitchen', 'home-kitchen', 'Home appliances and kitchen essentials'),
    ('Books', 'books', 'Books, magazines and stationery'),
    ('Sports', 'sports', 'Sports and fitness equipment');

-- Sample brands
INSERT INTO brands (name, slug) VALUES
    ('Samsung', 'samsung'),
    ('Apple', 'apple'),
    ('Nike', 'nike'),
    ('Sony', 'sony'),
    ('Adidas', 'adidas');
