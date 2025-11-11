package com.moveinsync.sentiment.config;

import lombok.experimental.UtilityClass;

/**
 * Utility class for Redis cache key patterns and constants
 */
@UtilityClass
public class CacheKeys {

    // ========== User Cache Keys ==========
    
    /**
     * User by ID: user:id:{userId}
     */
    public static String userById(Long userId) {
        return "user:id:" + userId;
    }

    /**
     * User by email: user:email:{email}
     */
    public static String userByEmail(String email) {
        return "user:email:" + email;
    }

    /**
     * User by driver ID: user:driver:{driverId}
     */
    public static String userByDriverId(Long driverId) {
        return "user:driver:" + driverId;
    }

    /**
     * All active drivers: user:active:drivers
     */
    public static final String ACTIVE_DRIVERS = "user:active:drivers";

    // ========== Driver Stats Cache Keys ==========

    /**
     * Driver stats by driver ID: stats:driver:{driverId}
     */
    public static String driverStatsById(Long driverId) {
        return "stats:driver:" + driverId;
    }

    /**
     * Driver EMA score: stats:ema:{driverId}
     */
    public static String driverEmaScore(Long driverId) {
        return "stats:ema:" + driverId;
    }

    /**
     * Drivers with low EMA scores: stats:low:ema:{threshold}
     */
    public static String driversWithLowEma(Double threshold) {
        return "stats:low:ema:" + threshold;
    }

    /**
     * Drivers needing attention: stats:attention:drivers
     */
    public static final String DRIVERS_NEEDING_ATTENTION = "stats:attention:drivers";

    /**
     * Top performing drivers: stats:top:performers:{limit}
     */
    public static String topPerformingDrivers(int limit) {
        return "stats:top:performers:" + limit;
    }

    /**
     * Bottom performing drivers: stats:bottom:performers:{limit}
     */
    public static String bottomPerformingDrivers(int limit) {
        return "stats:bottom:performers:" + limit;
    }

    // ========== Feedback Cache Keys ==========

    /**
     * Feedback by ID: feedback:id:{feedbackId}
     */
    public static String feedbackById(Long feedbackId) {
        return "feedback:id:" + feedbackId;
    }

    /**
     * Feedback list by driver: feedback:driver:{driverId}
     */
    public static String feedbackByDriverId(Long driverId) {
        return "feedback:driver:" + driverId;
    }

    /**
     * Recent feedback by driver: feedback:driver:{driverId}:recent:{limit}
     */
    public static String recentFeedbackByDriverId(Long driverId, int limit) {
        return "feedback:driver:" + driverId + ":recent:" + limit;
    }

    /**
     * Negative feedback by driver: feedback:driver:{driverId}:negative
     */
    public static String negativeFeedbackByDriverId(Long driverId) {
        return "feedback:driver:" + driverId + ":negative";
    }

    /**
     * Positive feedback by driver: feedback:driver:{driverId}:positive
     */
    public static String positiveFeedbackByDriverId(Long driverId) {
        return "feedback:driver:" + driverId + ":positive";
    }

    /**
     * Feedback requiring attention: feedback:attention
     */
    public static final String FEEDBACK_REQUIRING_ATTENTION = "feedback:attention";

    /**
     * Unprocessed feedback: feedback:unprocessed
     */
    public static final String UNPROCESSED_FEEDBACK = "feedback:unprocessed";

    /**
     * Feedback sentiment distribution: feedback:driver:{driverId}:sentiment:distribution
     */
    public static String feedbackSentimentDistribution(Long driverId) {
        return "feedback:driver:" + driverId + ":sentiment:distribution";
    }

    /**
     * Average sentiment score: feedback:driver:{driverId}:avg:sentiment
     */
    public static String averageSentimentScore(Long driverId) {
        return "feedback:driver:" + driverId + ":avg:sentiment";
    }

    // ========== Alert Cache Keys ==========

    /**
     * Alert by ID: alert:id:{alertId}
     */
    public static String alertById(Long alertId) {
        return "alert:id:" + alertId;
    }

    /**
     * Alerts by driver: alert:driver:{driverId}
     */
    public static String alertsByDriverId(Long driverId) {
        return "alert:driver:" + driverId;
    }

    /**
     * Active alerts by driver: alert:driver:{driverId}:active
     */
    public static String activeAlertsByDriverId(Long driverId) {
        return "alert:driver:" + driverId + ":active";
    }

    /**
     * All active alerts: alert:active
     */
    public static final String ACTIVE_ALERTS = "alert:active";

