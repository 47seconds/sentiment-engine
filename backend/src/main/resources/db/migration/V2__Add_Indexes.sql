-- -- ==================== V2__Add_Indexes.sql ====================
-- -- Performance indexes for Driver Sentiment Engine
-- -- Indexes dramatically speed up queries by avoiding full table scans
-- -- Trade-off: Slightly slower writes, but much faster reads

-- -- ==================== FEEDBACK TABLE INDEXES ====================

-- -- Index 1: Query all feedback for a specific driver
-- -- Query: SELECT * FROM feedback WHERE driver_id = ? AND created_at > ?
-- -- Without index: Full table scan (slow)
-- -- With index: Range scan (fast)
-- -- Expected improvement: 100-1000x faster
CREATE INDEX IF NOT EXISTS idx_feedback_driver_created 
ON feedback(driver_id, created_at DESC);

-- -- Index 2: Query unprocessed feedback
-- -- Query: SELECT * FROM feedback WHERE is_processed = FALSE
-- -- Use case: Background job to reprocess failed feedback
CREATE INDEX IF NOT EXISTS idx_feedback_unprocessed 
ON feedback(is_processed, created_at DESC);

-- -- Index 3: Query by feedback type for analytics
-- -- Query: SELECT COUNT(*) FROM feedback WHERE feedback_type = ? 
CREATE INDEX IF NOT EXISTS idx_feedback_type 
ON feedback(feedback_type, created_at DESC);

-- -- ==================== DRIVER SENTIMENT STATS INDEXES ====================

-- -- Index 4: Sort drivers by EMA score (for dashboard)
-- -- Query: SELECT * FROM driver_sentiment_stats ORDER BY ema_score ASC LIMIT 10
-- -- Use case: Find top 10 worst-performing drivers
-- -- Without index: Full sort needed
-- -- With index: Already sorted in index
CREATE INDEX IF NOT EXISTS idx_driver_sentiment_ema 
ON driver_sentiment_stats(ema_score ASC);

-- -- Index 5: Sort drivers by feedback count (for analytics)
-- -- Query: SELECT * FROM driver_sentiment_stats ORDER BY total_feedback_count DESC
CREATE INDEX IF NOT EXISTS idx_driver_feedback_count 
ON driver_sentiment_stats(total_feedback_count DESC);

-- -- ==================== ALERT HISTORY INDEXES ====================

-- -- Index 6: Query alerts for specific driver (most recent first)
-- -- Query: SELECT * FROM alert_history WHERE driver_id = ? ORDER BY created_at DESC
-- -- Use case: View alert history for a driver
CREATE INDEX IF NOT EXISTS idx_alert_driver_created 
ON alert_history(driver_id, created_at DESC);

-- -- Index 7: Query active cooldown periods
-- -- Query: SELECT * FROM alert_history WHERE driver_id = ? AND cooldown_expires_at > NOW()
-- -- Use case: Check if driver is in alert cooldown
-- -- Expected improvement: 10-100x faster than full table scan
CREATE INDEX IF NOT EXISTS idx_alert_cooldown 
ON alert_history(driver_id, cooldown_expires_at DESC);

-- -- Index 8: Query unacknowledged alerts (for ops team)
-- -- Query: SELECT * FROM alert_history WHERE is_acknowledged = FALSE
CREATE INDEX IF NOT EXISTS idx_alert_unacknowledged 
ON alert_history(is_acknowledged, created_at DESC);

-- -- ==================== USERS TABLE INDEX ====================

-- -- Index 9: Email lookup for authentication
-- Query: SELECT * FROM users WHERE email = ?
CREATE INDEX IF NOT EXISTS idx_user_email 
ON users(email);

-- -- ==================== STATISTICS ====================
-- -- Analyze table statistics for query planner optimization
-- -- PostgreSQL uses these stats to decide on best query execution plan
ANALYZE feedback;
ANALYZE driver_sentiment_stats;
ANALYZE alert_history;
ANALYZE users;
