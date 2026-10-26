-- ========================================================
-- V5__purchase_orders_and_versions.sql
-- Factory Core - Indo American Equipment / Agrisep®
-- Phase 5: Purchase Orders, Item Lines, and Audit Versioning
-- ========================================================

CREATE TABLE IF NOT EXISTS purchase_orders (
    id VARCHAR(36) NOT NULL,
    po_number VARCHAR(30) NOT NULL,
    client_id VARCHAR(36) NOT NULL,
    client_snapshot TEXT NOT NULL,
    office_id VARCHAR(36) NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    total_amount_inr DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    po_date DATE NOT NULL,
    expected_delivery_date DATE NOT NULL,
    notes TEXT NULL,
    rejection_reason TEXT NULL,
    cancellation_reason TEXT NULL,
    current_version INT NOT NULL DEFAULT 1,
    created_by VARCHAR(36) NOT NULL,
    approved_by VARCHAR(36) NULL,
    approved_at TIMESTAMP(3) NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_po_number (po_number),
    CONSTRAINT fk_po_client FOREIGN KEY (client_id) REFERENCES clients(id),
    CONSTRAINT fk_po_office FOREIGN KEY (office_id) REFERENCES offices(id),
    INDEX idx_po_client_id (client_id),
    INDEX idx_po_office_id (office_id),
    INDEX idx_po_status (status),
    INDEX idx_po_date (po_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS po_items (
    id VARCHAR(36) NOT NULL,
    po_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    product_snapshot TEXT NOT NULL,
    quantity INT NOT NULL,
    unit_price_inr DECIMAL(15, 2) NOT NULL,
    line_total_inr DECIMAL(15, 2) NOT NULL,
    reserved_quantity INT NOT NULL DEFAULT 0,
    dispatched_quantity INT NOT NULL DEFAULT 0,
    delivered_quantity INT NOT NULL DEFAULT 0,
    expected_delivery_date DATE NULL,
    notes TEXT NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_po_items_po FOREIGN KEY (po_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_po_items_product FOREIGN KEY (product_id) REFERENCES products(id),
    UNIQUE KEY uk_po_items_po_product (po_id, product_id),
    INDEX idx_po_items_po_id (po_id),
    INDEX idx_po_items_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS po_versions (
    id VARCHAR(36) NOT NULL,
    po_id VARCHAR(36) NOT NULL,
    version_number INT NOT NULL,
    changed_by VARCHAR(36) NOT NULL,
    changed_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    change_summary VARCHAR(255) NOT NULL,
    snapshot_data LONGTEXT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_po_versions_po FOREIGN KEY (po_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
    UNIQUE KEY uk_po_versions_po_version (po_id, version_number),
    INDEX idx_po_versions_po_id (po_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS po_version_changes (
    id VARCHAR(36) NOT NULL,
    version_id VARCHAR(36) NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    previous_value TEXT NULL,
    new_value TEXT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_po_changes_version FOREIGN KEY (version_id) REFERENCES po_versions(id) ON DELETE CASCADE,
    INDEX idx_po_changes_version_id (version_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
