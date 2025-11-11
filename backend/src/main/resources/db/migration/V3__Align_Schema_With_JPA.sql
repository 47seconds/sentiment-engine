-- -- ==================== V3__Align_Schema_With_JPA.sql ====================
-- -- Migration to align Flyway schema with JPA entity definitions
-- -- Changes: UUID primary keys -> BIGINT (auto-increment) primary keys
-- -- Created: 2025-11-09
-- -- Purpose: Resolve schema conflicts between Flyway and Hibernate

-- -- ==================== DROP EXISTING TABLES ====================
-- -- Drop in reverse dependency order to avoid foreign key constraint violations
DROP TABLE IF EXISTS alert_history CASCADE;
DROP TABLE IF EXISTS feature_flags CASCADE;
DROP TABLE IF EXISTS system_config CASCADE;
DROP TABLE IF EXISTS feedback CASCADE;
DROP TABLE IF EXISTS driver_sentiment_stats CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- -- ==================== RECREATE USERS TABLE (with BIGINT PK) ====================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    driver_id BIGINT UNIQUE,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'DRIVER' 
        CHECK (role IN ('DRIVER', 'ADMIN', 'SUPPORT', 'MANAGER', 'ANALYST')),
    is_active BOOLEAN DEFAULT TRUE,
    profile_picture_url VARCHAR(500),
    license_number VARCHAR(50),
    vehicle_number VARCHAR(50),
    last_login_at TIMESTAMP,
    password_changed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -- User permissions junction table
CREATE TABLE user_permissions (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    permissions VARCHAR(100) NOT NULL,
    PRIMARY KEY (user_id, permissions)
);

-- -- ==================== RECREATE FEEDBACK TABLE (with BIGINT PK) ====================
CREATE TABLE feedback (
    id BIGSERIAL PRIMARY KEY,
    driver_id BIGINT NOT NULL,
    trip_id BIGINT NOT NULL,
    user_id BIGINT REFERENCES users(id),
    feedback_type VARCHAR(30) NOT NULL DEFAULT 'DRIVER_BEHAVIOR'
        CHECK (feedback_type IN ('DRIVER_BEHAVIOR', 'VEHICLE_CONDITION', 'ROUTE_EFFICIENCY', 
                                  'CUSTOMER_SERVICE', 'SAFETY_CONCERN', 'PUNCTUALITY', 'OTHER')),
    feedback_text TEXT NOT NULL,
    rating DOUBLE PRECISION CHECK (rating >= 1.0 AND rating <= 5.0),
    source VARCHAR(20) NOT NULL DEFAULT 'RIDER'
        CHECK (source IN ('RIDER', 'MANAGER', 'SYSTEM', 'PEER_REVIEW', 'SELF_ASSESSMENT', 'CUSTOMER_SUPPORT', 'AUTOMATED')),
    sentiment_score DOUBLE PRECISION,
    sentiment_label VARCHAR(20)
        CHECK (sentiment_label IN ('VERY_POSITIVE', 'POSITIVE', 'NEUTRAL', 'NEGATIVE', 'VERY_NEGATIVE')),
    confidence DOUBLE PRECISION,
    requires_attention BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'PROCESSING', 'PROCESSED', 'FAILED', 'REVIEWED', 'FLAGGED', 'ARCHIVED')),
    processed_at TIMESTAMP,
    reviewed_at TIMESTAMP,
    reviewed_by BIGINT REFERENCES users(id),
    review_notes TEXT,
    metadata TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -- Feedback keywords collection table
CREATE TABLE feedback_keywords (
    feedback_id BIGINT NOT NULL REFERENCES feedback(id) ON DELETE CASCADE,
    keywords VARCHAR(255) NOT NULL
);

