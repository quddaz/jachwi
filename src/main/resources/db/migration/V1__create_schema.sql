CREATE TABLE members (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    subject VARCHAR(255) NOT NULL,
    email VARCHAR(320) NOT NULL,
    name VARCHAR(100) NOT NULL,
    last_login_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_members PRIMARY KEY (id),
    CONSTRAINT uk_members_subject UNIQUE (subject)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE refresh_tokens (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    member_id BIGINT UNSIGNED NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_member FOREIGN KEY (member_id)
        REFERENCES members (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_refresh_tokens_member_expires
    ON refresh_tokens (member_id, expires_at);

CREATE TABLE properties (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    member_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(50) NOT NULL,
    deposit_amount BIGINT UNSIGNED NULL,
    monthly_rent_amount BIGINT UNSIGNED NULL,
    maintenance_fee_amount BIGINT UNSIGNED NULL,
    address VARCHAR(500) NULL,
    discovery_source VARCHAR(500) NULL,
    last_activity_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_properties PRIMARY KEY (id),
    CONSTRAINT fk_properties_member FOREIGN KEY (member_id)
        REFERENCES members (id) ON DELETE CASCADE,
    CONSTRAINT chk_properties_deposit_nonnegative
        CHECK (deposit_amount IS NULL OR deposit_amount >= 0),
    CONSTRAINT chk_properties_monthly_rent_nonnegative
        CHECK (monthly_rent_amount IS NULL OR monthly_rent_amount >= 0),
    CONSTRAINT chk_properties_maintenance_fee_nonnegative
        CHECK (maintenance_fee_amount IS NULL OR maintenance_fee_amount >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_properties_member_activity
    ON properties (member_id, last_activity_at DESC, id DESC);

CREATE INDEX idx_properties_member_name
    ON properties (member_id, name);

CREATE TABLE property_photos (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    property_id BIGINT UNSIGNED NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    size_bytes BIGINT UNSIGNED NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_property_photos PRIMARY KEY (id),
    CONSTRAINT uk_property_photos_storage_key UNIQUE (storage_key),
    CONSTRAINT fk_property_photos_property FOREIGN KEY (property_id)
        REFERENCES properties (id) ON DELETE CASCADE,
    CONSTRAINT chk_property_photos_content_type
        CHECK (content_type IN ('image/jpeg', 'image/png', 'image/webp')),
    CONSTRAINT chk_property_photos_size_positive CHECK (size_bytes > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_property_photos_property_created
    ON property_photos (property_id, created_at, id);

CREATE TABLE property_memos (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    property_id BIGINT UNSIGNED NOT NULL,
    free_memo VARCHAR(2000) NOT NULL DEFAULT '',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_property_memos PRIMARY KEY (id),
    CONSTRAINT uk_property_memos_property UNIQUE (property_id),
    CONSTRAINT fk_property_memos_property FOREIGN KEY (property_id)
        REFERENCES properties (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE property_memo_items (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    property_memo_id BIGINT UNSIGNED NOT NULL,
    label VARCHAR(30) NOT NULL,
    content VARCHAR(200) NOT NULL,
    display_order SMALLINT UNSIGNED NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_property_memo_items PRIMARY KEY (id),
    CONSTRAINT uk_property_memo_items_order UNIQUE (property_memo_id, display_order),
    CONSTRAINT fk_property_memo_items_memo FOREIGN KEY (property_memo_id)
        REFERENCES property_memos (id) ON DELETE CASCADE,
    CONSTRAINT chk_property_memo_items_order_positive CHECK (display_order > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE system_check_items (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    stage VARCHAR(30) NOT NULL,
    item_type VARCHAR(20) NOT NULL,
    question VARCHAR(200) NOT NULL,
    guide VARCHAR(500) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_system_check_items PRIMARY KEY (id),
    CONSTRAINT chk_system_check_items_stage
        CHECK (stage IN ('ONLINE_PHONE', 'ON_SITE', 'PRE_CONTRACT')),
    CONSTRAINT chk_system_check_items_type
        CHECK (item_type IN ('CORE', 'OPTIONAL')),
    CONSTRAINT chk_system_check_items_active CHECK (is_active IN (FALSE, TRUE))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_system_check_items_search
    ON system_check_items (stage, is_active, item_type, id);

CREATE TABLE user_checklists (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    member_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(50) NOT NULL,
    stage VARCHAR(30) NOT NULL,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_user_checklists PRIMARY KEY (id),
    CONSTRAINT fk_user_checklists_member FOREIGN KEY (member_id)
        REFERENCES members (id) ON DELETE CASCADE,
    CONSTRAINT chk_user_checklists_stage
        CHECK (stage IN ('ONLINE_PHONE', 'ON_SITE', 'PRE_CONTRACT'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_user_checklists_member_active
    ON user_checklists (member_id, deleted_at, updated_at DESC, id DESC);

CREATE TABLE user_checklist_items (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_checklist_id BIGINT UNSIGNED NOT NULL,
    system_check_item_id BIGINT UNSIGNED NOT NULL,
    display_order SMALLINT UNSIGNED NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_user_checklist_items PRIMARY KEY (id),
    CONSTRAINT uk_user_checklist_items_system_item
        UNIQUE (user_checklist_id, system_check_item_id),
    CONSTRAINT uk_user_checklist_items_order
        UNIQUE (user_checklist_id, display_order),
    CONSTRAINT fk_user_checklist_items_checklist FOREIGN KEY (user_checklist_id)
        REFERENCES user_checklists (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_checklist_items_system_item FOREIGN KEY (system_check_item_id)
        REFERENCES system_check_items (id) ON DELETE RESTRICT,
    CONSTRAINT chk_user_checklist_items_order_positive CHECK (display_order > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_user_checklist_items_system_item
    ON user_checklist_items (system_check_item_id);

CREATE TABLE property_checklists (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    property_id BIGINT UNSIGNED NOT NULL,
    source_user_checklist_id BIGINT UNSIGNED NULL,
    checklist_name_snapshot VARCHAR(50) NOT NULL,
    stage VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_property_checklists PRIMARY KEY (id),
    CONSTRAINT uk_property_checklists_stage UNIQUE (property_id, stage),
    CONSTRAINT fk_property_checklists_property FOREIGN KEY (property_id)
        REFERENCES properties (id) ON DELETE CASCADE,
    CONSTRAINT fk_property_checklists_source FOREIGN KEY (source_user_checklist_id)
        REFERENCES user_checklists (id) ON DELETE SET NULL,
    CONSTRAINT chk_property_checklists_stage
        CHECK (stage IN ('ONLINE_PHONE', 'ON_SITE', 'PRE_CONTRACT'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_property_checklists_property
    ON property_checklists (property_id, updated_at DESC, id DESC);

CREATE INDEX idx_property_checklists_source
    ON property_checklists (source_user_checklist_id);

CREATE TABLE property_checklist_items (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    property_checklist_id BIGINT UNSIGNED NOT NULL,
    source_system_check_item_id BIGINT UNSIGNED NOT NULL,
    question_snapshot VARCHAR(200) NOT NULL,
    guide_snapshot VARCHAR(500) NULL,
    display_order SMALLINT UNSIGNED NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'UNCONFIRMED',
    memo VARCHAR(500) NOT NULL DEFAULT '',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_property_checklist_items PRIMARY KEY (id),
    CONSTRAINT uk_property_checklist_items_system_item
        UNIQUE (property_checklist_id, source_system_check_item_id),
    CONSTRAINT uk_property_checklist_items_order
        UNIQUE (property_checklist_id, display_order),
    CONSTRAINT fk_property_checklist_items_checklist FOREIGN KEY (property_checklist_id)
        REFERENCES property_checklists (id) ON DELETE CASCADE,
    CONSTRAINT fk_property_checklist_items_system_item FOREIGN KEY (source_system_check_item_id)
        REFERENCES system_check_items (id) ON DELETE RESTRICT,
    CONSTRAINT chk_property_checklist_items_order_positive CHECK (display_order > 0),
    CONSTRAINT chk_property_checklist_items_status
        CHECK (status IN ('UNCONFIRMED', 'GOOD', 'CAUTION'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_property_checklist_items_system_item
    ON property_checklist_items (source_system_check_item_id);
