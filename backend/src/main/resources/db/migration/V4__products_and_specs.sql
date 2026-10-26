-- ========================================================
-- V4__products_and_specs.sql
-- Factory Core - Indo American Equipment / Agrisep®
-- Phase 4: Machinery Master Catalog & Engineering Specs
-- ========================================================

CREATE TABLE IF NOT EXISTS products (
    id VARCHAR(36) NOT NULL,
    sku VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    unit VARCHAR(20) NOT NULL DEFAULT 'UNIT',
    description TEXT NULL,
    base_price_inr DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    lead_time_weeks INT NOT NULL DEFAULT 4,
    internal_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    website_visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    brochure_url VARCHAR(255) NULL,
    is_locked_for_sku_change BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_products_sku (sku),
    INDEX idx_products_category (category),
    INDEX idx_products_internal_status (internal_status),
    INDEX idx_products_website_visibility (website_visibility)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS product_specs (
    id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    capacity VARCHAR(100) NOT NULL,
    motor_power VARCHAR(100) NOT NULL,
    motor_rating VARCHAR(100) NULL,
    dimensions VARCHAR(100) NOT NULL,
    weight VARCHAR(50) NOT NULL,
    air_volume VARCHAR(100) NULL,
    screen_area VARCHAR(100) NULL,
    seed_compatibility TEXT NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_product_specs_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY uk_product_specs_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