    /**
     * Critical alerts: alert:critical
     */
    public static final String CRITICAL_ALERTS = "alert:critical";

    /**
     * Unacknowledged alerts: alert:unacknowledged
     */
    public static final String UNACKNOWLEDGED_ALERTS = "alert:unacknowledged";

    /**
     * Overdue alerts: alert:overdue
     */
    public static final String OVERDUE_ALERTS = "alert:overdue";

    /**
     * Alerts by manager: alert:manager:{managerId}
     */
    public static String alertsByManager(Long managerId) {
        return "alert:manager:" + managerId;
    }

    /**
     * Driver has active alert flag: alert:driver:{driverId}:has:active
     */
    public static String driverHasActiveAlert(Long driverId) {
        return "alert:driver:" + driverId + ":has:active";
    }

    // ========== Sentiment Analysis Cache Keys ==========

    /**
     * Sentiment analysis result: sentiment:analysis:{feedbackId}
     */
    public static String sentimentAnalysisResult(Long feedbackId) {
        return "sentiment:analysis:" + feedbackId;
    }

    /**
     * Sentiment model version: sentiment:model:version
     */
    public static final String SENTIMENT_MODEL_VERSION = "sentiment:model:version";

    /**
     * Sentiment keywords: sentiment:keywords:{feedbackId}
     */
    public static String sentimentKeywords(Long feedbackId) {
        return "sentiment:keywords:" + feedbackId;
    }

    // ========== Statistics Cache Keys ==========

    /**
     * Overall statistics: stats:overall
     */
    public static final String OVERALL_STATISTICS = "stats:overall";

    /**
     * Daily statistics: stats:daily:{date}
     */
    public static String dailyStatistics(String date) {
        return "stats:daily:" + date;
    }

    /**
     * Driver statistics summary: stats:driver:{driverId}:summary
     */
    public static String driverStatisticsSummary(Long driverId) {
        return "stats:driver:" + driverId + ":summary";
    }

    /**
     * EMA score distribution: stats:ema:distribution
     */
    public static final String EMA_SCORE_DISTRIBUTION = "stats:ema:distribution";

    /**
     * Alert statistics: stats:alerts
     */
    public static final String ALERT_STATISTICS = "stats:alerts";

    /**
     * Feedback statistics: stats:feedback
     */
    public static final String FEEDBACK_STATISTICS = "stats:feedback";

    // ========== Session and Token Keys ==========

    /**
     * User session: session:user:{userId}
     */
    public static String userSession(Long userId) {
        return "session:user:" + userId;
    }

    /**
     * JWT token: token:jwt:{userId}
     */
    public static String jwtToken(Long userId) {
        return "token:jwt:" + userId;
    }

    /**
     * Refresh token: token:refresh:{userId}
     */
    public static String refreshToken(Long userId) {
        return "token:refresh:" + userId;
    }

    /**
     * Blacklisted token: token:blacklist:{token}
     */
    public static String blacklistedToken(String token) {
        return "token:blacklist:" + token;
    }

    // ========== Rate Limiting Keys ==========

    /**
     * Rate limit for API endpoint: ratelimit:api:{endpoint}:{userId}
     */
    public static String apiRateLimit(String endpoint, Long userId) {
        return "ratelimit:api:" + endpoint + ":" + userId;
    }

    /**
     * Rate limit for feedback submission: ratelimit:feedback:{userId}
     */
    public static String feedbackSubmissionRateLimit(Long userId) {
        return "ratelimit:feedback:" + userId;
    }

    // ========== Lock Keys (for distributed locking) ==========

    /**
     * Lock for driver stats update: lock:stats:{driverId}
     */
    public static String driverStatsLock(Long driverId) {
        return "lock:stats:" + driverId;
    }

    /**
     * Lock for alert creation: lock:alert:{driverId}
     */
    public static String alertCreationLock(Long driverId) {
        return "lock:alert:" + driverId;
    }

    /**
     * Lock for feedback processing: lock:feedback:{feedbackId}
     */
    public static String feedbackProcessingLock(Long feedbackId) {
        return "lock:feedback:" + feedbackId;
    }

    // ========== Helper Methods ==========

    /**
     * Get pattern for all keys of a specific type
     */
    public static String allKeysPattern(String prefix) {
        return prefix + ":*";
    }

    /**
     * Get pattern for driver-specific keys
     */
    public static String driverKeysPattern(Long driverId) {
        return "*:driver:" + driverId + "*";
    }

    /**
     * Get pattern for user-specific keys
     */
    public static String userKeysPattern(Long userId) {
        return "*:user:" + userId + "*";
    }
}
