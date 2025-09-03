-- V3__security_and_audit_enhancements.sql
-- Security and auditing enhancements for inventory_saas database
-- Generated: 2025-09-03
-- Enhancements:
-- 1. User security fields (password history, login attempts, account status)
-- 2. Employee code field for user accounts
-- 3. Password policy configuration
-- 4. Audit logging tables
-- 5. Session management tables

SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
SET time_zone = '+00:00';

-- Enhance user_account table with security fields
ALTER TABLE user_account 
ADD COLUMN employee_code VARCHAR(32) NULL AFTER email,
ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0 AFTER password_hash,
ADD COLUMN account_locked_until TIMESTAMP NULL AFTER failed_login_attempts,
ADD COLUMN last_login_at TIMESTAMP NULL AFTER account_locked_until,
ADD COLUMN password_expires_at TIMESTAMP NULL AFTER last_login_at,
ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE AFTER password_expires_at,
ADD COLUMN created_by BIGINT NULL AFTER must_change_password,
ADD COLUMN updated_by BIGINT NULL AFTER created_by,
ADD INDEX idx_user_employee_code (tenant_id, employee_code),
ADD INDEX idx_user_account_locked (account_locked_until),
ADD INDEX idx_user_password_expires (password_expires_at);

-- Add foreign key constraints for created_by and updated_by (self-referencing)
ALTER TABLE user_account
ADD CONSTRAINT fk_user_created_by FOREIGN KEY (created_by) REFERENCES user_account(id) ON DELETE SET NULL,
ADD CONSTRAINT fk_user_updated_by FOREIGN KEY (updated_by) REFERENCES user_account(id) ON DELETE SET NULL;

-- User password history table
CREATE TABLE user_password_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    password_hash VARBINARY(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pwd_hist_user FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE,
    INDEX idx_pwd_hist_user_date (user_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- System configuration table for password policies and other settings
CREATE TABLE system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(64) NOT NULL UNIQUE,
    config_value VARCHAR(512) NOT NULL,
    description VARCHAR(255) NULL,
    updated_by BIGINT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_config_updated_by FOREIGN KEY (updated_by) REFERENCES user_account(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Login attempt tracking
CREATE TABLE login_attempts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(320) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    user_agent VARCHAR(512) NULL,
    success BOOLEAN NOT NULL,
    failure_reason VARCHAR(100) NULL,
    attempted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_login_attempts_email_date (email, attempted_at DESC),
    INDEX idx_login_attempts_ip_date (ip_address, attempted_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- User session management
CREATE TABLE user_sessions (
    id VARCHAR(128) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    user_agent VARCHAR(512) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_accessed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE,
    CONSTRAINT fk_session_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
    INDEX idx_user_sessions_user (user_id),
    INDEX idx_user_sessions_expires (expires_at),
    INDEX idx_user_sessions_active (is_active, last_accessed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Audit log table for tracking all user actions
-- Note: use VARCHAR for action_type so it maps to JPA EnumType.STRING
CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NULL,
    session_id VARCHAR(128) NULL,
    action_type VARCHAR(32) NOT NULL,
    entity_type VARCHAR(64) NOT NULL,
    entity_id VARCHAR(64) NULL,
    old_values JSON NULL,
    new_values JSON NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(512) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE SET NULL,
    INDEX idx_audit_tenant_date (tenant_id, created_at DESC),
    INDEX idx_audit_user_date (user_id, created_at DESC),
    INDEX idx_audit_entity (entity_type, entity_id),
    INDEX idx_audit_action_date (action_type, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Insert default system configuration values
INSERT INTO system_config (config_key, config_value, description) VALUES
('password.expiry.days', '60', 'Number of days before password expires'),
('password.min.length', '8', 'Minimum password length'),
('password.require.uppercase', 'true', 'Require at least one uppercase letter'),
('password.require.lowercase', 'true', 'Require at least one lowercase letter'),
('password.require.digit', 'true', 'Require at least one digit'),
('password.require.special', 'true', 'Require at least one special character'),
('password.history.count', '3', 'Number of previous passwords to remember'),
('login.max.attempts', '5', 'Maximum failed login attempts before lockout'),
('login.lockout.minutes', '30', 'Account lockout duration in minutes'),
('session.timeout.minutes', '480', 'Session timeout in minutes (8 hours)'),
('password.reset.token.expiry.hours', '24', 'Password reset token expiry in hours');

-- Create a view for active user accounts with role information
CREATE OR REPLACE VIEW v_active_users AS
SELECT 
    ua.id,
    ua.tenant_id,
    ua.email,
    ua.employee_code,
    ua.display_name,
    ua.status,
    ua.failed_login_attempts,
    ua.account_locked_until,
    ua.last_login_at,
    ua.password_expires_at,
    ua.must_change_password,
    ua.created_at,
    ua.updated_at,
    GROUP_CONCAT(r.name ORDER BY r.name SEPARATOR ', ') as roles,
    GROUP_CONCAT(r.code ORDER BY r.code SEPARATOR ', ') as role_codes
FROM user_account ua
LEFT JOIN user_role ur ON ua.id = ur.user_id
LEFT JOIN role r ON ur.role_id = r.id
WHERE ua.status = 'ACTIVE'
GROUP BY ua.id, ua.tenant_id, ua.email, ua.employee_code, ua.display_name, 
         ua.status, ua.failed_login_attempts, ua.account_locked_until, 
         ua.last_login_at, ua.password_expires_at, ua.must_change_password,
         ua.created_at, ua.updated_at;

-- Create a view for password expiry monitoring
CREATE OR REPLACE VIEW v_password_expiry_monitor AS
SELECT 
    ua.id,
    ua.tenant_id,
    ua.email,
    ua.employee_code,
    ua.display_name,
    ua.password_expires_at,
    ua.must_change_password,
    CASE 
        WHEN ua.password_expires_at IS NULL THEN NULL
        WHEN ua.password_expires_at <= NOW() THEN 'EXPIRED'
        WHEN ua.password_expires_at <= DATE_ADD(NOW(), INTERVAL 7 DAY) THEN 'EXPIRING_SOON'
        ELSE 'VALID'
    END as password_status,
    CASE 
        WHEN ua.password_expires_at IS NULL THEN NULL
        ELSE DATEDIFF(ua.password_expires_at, NOW())
    END as days_until_expiry
FROM user_account ua
WHERE ua.status = 'ACTIVE'
ORDER BY ua.password_expires_at ASC;

-- Update the existing sample data to include employee codes
UPDATE user_account 
SET employee_code = CASE 
    WHEN email = 'admin@demo.example' THEN 'EMP001'
    WHEN email = 'manager@demo.example' THEN 'EMP002'
    ELSE CONCAT('EMP', LPAD(id, 3, '0'))
END,
password_expires_at = DATE_ADD(NOW(), INTERVAL 60 DAY),
created_by = 1,
updated_by = 1
WHERE tenant_id = 1;