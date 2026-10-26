-- ========================================================
-- V2__login_history.sql
-- Factory Core - Indo American Equipment / Agrisep®
-- Phase 2: Login History Tracking
-- ========================================================

CREATE TABLE IF NOT EXISTS login_history (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NULL,
    email VARCHAR(100) NOT NULL,
    login_status VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(255) NULL,
    timestamp TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    INDEX idx_login_history_user (user_id),
    INDEX idx_login_history_time (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
