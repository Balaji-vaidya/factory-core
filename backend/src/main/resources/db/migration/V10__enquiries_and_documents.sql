-- ========================================================
-- V10__enquiries_and_documents.sql
-- Factory Core - Indo American Equipment / Agrisep®
-- Phase 13: Document Storage & Website Enquiry Management
-- ========================================================

CREATE TABLE IF NOT EXISTS website_enquiries (
    id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    company_name VARCHAR(150) NULL,
    state VARCHAR(50) NULL,
    message TEXT NOT NULL,
    interested_products TEXT NULL,
    source VARCHAR(50) NOT NULL DEFAULT 'WEBSITE_CONTACT',
    status VARCHAR(30) NOT NULL DEFAULT 'NEW',
    internal_notes TEXT NULL,
    converted_client_id VARCHAR(36) NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    INDEX idx_enquiry_status (status),
    INDEX idx_enquiry_email (email),
    INDEX idx_enquiry_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS documents (
    id VARCHAR(36) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(100) NOT NULL,
    uploaded_by VARCHAR(100) NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    INDEX idx_doc_entity (entity_type, entity_id),
    INDEX idx_doc_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
