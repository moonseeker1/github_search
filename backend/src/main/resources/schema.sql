-- GitHub Search Database Schema

CREATE DATABASE IF NOT EXISTS github_search DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE github_search;

-- Repository Table
CREATE TABLE IF NOT EXISTS gh_repository (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    repo_id VARCHAR(50) UNIQUE COMMENT 'GitHub Repository ID',
    owner VARCHAR(100) COMMENT 'Owner',
    name VARCHAR(100) COMMENT 'Repository Name',
    full_name VARCHAR(200) COMMENT 'Full Name (owner/name)',
    description TEXT COMMENT 'Description',
    language VARCHAR(50) COMMENT 'Primary Language',
    stars INT DEFAULT 0 COMMENT 'Stars Count',
    forks INT DEFAULT 0 COMMENT 'Forks Count',
    clone_url VARCHAR(500) COMMENT 'Clone URL',
    homepage VARCHAR(500) COMMENT 'Homepage',
    topics TEXT COMMENT 'Topics (JSON array)',
    github_created_at DATETIME COMMENT 'GitHub Created At',
    github_updated_at DATETIME COMMENT 'GitHub Updated At',
    status TINYINT DEFAULT 0 COMMENT 'Status: 0-pending, 1-parsed, 2-indexed',
    create_by VARCHAR(64) DEFAULT '',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT '',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_owner (owner),
    INDEX idx_name (name),
    INDEX idx_language (language),
    INDEX idx_stars (stars),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GitHub Repository';

-- Code File Table
CREATE TABLE IF NOT EXISTS gh_code_file (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    repo_id BIGINT COMMENT 'Repository ID',
    file_path VARCHAR(500) COMMENT 'File Path',
    file_name VARCHAR(200) COMMENT 'File Name',
    language VARCHAR(50) COMMENT 'Programming Language',
    size INT COMMENT 'File Size',
    content_hash VARCHAR(64) COMMENT 'Content Hash (SHA)',
    content LONGTEXT COMMENT 'File Content',
    is_indexed TINYINT DEFAULT 0 COMMENT 'Is Indexed: 0-no, 1-yes',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_repo_id (repo_id),
    INDEX idx_language (language),
    INDEX idx_is_indexed (is_indexed),
    FOREIGN KEY (repo_id) REFERENCES gh_repository(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Code File';

-- Search History Table
CREATE TABLE IF NOT EXISTS search_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    query_text VARCHAR(500) COMMENT 'Search Query',
    query_type VARCHAR(20) COMMENT 'Query Type: repository/code/qa',
    language VARCHAR(50) COMMENT 'Language Filter',
    result_count INT DEFAULT 0 COMMENT 'Result Count',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_query_type (query_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Search History';
