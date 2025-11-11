package com.moveinsync.sentiment.model.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Driver Stats Updated Event
 * 
 * Published when driver sentiment statistics are recalculated.
 * Sent to 'driver.stats.updated' topic after processing feedback.
 * 
 * Flow:
 * FeedbackProcessedEvent → EMACalculator → DriverStatsUpdatedEvent → AlertService
 * 
 * Event Schema Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverStatsUpdatedEvent {

    /**
     * Driver whose stats were updated
     */
    private UUID driverId;

    /**
     * Statistics
     */
    
    /**
     * Current EMA (Exponential Moving Average) score
     * Range: 1.0 (worst) to 5.0 (best)
     */
    private Double emaScore;

    /**
     * Previous EMA score (before this update)
     */
    private Double previousEmaScore;

    /**
     * Total number of feedback received
     */
    private Integer totalFeedbackCount;

    /**
     * Number of positive feedback (rating >= 4)
     */
    private Integer positiveFeedbackCount;

    /**
     * Number of negative feedback (rating <= 2)
     */
    private Integer negativeFeedbackCount;

    /**
     * Number of neutral feedback (rating == 3)
     */
    private Integer neutralFeedbackCount;

    /**
     * Average sentiment score across all feedback
     */
    private Double averageSentimentScore;

    /**
     * Latest feedback that triggered this update
     */
    private UUID latestFeedbackId;
    private Double latestSentimentScore;
    private Integer latestRating;

    /**
     * Alert Information
     */
    
    /**
     * Whether EMA score crossed below alert threshold
     */
    private Boolean alertTriggered;

    /**
     * Alert severity: LOW, MEDIUM, HIGH, CRITICAL
     * null if no alert
     */
    private String alertSeverity;

    /**
     * EMA score change from previous
     */
    private Double scoreDelta;

    /**
     * Timestamps
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastFeedbackAt;

    /**
     * Event tracking
     */
    private String eventId;
    private String eventVersion;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventTimestamp;

    /**
     * Calculate score delta and alert status
     */
    public void calculateDerivedFields(Double alertThreshold, Double criticalThreshold) {
        // Calculate delta
        if (previousEmaScore != null) {
            this.scoreDelta = emaScore - previousEmaScore;
        }

        // Determine alert status
        if (emaScore <= criticalThreshold) {
            this.alertTriggered = true;
            this.alertSeverity = "CRITICAL";
        } else if (emaScore <= alertThreshold) {
            this.alertTriggered = true;
            this.alertSeverity = "HIGH";
        } else if (emaScore <= (alertThreshold + 0.5)) {
            this.alertTriggered = true;
            this.alertSeverity = "MEDIUM";
        } else {
            this.alertTriggered = false;
            this.alertSeverity = null;
        }
    }

    /**
     * Factory method
     */
    public static DriverStatsUpdatedEvent create(
            UUID driverId,
            Double emaScore,
            Double previousEmaScore,
            Integer totalFeedbackCount,
            Integer positiveFeedbackCount,
            Integer negativeFeedbackCount,
            Integer neutralFeedbackCount,
            Double averageSentimentScore,
            UUID latestFeedbackId,
            Double latestSentimentScore,
            Integer latestRating,
            LocalDateTime lastFeedbackAt) {
        
        DriverStatsUpdatedEvent event = DriverStatsUpdatedEvent.builder()
                .driverId(driverId)
                .emaScore(emaScore)
                .previousEmaScore(previousEmaScore)
                .totalFeedbackCount(totalFeedbackCount)
                .positiveFeedbackCount(positiveFeedbackCount)
                .negativeFeedbackCount(negativeFeedbackCount)
                .neutralFeedbackCount(neutralFeedbackCount)
                .averageSentimentScore(averageSentimentScore)
                .latestFeedbackId(latestFeedbackId)
                .latestSentimentScore(latestSentimentScore)
                .latestRating(latestRating)
                .updatedAt(LocalDateTime.now())
                .lastFeedbackAt(lastFeedbackAt)
                .eventId(UUID.randomUUID().toString())
                .eventVersion("1.0")
                .eventTimestamp(LocalDateTime.now())
                .build();
        
        return event;
    }
}
