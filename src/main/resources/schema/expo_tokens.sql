-- Expo Push Token 테이블 생성
CREATE TABLE IF NOT EXISTS expo_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    expo_push_token VARCHAR(255) NOT NULL UNIQUE,
    device_id VARCHAR(255) NOT NULL,
    platform VARCHAR(20),
    app_version VARCHAR(20),
    active BOOLEAN DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_expo_token_user (user_id),
    INDEX idx_expo_token_device (device_id),
    INDEX idx_expo_token_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 기존 FCM 토큰 테이블이 없다면 생성
CREATE TABLE IF NOT EXISTS fcm_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    app_id VARCHAR(255),
    user_id VARCHAR(255) NOT NULL,
    device_id VARCHAR(255) NOT NULL,
    fcm_token VARCHAR(500) NOT NULL,
    use_at VARCHAR(1) DEFAULT 'Y',
    reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP,
    device_type VARCHAR(20),
    INDEX idx_fcm_token_user (user_id),
    INDEX idx_fcm_token_device (device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
