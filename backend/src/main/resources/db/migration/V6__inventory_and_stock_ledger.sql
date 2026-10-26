-- ========================================================
-- V6__inventory_and_stock_ledger.sql
-- Factory Core - Indo American Equipment / Agrisep®
-- Phase 6: Inventory Management & Append-Only Stock Ledger
-- ========================================================

-- 1. Inventory Table
CREATE TABLE IF NOT EXISTS inventory (
    id VARCHAR(36) NOT NULL,
    office_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    on_hand_quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    available_quantity INT NOT NULL DEFAULT 0,
    reorder_threshold INT NOT NULL DEFAULT 2,
    last_adjusted_at TIMESTAMP(3) NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_inventory_office_product (office_id, product_id),
    CONSTRAINT fk_inventory_office FOREIGN KEY (office_id) REFERENCES offices(id),
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT chk_inv_on_hand CHECK (on_hand_quantity >= 0),
    CONSTRAINT chk_inv_reserved CHECK (reserved_quantity >= 0),
    CONSTRAINT chk_inv_reserved_lte CHECK (reserved_quantity <= on_hand_quantity),
    CONSTRAINT chk_inv_available CHECK (available_quantity = on_hand_quantity - reserved_quantity),
    INDEX idx_inventory_office (office_id),
    INDEX idx_inventory_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 2. Append-Only Stock Ledger Table
CREATE TABLE IF NOT EXISTS stock_ledger (
    id VARCHAR(36) NOT NULL,
    inventory_id VARCHAR(36) NOT NULL,
    office_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    quantity_delta INT NOT NULL,
    on_hand_before INT NOT NULL,
    on_hand_after INT NOT NULL,
    reserved_before INT NOT NULL,
    reserved_after INT NOT NULL,
    available_before INT NOT NULL,
    available_after INT NOT NULL,
    reference_id VARCHAR(100) NULL,
    reference_type VARCHAR(50) NULL,
    notes TEXT NULL,
    created_by VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_ledger_inventory FOREIGN KEY (inventory_id) REFERENCES inventory(id),
    CONSTRAINT fk_ledger_office FOREIGN KEY (office_id) REFERENCES offices(id),
    CONSTRAINT fk_ledger_product FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_ledger_inventory (inventory_id),
    INDEX idx_ledger_office (office_id),
    INDEX idx_ledger_product (product_id),
    INDEX idx_ledger_tx_type (transaction_type),
    INDEX idx_ledger_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
