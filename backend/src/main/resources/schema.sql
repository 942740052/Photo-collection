-- 创建数据库
CREATE DATABASE IF NOT EXISTS photo_message DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE photo_message;

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像URL',
    role VARCHAR(20) DEFAULT 'USER' COMMENT '角色：ADMIN/USER',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_login_at DATETIME COMMENT '最后登录时间',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 照片表
CREATE TABLE IF NOT EXISTS t_photo (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '照片ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    album_id BIGINT COMMENT '相册ID',
    title VARCHAR(200) COMMENT '照片标题',
    description TEXT COMMENT '照片描述',
    file_path VARCHAR(500) NOT NULL COMMENT '原始文件路径',
    thumbnail_path VARCHAR(500) COMMENT '缩略图路径',
    file_size BIGINT COMMENT '文件大小（字节）',
    file_type VARCHAR(20) COMMENT '文件类型',
    width INT COMMENT '图片宽度',
    height INT COMMENT '图片高度',
    shoot_time DATETIME COMMENT '拍摄时间',
    camera_model VARCHAR(100) COMMENT '相机型号',
    gps_latitude DECIMAL(10,7) COMMENT 'GPS纬度',
    gps_longitude DECIMAL(10,7) COMMENT 'GPS经度',
    is_favorite TINYINT DEFAULT 0 COMMENT '是否收藏：0-否，1-是',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_album_id (album_id),
    INDEX idx_shoot_time (shoot_time),
    INDEX idx_is_favorite (is_favorite),
    INDEX idx_is_deleted (is_deleted),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='照片表';

-- 相册表
CREATE TABLE IF NOT EXISTS t_album (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '相册ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(100) NOT NULL COMMENT '相册名称',
    description TEXT COMMENT '相册描述',
    cover_photo_id BIGINT COMMENT '封面照片ID',
    photo_count INT DEFAULT 0 COMMENT '照片数量',
    is_public TINYINT DEFAULT 0 COMMENT '是否公开：0-否，1-是',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_is_public (is_public)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='相册表';

-- 标签表
CREATE TABLE IF NOT EXISTS t_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(50) NOT NULL COMMENT '标签名称',
    color VARCHAR(20) DEFAULT '#409EFF' COMMENT '标签颜色',
    photo_count INT DEFAULT 0 COMMENT '照片数量',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    UNIQUE KEY uk_user_name (user_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- 照片标签关联表
CREATE TABLE IF NOT EXISTS t_photo_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    photo_id BIGINT NOT NULL COMMENT '照片ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_photo_id (photo_id),
    INDEX idx_tag_id (tag_id),
    UNIQUE KEY uk_photo_tag (photo_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='照片标签关联表';

-- 照片元数据表
CREATE TABLE IF NOT EXISTS t_photo_metadata (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    photo_id BIGINT NOT NULL UNIQUE COMMENT '照片ID',
    iso INT COMMENT 'ISO感光度',
    aperture VARCHAR(20) COMMENT '光圈',
    shutter_speed VARCHAR(20) COMMENT '快门速度',
    focal_length VARCHAR(20) COMMENT '焦距',
    flash TINYINT COMMENT '是否使用闪光灯',
    white_balance VARCHAR(20) COMMENT '白平衡',
    exposure_mode VARCHAR(20) COMMENT '曝光模式',
    metering_mode VARCHAR(20) COMMENT '测光模式',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_photo_id (photo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='照片元数据表';

-- 分享记录表
CREATE TABLE IF NOT EXISTS t_share (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    photo_id BIGINT COMMENT '照片ID',
    album_id BIGINT COMMENT '相册ID',
    share_code VARCHAR(50) NOT NULL UNIQUE COMMENT '分享码',
    password VARCHAR(100) COMMENT '访问密码',
    expire_at DATETIME COMMENT '过期时间',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    is_active TINYINT DEFAULT 1 COMMENT '是否有效：0-否，1-是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_share_code (share_code),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分享记录表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS t_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT COMMENT '用户ID',
    operation VARCHAR(50) NOT NULL COMMENT '操作类型',
    target_type VARCHAR(50) COMMENT '目标类型',
    target_id BIGINT COMMENT '目标ID',
    detail TEXT COMMENT '操作详情',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_operation (operation),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 分享访问记录表
CREATE TABLE IF NOT EXISTS t_share_access (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    share_id BIGINT NOT NULL COMMENT '分享ID',
    ip_address VARCHAR(50) COMMENT '访问IP',
    user_agent VARCHAR(500) COMMENT '浏览器信息',
    accessed_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
    INDEX idx_share_id (share_id),
    INDEX idx_accessed_at (accessed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分享访问记录表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS t_system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(255) COMMENT '配置描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 系统公告表
CREATE TABLE IF NOT EXISTS t_announcement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content TEXT COMMENT '公告内容',
    type VARCHAR(20) DEFAULT 'NORMAL' COMMENT '公告类型',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统公告表';

-- 人脸信息表
CREATE TABLE IF NOT EXISTS t_face (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    photo_id BIGINT NOT NULL COMMENT '照片ID',
    face_token VARCHAR(100) COMMENT '人脸标识',
    face_group_id VARCHAR(100) COMMENT '人脸分组ID',
    person_name VARCHAR(50) COMMENT '人物名称',
    position VARCHAR(100) COMMENT '人脸位置JSON',
    confidence DECIMAL(5,2) COMMENT '置信度',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_photo_id (photo_id),
    INDEX idx_face_group_id (face_group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人脸信息表';

-- AI识别记录表
CREATE TABLE IF NOT EXISTS t_ai_recognition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    photo_id BIGINT NOT NULL COMMENT '照片ID',
    type VARCHAR(20) COMMENT '识别类型',
    result TEXT COMMENT '识别结果JSON',
    confidence DECIMAL(5,2) COMMENT '置信度',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_photo_id (photo_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI识别记录表';

-- 插入默认管理员账号
INSERT INTO t_user (username, password, email, nickname, role, status)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@example.com', '管理员', 'ADMIN', 1);

-- 插入默认系统配置
INSERT INTO t_system_config (config_key, config_value, description) VALUES
('site_name', '照片管理系统', '网站名称'),
('site_description', '一个功能完善的照片信息管理系统', '网站描述'),
('max_upload_size', '52428800', '最大上传文件大小（字节）'),
('allowed_file_types', 'jpg,jpeg,png,gif,webp,bmp', '允许上传的文件类型'),
('enable_ai_features', 'false', '是否启用AI功能'),
('enable_share', 'true', '是否启用分享功能');
