-- ========================================================
-- V7__stock_shortages.sql
-- Factory Core - Indo American Equipment / Agrisep®
-- Phase 7 & 8: Stock Shortages & Priority Allocation
-- ========================================================

CREATE TABLE IF NOT EXISTS stock_shortages (
    id VARCHAR(36) NOT NULL,
    po_id VARCHAR(36) NOT NULL,
    po_item_id VARCHAR(36) NOT NULL,
    office_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    required_quantity INT NOT NULL,
    allocated_quantity INT NOT NULL DEFAULT 0,
    shortage_quantity INT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_shortage_po FOREIGN KEY (po_id) REFERENCES purchase_orders(id),
    CONSTRAINT fk_shortage_po_item FOREIGN KEY (po_item_id) REFERENCES po_items(id),
    CONSTRAINT fk_shortage_office FOREIGN KEY (office_id) REFERENCES offices(id),
    CONSTRAINT fk_shortage_product FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_shortage_po_id (po_id),
    INDEX idx_shortage_office_id (office_id),
    INDEX idx_shortage_product_id (product_id),
    INDEX idx_shortage_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
