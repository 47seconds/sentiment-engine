-- -- ==================== V1__Initial_Schema.sql ====================
-- -- Initial database schema for Driver Sentiment Engine
-- -- Created: 2025-11-09
-- -- This migration creates all core tables for the sentiment engine

-- -- ==================== USERS TABLE ====================
CREATE TABLE IF NOT EXISTS users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'EMPLOYEE' CHECK (role IN ('EMPLOYEE', 'ADMIN', 'SUPER_ADMIN')),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -- ==================== FEEDBACK TABLE ====================
-- -- Core table storing all rider feedback
-- -- Primary data source for sentiment analysis
CREATE TABLE IF NOT EXISTS feedback (
    feedback_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id VARCHAR(50) NOT NULL,
    trip_id VARCHAR(50) NOT NULL UNIQUE,  -- Unique constraint ensures no duplicate feedback per trip
    submitted_by UUID NOT NULL REFERENCES users(user_id),
    feedback_text TEXT NOT NULL,
    optional_rating DECIMAL(3,2) CHECK (optional_rating >= 1 AND optional_rating <= 5),
    feedback_type VARCHAR(20) NOT NULL DEFAULT 'DRIVER' 
        CHECK (feedback_type IN ('DRIVER', 'TRIP', 'APP', 'MARSHAL')),
    sentiment_score DECIMAL(3,2) NOT NULL,  -- 1.0 to 5.0
    sentiment_class VARCHAR(10) NOT NULL 
        CHECK (sentiment_class IN ('POSITIVE', 'NEUTRAL', 'NEGATIVE')),
    is_processed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -- ==================== DRIVER SENTIMENT STATS TABLE ====================
-- -- Aggregated statistics per driver
-- -- Updated in real-time as new feedback arrives
-- -- Purpose: Fast queries for dashboard without recalculating from feedback table
CREATE TABLE IF NOT EXISTS driver_sentiment_stats (
    driver_id VARCHAR(50) PRIMARY KEY,
    current_avg_score DECIMAL(4,3) NOT NULL DEFAULT 3.0,  -- Simple average (1-5 scale)
    ema_score DECIMAL(4,3) NOT NULL DEFAULT 3.0,          -- Exponential Moving Average
    total_feedback_count INT NOT NULL DEFAULT 0,
    positive_count INT NOT NULL DEFAULT 0,                -- Sentiment class counts
    neutral_count INT NOT NULL DEFAULT 0,
    negative_count INT NOT NULL DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_feedback_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -- ==================== ALERT HISTORY TABLE ====================
-- -- Records of all alerts triggered for drivers
-- -- Used for:
-- -- 1. Alert cooldown/throttling (prevent spam)
-- -- 2. Audit trail of when alerts were raised
-- -- 3. Analytics on alert frequency
CREATE TABLE IF NOT EXISTS alert_history (
    alert_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id VARCHAR(50) NOT NULL REFERENCES driver_sentiment_stats(driver_id),
    alert_type VARCHAR(30) NOT NULL DEFAULT 'LOW_SENTIMENT'
        CHECK (alert_type IN ('LOW_SENTIMENT', 'CRITICAL_SENTIMENT', 'QUALITY_SPIKE')),
    trigger_score DECIMAL(4,3) NOT NULL,      -- EMA score when alert triggered
    threshold DECIMAL(4,3) NOT NULL,           -- Threshold at time of alert
    is_acknowledged BOOLEAN DEFAULT FALSE,     -- Whether ops team acknowledged
    acknowledged_by UUID REFERENCES users(user_id),
    acknowledged_at TIMESTAMP,
    cooldown_expires_at TIMESTAMP,             -- When this alert becomes triggerable again
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -- ==================== SYSTEM CONFIG TABLE ====================
-- -- Dynamic configuration that can be changed at runtime
-- -- No application restart needed
-- -- Examples: alert thresholds, cooldown periods, feature flags
CREATE TABLE IF NOT EXISTS system_config (
    config_key VARCHAR(100) PRIMARY KEY,
    config_value TEXT NOT NULL,
    data_type VARCHAR(20) NOT NULL 
        CHECK (data_type IN ('STRING', 'INT', 'FLOAT', 'BOOLEAN')),
    description TEXT,
    updated_by UUID REFERENCES users(user_id),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -- ==================== FEATURE FLAGS TABLE ====================
-- -- Feature toggles that can be enabled/disabled without code changes
-- -- Used to gradually roll out features or disable broken features
CREATE TABLE IF NOT EXISTS feature_flags (
    feature_name VARCHAR(50) PRIMARY KEY,
    is_enabled BOOLEAN DEFAULT TRUE,
    description TEXT,
    updated_by UUID REFERENCES users(user_id),
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
