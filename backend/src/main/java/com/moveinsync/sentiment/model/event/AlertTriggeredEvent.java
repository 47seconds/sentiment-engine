package com.moveinsync.sentiment.model.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Alert Triggered Event
 * 
 * Published when a driver's sentiment falls below threshold.
 * Sent to 'alert.triggered' topic for notification/action.
 * 
 * Flow:
 * DriverStatsUpdatedEvent → AlertService → AlertTriggeredEvent → NotificationService
 * 
 * Event Schema Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertTriggeredEvent {

    /**
     * Unique alert ID
     */
    private UUID alertId;

    /**
     * Driver for whom alert was triggered
     */
    private UUID driverId;

    /**
     * Alert Details
     */
    
    /**
     * Alert type: SENTIMENT_DROP, LOW_SENTIMENT, CRITICAL_SENTIMENT
     */
    private String alertType;

    /**
     * Severity: LOW, MEDIUM, HIGH, CRITICAL
     */
    private String severity;

    /**
     * Current EMA score that triggered the alert
     */
    private Double currentEmaScore;

    /**
     * Previous EMA score
     */
    private Double previousEmaScore;

    /**
     * EMA score drop (previous - current)
     */
    private Double scoreDrop;

    /**
     * Alert threshold that was crossed
     */
    private Double threshold;

    /**
     * Human-readable alert message
     */
    private String alertMessage;

    /**
     * Recommended action: REVIEW, CONTACT_DRIVER, SUSPEND, INVESTIGATE
     */
    private String recommendedAction;

    /**
     * Context Information
     */
    
    /**
     * Recent feedback count that contributed to this score
     */
    private Integer recentFeedbackCount;

    /**
     * Number of negative feedback in recent period
     */
    private Integer recentNegativeFeedbackCount;

    /**
     * Latest feedback ID that triggered threshold crossing
     */
    private UUID triggerFeedbackId;

    /**
     * Latest feedback text (for context)
     */
    private String triggerFeedbackText;

    /**
     * Latest feedback sentiment score
     */
    private Double triggerSentimentScore;

    /**
     * Alert State
     */
    
    /**
     * Is this alert acknowledged by admin?
     */
    private Boolean isAcknowledged;

    /**
     * When can next alert be triggered (cooldown)
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime cooldownExpiresAt;

    /**
     * Timestamps
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime triggeredAt;

    /**
     * Event tracking
     */
    private String eventId;
    private String eventVersion;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventTimestamp;

    /**
     * Factory method to create alert event
     */
    public static AlertTriggeredEvent create(
            UUID driverId,
            String alertType,
            String severity,
            Double currentEmaScore,
            Double previousEmaScore,
            Double threshold,
            String alertMessage,
            String recommendedAction,
            Integer recentFeedbackCount,
            Integer recentNegativeFeedbackCount,
            UUID triggerFeedbackId,
            String triggerFeedbackText,
            Double triggerSentimentScore,
            LocalDateTime cooldownExpiresAt) {
        
        Double scoreDrop = previousEmaScore != null ? previousEmaScore - currentEmaScore : 0.0;
        
        return AlertTriggeredEvent.builder()
                .alertId(UUID.randomUUID())
                .driverId(driverId)
                .alertType(alertType)
                .severity(severity)
                .currentEmaScore(currentEmaScore)
                .previousEmaScore(previousEmaScore)
                .scoreDrop(scoreDrop)
                .threshold(threshold)
                .alertMessage(alertMessage)
                .recommendedAction(recommendedAction)
                .recentFeedbackCount(recentFeedbackCount)
                .recentNegativeFeedbackCount(recentNegativeFeedbackCount)
                .triggerFeedbackId(triggerFeedbackId)
                .triggerFeedbackText(triggerFeedbackText)
                .triggerSentimentScore(triggerSentimentScore)
                .isAcknowledged(false)
                .cooldownExpiresAt(cooldownExpiresAt)
                .triggeredAt(LocalDateTime.now())
                .eventId(UUID.randomUUID().toString())
                .eventVersion("1.0")
                .eventTimestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Generate alert message based on severity
     */
    public static String generateAlertMessage(String severity, Double emaScore, String driverName) {
        return switch (severity) {
            case "CRITICAL" -> String.format(
                "CRITICAL: Driver %s sentiment score dropped to %.2f. Immediate action required.",
                driverName, emaScore
            );
            case "HIGH" -> String.format(
                "HIGH ALERT: Driver %s sentiment score is %.2f. Review recommended.",
                driverName, emaScore
            );
            case "MEDIUM" -> String.format(
                "MEDIUM ALERT: Driver %s sentiment score is %.2f. Monitor closely.",
                driverName, emaScore
            );
            default -> String.format(
                "LOW ALERT: Driver %s sentiment score is %.2f.",
                driverName, emaScore
            );
        };
    }

    /**
     * Determine recommended action based on severity
     */
    public static String getRecommendedAction(String severity) {
        return switch (severity) {
            case "CRITICAL" -> "SUSPEND";
            case "HIGH" -> "CONTACT_DRIVER";
            case "MEDIUM" -> "REVIEW";
            default -> "MONITOR";
        };
    }
}
