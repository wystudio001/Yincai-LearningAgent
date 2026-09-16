-- ============================================================
-- 因材智学系统 — 数据库初始化脚本
-- All databases in a single MySQL instance for MVP phase
-- ============================================================

CREATE DATABASE IF NOT EXISTS learning_auth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS learning_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS learning_course DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS learning_profile DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS learning_resource DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS learning_path DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS learning_ai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS learning_exam DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS learning_notify DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ============================================================
-- learning_auth
-- ============================================================
USE learning_auth;

CREATE TABLE auth_user (
    id BIGINT NOT NULL PRIMARY KEY,
    tenant_id BIGINT DEFAULT 0,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(256) NOT NULL,
    role_code VARCHAR(32) NOT NULL DEFAULT 'STUDENT',
    status TINYINT NOT NULL DEFAULT 1,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE auth_user_session (
    id BIGINT NOT NULL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    refresh_token VARCHAR(512) NOT NULL,
    device_id VARCHAR(128),
    expires_at DATETIME NOT NULL,
    revoked TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_refresh_token (refresh_token(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE auth_login_log (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(64),
    client_type VARCHAR(32),
    ip_address VARCHAR(64),
    success TINYINT NOT NULL DEFAULT 1,
    fail_reason VARCHAR(256),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert a default admin user (password: admin123)
INSERT INTO auth_user (id, tenant_id, username, password_hash, role_code) VALUES
(1, 0, 'admin', 'admin123', 'PLATFORM_ADMIN'),
(2, 0, 'teacher01', 'teacher123', 'TEACHER'),
(3, 0, 'student01', 'student123', 'STUDENT');

-- ============================================================
-- learning_user
-- ============================================================
USE learning_user;

CREATE TABLE user (
    id BIGINT NOT NULL PRIMARY KEY,
    tenant_id BIGINT DEFAULT 0,
    username VARCHAR(64) NOT NULL,
    real_name VARCHAR(64),
    email VARCHAR(128),
    phone VARCHAR(20),
    role_code VARCHAR(32) NOT NULL DEFAULT 'STUDENT',
    status TINYINT NOT NULL DEFAULT 1,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    trace_id VARCHAR(64),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE role (
    id BIGINT NOT NULL PRIMARY KEY,
    tenant_id BIGINT DEFAULT 0,
    name VARCHAR(64) NOT NULL,
    code VARCHAR(32) NOT NULL,
    description VARCHAR(256),
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE permission (
    id BIGINT NOT NULL PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    name VARCHAR(64) NOT NULL,
    code VARCHAR(64) NOT NULL,
    type VARCHAR(16) DEFAULT 'MENU',
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE role_permission (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_perm (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- learning_course
-- ============================================================
USE learning_course;

CREATE TABLE course (
    id BIGINT NOT NULL PRIMARY KEY,
    tenant_id BIGINT DEFAULT 0,
    name VARCHAR(256) NOT NULL,
    description TEXT,
    cover_image VARCHAR(512),
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE chapter (
    id BIGINT NOT NULL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    parent_id BIGINT DEFAULT 0,
    name VARCHAR(256) NOT NULL,
    sort_order INT DEFAULT 0,
    description TEXT,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE knowledge_point (
    id BIGINT NOT NULL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    chapter_id BIGINT,
    name VARCHAR(256) NOT NULL,
    description TEXT,
    difficulty VARCHAR(16) DEFAULT 'MEDIUM',
    tags VARCHAR(512),
    vector_id VARCHAR(128),
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE knowledge_relation (
    id BIGINT NOT NULL PRIMARY KEY,
    point_id BIGINT NOT NULL,
    related_point_id BIGINT NOT NULL,
    relation_type VARCHAR(32) NOT NULL DEFAULT 'RELATED',
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_point (point_id),
    INDEX idx_related (related_point_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE course_material (
    id BIGINT NOT NULL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    file_name VARCHAR(256) NOT NULL,
    file_type VARCHAR(64),
    file_size BIGINT DEFAULT 0,
    storage_path VARCHAR(512),
    uploaded_by BIGINT,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- learning_profile
-- ============================================================
USE learning_profile;

CREATE TABLE student_profile (
    id BIGINT NOT NULL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'INITIALIZING',
    summary TEXT,
    confidence DOUBLE DEFAULT 0.0,
    last_refresh_at DATETIME,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_course (student_id, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE profile_dimension (
    id BIGINT NOT NULL PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    dimension_code VARCHAR(64) NOT NULL,
    dimension_name VARCHAR(128) NOT NULL,
    score INT DEFAULT 0,
    label VARCHAR(256),
    evidence TEXT,
    confidence DOUBLE DEFAULT 0.0,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_profile (profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE profile_version (
    id BIGINT NOT NULL PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    version_no INT NOT NULL,
    snapshot JSON,
    trigger_type VARCHAR(32),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_profile (profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE profile_dialogue (
    id BIGINT NOT NULL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    role VARCHAR(16) NOT NULL DEFAULT 'STUDENT',
    content TEXT,
    metadata JSON,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- learning_resource
-- ============================================================
USE learning_resource;

CREATE TABLE resource (
    id BIGINT NOT NULL PRIMARY KEY,
    tenant_id BIGINT DEFAULT 0,
    resource_type VARCHAR(32) NOT NULL,
    title VARCHAR(256) NOT NULL,
    summary TEXT,
    course_id BIGINT,
    knowledge_point_id BIGINT,
    difficulty VARCHAR(16) DEFAULT 'MEDIUM',
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    quality_score DOUBLE DEFAULT 0.0,
    source_task_id BIGINT,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_course (course_id),
    INDEX idx_type (resource_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE resource_file (
    id BIGINT NOT NULL PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    file_name VARCHAR(256),
    file_type VARCHAR(64),
    file_size BIGINT DEFAULT 0,
    storage_path VARCHAR(512),
    download_url VARCHAR(512),
    preview_url VARCHAR(512),
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_resource (resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE resource_package (
    id BIGINT NOT NULL PRIMARY KEY,
    tenant_id BIGINT DEFAULT 0,
    name VARCHAR(256) NOT NULL,
    description TEXT,
    course_id BIGINT,
    student_id BIGINT,
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    resource_ids JSON,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE generation_task (
    id BIGINT NOT NULL PRIMARY KEY,
    tenant_id BIGINT DEFAULT 0,
    requester_id BIGINT NOT NULL,
    course_id BIGINT,
    knowledge_point_id BIGINT,
    task_type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    progress INT DEFAULT 0,
    error_message TEXT,
    input_context JSON,
    output_summary JSON,
    started_at DATETIME,
    finished_at DATETIME,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_requester (requester_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE generation_step (
    id BIGINT NOT NULL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    agent_code VARCHAR(64),
    step_type VARCHAR(64),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    input_summary TEXT,
    output_resource_id BIGINT,
    started_at DATETIME,
    finished_at DATETIME,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE resource_favorite (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_resource (user_id, resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE resource_feedback (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    rating INT DEFAULT 0,
    comment TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE file (
    id BIGINT NOT NULL PRIMARY KEY,
    tenant_id BIGINT DEFAULT 0,
    original_name VARCHAR(256),
    file_type VARCHAR(64),
    file_size BIGINT DEFAULT 0,
    storage_path VARCHAR(512),
    status VARCHAR(16) DEFAULT 'UPLOADED',
    uploaded_by BIGINT,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- learning_ai
-- ============================================================
USE learning_ai;

CREATE TABLE ai_model_provider (
    id BIGINT NOT NULL PRIMARY KEY,
    tenant_id BIGINT DEFAULT 0,
    name VARCHAR(100) NOT NULL,
    provider_code VARCHAR(64) NOT NULL,
    base_url VARCHAR(512) NOT NULL,
    api_key VARCHAR(1024) NOT NULL,
    default_model VARCHAR(128) NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 0,
    timeout_ms INT NOT NULL DEFAULT 120000,
    temperature DECIMAL(4,2) NOT NULL DEFAULT 0.20,
    max_tokens INT NOT NULL DEFAULT 2048,
    remark VARCHAR(500),
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_ai_model_provider_enabled (enabled, deleted),
    INDEX idx_ai_model_provider_provider_code (provider_code, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- learning_path
-- ============================================================
USE learning_path;

CREATE TABLE learning_path (
    id BIGINT NOT NULL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    version INT DEFAULT 1,
    generated_by VARCHAR(32) DEFAULT 'SYSTEM',
    current_node_id BIGINT,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_course (student_id, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE learning_path_node (
    id BIGINT NOT NULL PRIMARY KEY,
    path_id BIGINT NOT NULL,
    knowledge_point_id BIGINT,
    node_order INT DEFAULT 0,
    target VARCHAR(512),
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    completion_rule TEXT,
    started_at DATETIME,
    completed_at DATETIME,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_path (path_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE path_recommendation (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    path_node_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    resource_type VARCHAR(32),
    priority INT DEFAULT 0,
    reason VARCHAR(512),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_node (path_node_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE path_intervention (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    path_id BIGINT NOT NULL,
    instructor_id BIGINT NOT NULL,
    action VARCHAR(64),
    reason TEXT,
    result TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- learning_exam
-- ============================================================
USE learning_exam;

CREATE TABLE exam_question (
    id BIGINT NOT NULL PRIMARY KEY,
    tenant_id BIGINT DEFAULT 0,
    question_type VARCHAR(32) NOT NULL,
    title VARCHAR(512),
    content TEXT,
    options_json JSON,
    answer TEXT,
    explanation TEXT,
    tags VARCHAR(256),
    difficulty VARCHAR(16) DEFAULT 'MEDIUM',
    course_id BIGINT,
    knowledge_point_id BIGINT,
    source VARCHAR(32) DEFAULT 'TEACHER',
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_course (course_id),
    INDEX idx_type (question_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE exam_exercise (
    id BIGINT NOT NULL PRIMARY KEY,
    tenant_id BIGINT DEFAULT 0,
    title VARCHAR(256) NOT NULL,
    description TEXT,
    course_id BIGINT,
    knowledge_point_ids JSON,
    question_count INT DEFAULT 0,
    time_limit_min INT DEFAULT 0,
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE exam_exercise_question (
    id BIGINT NOT NULL PRIMARY KEY,
    exercise_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    sort_order INT DEFAULT 0,
    score DOUBLE DEFAULT 0.0,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_exercise (exercise_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE exam_answer_record (
    id BIGINT NOT NULL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    exercise_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_content TEXT,
    score DOUBLE DEFAULT 0.0,
    is_correct TINYINT DEFAULT 0,
    feedback TEXT,
    knowledge_point_id BIGINT,
    submitted_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_student_exercise (student_id, exercise_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE exam_wrong_question (
    id BIGINT NOT NULL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    exercise_id BIGINT,
    review_status VARCHAR(16) DEFAULT 'PENDING',
    wrong_count INT DEFAULT 1,
    last_wrong_at DATETIME,
    reviewed_at DATETIME,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE exam_assessment_report (
    id BIGINT NOT NULL PRIMARY KEY,
    tenant_id BIGINT DEFAULT 0,
    student_id BIGINT NOT NULL,
    course_id BIGINT,
    report_type VARCHAR(32) NOT NULL DEFAULT 'PERIODIC',
    summary TEXT,
    weaknesses JSON,
    suggestions JSON,
    score_detail JSON,
    generated_at DATETIME,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_student_course (student_id, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- learning_notify
-- ============================================================
USE learning_notify;

CREATE TABLE notify_notification (
    id BIGINT NOT NULL PRIMARY KEY,
    tenant_id BIGINT DEFAULT 0,
    user_id BIGINT NOT NULL,
    title VARCHAR(256) NOT NULL,
    content TEXT,
    type VARCHAR(32) NOT NULL DEFAULT 'SYSTEM',
    status VARCHAR(16) NOT NULL DEFAULT 'UNREAD',
    target_type VARCHAR(32),
    target_id BIGINT,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE notify_notification_template (
    id BIGINT NOT NULL PRIMARY KEY,
    tenant_id BIGINT DEFAULT 0,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(128),
    type VARCHAR(32) NOT NULL DEFAULT 'SYSTEM',
    title_template VARCHAR(512),
    content_template TEXT,
    variables JSON,
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE notify_push_task (
    id BIGINT NOT NULL PRIMARY KEY,
    tenant_id BIGINT DEFAULT 0,
    user_id BIGINT NOT NULL,
    notification_id BIGINT,
    channel VARCHAR(16) NOT NULL DEFAULT 'IN_APP',
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    retry_count INT DEFAULT 0,
    sent_at DATETIME,
    error_message TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
