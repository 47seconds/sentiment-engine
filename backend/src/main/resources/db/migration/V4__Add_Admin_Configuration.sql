-- V4__Add_Admin_Configuration.sql
-- Add admin panel configuration keys to system_config table

-- Add new configuration keys for admin panel
INSERT INTO system_config (config_key, config_value, data_type, description) VALUES
-- Update existing keys with correct values
('alert.threshold.critical', '-0.6', 'FLOAT', 'EMA score threshold for critical alerts'),
('alert.threshold.warning', '-0.3', 'FLOAT', 'EMA score threshold for warning alerts'),

-- Feature flags (some may already exist in feature_flags table, but we'll use system_config for admin panel)
('driver.feedback.enabled', 'true', 'BOOLEAN', 'Allow feedback submission for drivers'),
('trip.feedback.enabled', 'false', 'BOOLEAN', 'Allow feedback submission for trips'),
('app.feedback.enabled', 'false', 'BOOLEAN', 'Allow feedback submission for app experience'),
('marshal.feedback.enabled', 'false', 'BOOLEAN', 'Allow feedback submission for marshals'),

-- Alert settings
('alert.max.per.driver', '5', 'INT', 'Maximum active alerts per driver'),
('alert.retention.days', '30', 'INT', 'How long to keep resolved alerts'),
('alert.auto.escalation.enabled', 'true', 'BOOLEAN', 'Automatically escalate unresolved alerts'),

-- Notification settings
('notification.email.enabled', 'true', 'BOOLEAN', 'Send email alerts to managers'),
('notification.sms.enabled', 'false', 'BOOLEAN', 'Send SMS alerts for critical issues')

ON CONFLICT (config_key) DO UPDATE SET
    config_value = EXCLUDED.config_value,
    data_type = EXCLUDED.data_type,
    description = EXCLUDED.description,
    updated_at = CURRENT_TIMESTAMP;

-- Analyze the updated table
ANALYZE system_config;