-- 操作日志表
CREATE TABLE IF NOT EXISTS t_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    operation VARCHAR(100) NOT NULL,
    module VARCHAR(50),
    description VARCHAR(500),
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    request_method VARCHAR(10),
    request_url VARCHAR(500),
    request_params TEXT,
    response_status INT,
    execution_time BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 备份记录表
CREATE TABLE IF NOT EXISTS t_backup (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'full',
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    file_path VARCHAR(500),
    file_size BIGINT,
    photo_count INT DEFAULT 0,
    album_count INT DEFAULT 0,
    tag_count INT DEFAULT 0,
    started_at DATETIME,
    completed_at DATETIME,
    error_message TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 添加照片保护字段
ALTER TABLE t_photo ADD COLUMN IF NOT EXISTS is_private INT DEFAULT 0;
ALTER TABLE t_photo ADD COLUMN IF NOT EXISTS private_password VARCHAR(255);
