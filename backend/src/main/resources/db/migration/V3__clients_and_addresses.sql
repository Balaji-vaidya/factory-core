-- ========================================================
-- V3__clients_and_addresses.sql
-- Factory Core - Indo American Equipment / Agrisep®
-- Phase 3: Client Management & Delivery Sites
-- ========================================================

CREATE TABLE IF NOT EXISTS clients (
    id VARCHAR(36) NOT NULL,
    company_name VARCHAR(150) NOT NULL,
    client_type VARCHAR(30) NOT NULL,
    gstin VARCHAR(15) NULL,
    pan VARCHAR(10) NULL,
    state VARCHAR(50) NOT NULL,
    primary_contact_name VARCHAR(100) NOT NULL,
    primary_contact_designation VARCHAR(100) NULL DEFAULT 'Procurement Officer',
    primary_contact_phone VARCHAR(20) NOT NULL,
    primary_contact_email VARCHAR(100) NOT NULL,
    billing_contact_name VARCHAR(100) NULL,
    billing_contact_designation VARCHAR(100) NULL,
    billing_contact_phone VARCHAR(20) NULL,
    billing_contact_email VARCHAR(100) NULL,
    credit_days INT NOT NULL DEFAULT 30,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_by VARCHAR(36) NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_clients_gstin (gstin),
    INDEX idx_clients_company_name (company_name),
    INDEX idx_clients_state (state),
    INDEX idx_clients_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS client_addresses (
    id VARCHAR(36) NOT NULL,
    client_id VARCHAR(36) NOT NULL,
    site_name VARCHAR(100) NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(50) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    contact_person VARCHAR(100) NULL,
    phone VARCHAR(20) NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_client_addresses_client FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    INDEX idx_client_addresses_client_id (client_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
