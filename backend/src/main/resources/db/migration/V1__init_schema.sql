-- ========================================================
-- V1__init_schema.sql
-- Factory Core - Indo American Equipment / Agrisep®
-- Phase 1: Core Foundation & Organization Baseline
-- ========================================================

-- 1. Sequential Number Generator Table (Row-level lock safe)
CREATE TABLE IF NOT EXISTS system_sequences (
    sequence_name VARCHAR(50) NOT NULL,
    current_value BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (sequence_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO system_sequences (sequence_name, current_value) 
VALUES ('PO_NUMBER', 0), ('DC_NUMBER', 0)
ON DUPLICATE KEY UPDATE current_value = current_value;

-- 2. Offices and Godowns
CREATE TABLE IF NOT EXISTS offices (
    id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(30) NOT NULL,
    state VARCHAR(50) NOT NULL,
    city VARCHAR(100) NOT NULL,
    address TEXT NOT NULL,
    gstin VARCHAR(15) NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    INDEX idx_offices_state (state),
    INDEX idx_offices_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3. Users and Credentials
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    role VARCHAR(30) NOT NULL,
    primary_office_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    failed_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP(3) NULL,
    last_login_at TIMESTAMP(3) NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    CONSTRAINT fk_users_primary_office FOREIGN KEY (primary_office_id) REFERENCES offices(id),
    INDEX idx_users_role (role),
    INDEX idx_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 4. User Multi-Office Permitted Access (State/Office Scoping)
CREATE TABLE IF NOT EXISTS user_allowed_offices (
    user_id VARCHAR(36) NOT NULL,
    office_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (user_id, office_id),
    CONSTRAINT fk_uao_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_uao_office FOREIGN KEY (office_id) REFERENCES offices(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