-- -- ==================== RECREATE DRIVER SENTIMENT STATS TABLE (with BIGINT PK) ====================
CREATE TABLE driver_stats (
    id BIGSERIAL PRIMARY KEY,
    driver_id BIGINT NOT NULL UNIQUE,
    ema_score DOUBLE PRECISION NOT NULL DEFAULT 3.0,
    previous_ema_score DOUBLE PRECISION,
    total_feedback_count INTEGER NOT NULL DEFAULT 0,
    positive_feedback_count INTEGER NOT NULL DEFAULT 0,
    negative_feedback_count INTEGER NOT NULL DEFAULT 0,
    neutral_feedback_count INTEGER NOT NULL DEFAULT 0,
    very_positive_count INTEGER NOT NULL DEFAULT 0,
    very_negative_count INTEGER NOT NULL DEFAULT 0,
    average_rating DOUBLE PRECISION,
    total_ratings_count INTEGER NOT NULL DEFAULT 0,
    alert_status VARCHAR(20) NOT NULL DEFAULT 'NORMAL'
        CHECK (alert_status IN ('NORMAL', 'WARNING', 'CRITICAL', 'UNDER_REVIEW', 'RESOLVED')),
    alert_triggered_at TIMESTAMP,
    alert_count INTEGER NOT NULL DEFAULT 0,
    last_alert_severity VARCHAR(20)
        CHECK (last_alert_severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    consecutive_negative_feedback INTEGER NOT NULL DEFAULT 0,
    last_positive_feedback_at TIMESTAMP,
    last_negative_feedback_at TIMESTAMP,
    last_feedback_at TIMESTAMP,
    ema_alpha DOUBLE PRECISION NOT NULL DEFAULT 0.20,
    stats_calculation_version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -- ==================== RECREATE ALERTS TABLE (with BIGINT PK) ====================
CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    driver_id BIGINT NOT NULL,
    alert_type VARCHAR(30) NOT NULL DEFAULT 'LOW_SENTIMENT'
        CHECK (alert_type IN ('LOW_SENTIMENT', 'CRITICAL_SENTIMENT', 'CONSECUTIVE_NEGATIVE', 
                               'SUDDEN_DROP', 'QUALITY_SPIKE', 'NO_RECENT_FEEDBACK', 'CUSTOM')),
    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM'
        CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    current_ema_score DOUBLE PRECISION,
    previous_ema_score DOUBLE PRECISION,
    score_drop DOUBLE PRECISION,
    threshold_value DOUBLE PRECISION,
    alert_message TEXT,
    recommended_action VARCHAR(50)
        CHECK (recommended_action IN ('COACHING_SESSION', 'PERFORMANCE_REVIEW', 'IMMEDIATE_INTERVENTION', 
                                       'MONITOR_CLOSELY', 'TRAINING_RECOMMENDED', 'ESCALATE_TO_MANAGER', 
                                       'COUNSELING', 'NO_ACTION')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'ACKNOWLEDGED', 'IN_PROGRESS', 'RESOLVED', 'DISMISSED', 'ESCALATED')),
    assigned_to BIGINT REFERENCES users(id),
    acknowledged_at TIMESTAMP,
    acknowledged_by BIGINT REFERENCES users(id),
    resolved_at TIMESTAMP,
    resolved_by BIGINT REFERENCES users(id),
    resolution_notes TEXT,
    cooldown_expires_at TIMESTAMP,
    notification_sent BOOLEAN DEFAULT FALSE,
    notification_sent_at TIMESTAMP,
    related_feedback_ids TEXT,
    metadata TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -- ==================== CREATE INDEXES ====================

-- -- Users indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_driver_id ON users(driver_id);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_active ON users(is_active);
CREATE INDEX idx_users_created ON users(created_at);

-- -- Feedback indexes
CREATE INDEX idx_feedback_driver_id ON feedback(driver_id);
CREATE INDEX idx_feedback_trip_id ON feedback(trip_id);
CREATE INDEX idx_feedback_user_id ON feedback(user_id);
CREATE INDEX idx_feedback_type ON feedback(feedback_type);
CREATE INDEX idx_feedback_sentiment_label ON feedback(sentiment_label);
CREATE INDEX idx_feedback_status ON feedback(status);
CREATE INDEX idx_feedback_requires_attention ON feedback(requires_attention);
CREATE INDEX idx_feedback_created ON feedback(created_at);
CREATE INDEX idx_feedback_driver_created ON feedback(driver_id, created_at DESC);

-- -- Driver stats indexes
CREATE INDEX idx_driver_stats_driver_id ON driver_stats(driver_id);
CREATE INDEX idx_driver_stats_ema_score ON driver_stats(ema_score);
CREATE INDEX idx_driver_stats_alert_status ON driver_stats(alert_status);
CREATE INDEX idx_driver_stats_last_feedback ON driver_stats(last_feedback_at);

-- -- Alerts indexes
CREATE INDEX idx_alerts_driver_id ON alerts(driver_id);
CREATE INDEX idx_alerts_type ON alerts(alert_type);
CREATE INDEX idx_alerts_severity ON alerts(severity);
CREATE INDEX idx_alerts_status ON alerts(status);
CREATE INDEX idx_alerts_assigned_to ON alerts(assigned_to);
CREATE INDEX idx_alerts_created ON alerts(created_at);
CREATE INDEX idx_alerts_cooldown ON alerts(driver_id, cooldown_expires_at);

-- -- ==================== FEATURE FLAGS TABLE ====================
CREATE TABLE feature_flags (
    feature_name VARCHAR(50) PRIMARY KEY,
    is_enabled BOOLEAN DEFAULT TRUE,
    description TEXT,
    updated_by BIGINT REFERENCES users(id),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -- ==================== SYSTEM CONFIG TABLE ====================
CREATE TABLE system_config (
    config_key VARCHAR(100) PRIMARY KEY,
    config_value TEXT NOT NULL,
    data_type VARCHAR(20) NOT NULL 
        CHECK (data_type IN ('STRING', 'INT', 'FLOAT', 'BOOLEAN')),
    description TEXT,
    updated_by BIGINT REFERENCES users(id),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -- ==================== INSERT DEFAULT DATA ====================

-- -- Insert default feature flags
INSERT INTO feature_flags (feature_name, is_enabled, description) VALUES
('driver.feedback.enabled', true, 'Allow feedback submission for drivers'),
('trip.feedback.enabled', true, 'Allow feedback submission for trips'),
('app.feedback.enabled', true, 'Allow feedback submission for app experience'),
('marshal.feedback.enabled', false, 'Allow feedback submission for on-ground marshals'),
('alert.system.enabled', true, 'Enable alert triggering for low sentiment drivers'),
('cache.enabled', true, 'Enable Redis caching layer')
ON CONFLICT (feature_name) DO NOTHING;

-- -- Insert default system configuration
INSERT INTO system_config (config_key, config_value, data_type, description) VALUES
('alert.threshold.sentiment', '2.5', 'FLOAT', 'EMA score threshold for triggering alerts'),
('alert.threshold.critical', '1.5', 'FLOAT', 'EMA score threshold for critical alerts'),
('alert.cooldown.minutes', '120', 'INT', 'Cooldown period between alerts for same driver'),
('cache.ttl.minutes', '60', 'INT', 'Time-to-live for cached driver stats'),
('feedback.batch.size', '100', 'INT', 'Batch size for processing feedback'),
('ema.alpha', '0.1818', 'FLOAT', 'EMA alpha parameter (sensitivity to recent scores)')
ON CONFLICT (config_key) DO NOTHING;

-- -- ==================== ANALYZE TABLES ====================
ANALYZE users;
ANALYZE feedback;
ANALYZE driver_stats;
ANALYZE alerts;
ANALYZE user_permissions;
ANALYZE feedback_keywords;
ANALYZE feature_flags;
ANALYZE system_config;
