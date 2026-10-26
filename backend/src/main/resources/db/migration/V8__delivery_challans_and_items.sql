-- ========================================================
-- V8__delivery_challans_and_items.sql
-- Factory Core - Indo American Equipment / Agrisep®
-- Phase 9 & 10: Delivery Challans, Dispatch & Delivery
-- ========================================================

CREATE TABLE IF NOT EXISTS delivery_challans (
    id VARCHAR(36) NOT NULL,
    dc_number VARCHAR(30) NOT NULL,
    purchase_order_id VARCHAR(36) NOT NULL,
    client_id VARCHAR(36) NOT NULL,
    office_id VARCHAR(36) NOT NULL,
    delivery_address_id VARCHAR(36) NULL,
    delivery_address_snapshot TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    vehicle_number VARCHAR(30) NOT NULL,
    driver_name VARCHAR(100) NOT NULL,
    driver_phone VARCHAR(20) NOT NULL,
    transporter_name VARCHAR(150) NOT NULL,
    lr_number VARCHAR(50) NULL,
    eway_bill_number VARCHAR(20) NULL,
    eway_bill_date DATE NULL,
    challan_date DATE NOT NULL,
    dispatched_at TIMESTAMP(3) NULL,
    dispatched_by VARCHAR(36) NULL,
    delivered_at TIMESTAMP(3) NULL,
    delivery_confirmed_by VARCHAR(36) NULL,
    recipient_name VARCHAR(100) NULL,
    recipient_phone VARCHAR(20) NULL,
    proof_of_delivery_url VARCHAR(255) NULL,
    notes TEXT NULL,
    created_by VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_dc_number (dc_number),
    CONSTRAINT fk_dc_po FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id),
    CONSTRAINT fk_dc_client FOREIGN KEY (client_id) REFERENCES clients(id),
    CONSTRAINT fk_dc_office FOREIGN KEY (office_id) REFERENCES offices(id),
    INDEX idx_dc_po (purchase_order_id),
    INDEX idx_dc_client (client_id),
    INDEX idx_dc_office (office_id),
    INDEX idx_dc_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS dc_items (
    id VARCHAR(36) NOT NULL,
    delivery_challan_id VARCHAR(36) NOT NULL,
    po_item_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    product_snapshot TEXT NOT NULL,
    quantity INT NOT NULL,
    notes TEXT NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_dc_items_challan FOREIGN KEY (delivery_challan_id) REFERENCES delivery_challans(id),
    CONSTRAINT fk_dc_items_po_item FOREIGN KEY (po_item_id) REFERENCES po_items(id),
    CONSTRAINT fk_dc_items_product FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_dc_items_challan (delivery_challan_id),
    INDEX idx_dc_items_po_item (po_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
