package com.moveinsync.sentiment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Driver statistics entity tracking sentiment EMA and feedback metrics
 */
@Entity
@Table(name = "driver_stats", indexes = {
    @Index(name = "idx_driver_stats_driver", columnList = "driver_id", unique = true),
    @Index(name = "idx_driver_stats_ema", columnList = "ema_score"),
    @Index(name = "idx_driver_stats_alert", columnList = "alert_status"),
    @Index(name = "idx_driver_stats_updated", columnList = "last_updated_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Driver ID is required")
    @Column(name = "driver_id", nullable = false, unique = true)
    private Long driverId;

    @Min(value = -1, message = "EMA score must be >= -1")
    @Max(value = 1, message = "EMA score must be <= 1")
    @Column(name = "ema_score", nullable = false)
    @Builder.Default
    private Double emaScore = 0.0;

    @Column(name = "previous_ema_score")
    private Double previousEmaScore;

    @Column(name = "total_feedback_count", nullable = false)
    @Builder.Default
    private Integer totalFeedbackCount = 0;

    @Column(name = "positive_feedback_count", nullable = false)
    @Builder.Default
    private Integer positiveFeedbackCount = 0;

    @Column(name = "negative_feedback_count", nullable = false)
    @Builder.Default
    private Integer negativeFeedbackCount = 0;

    @Column(name = "neutral_feedback_count", nullable = false)
    @Builder.Default
    private Integer neutralFeedbackCount = 0;

    @Column(name = "very_positive_count", nullable = false)
    @Builder.Default
    private Integer veryPositiveCount = 0;

    @Column(name = "very_negative_count", nullable = false)
    @Builder.Default
    private Integer veryNegativeCount = 0;

    @Column(name = "average_rating")
    private Double averageRating;

    @Column(name = "total_ratings_count")
    @Builder.Default
    private Integer totalRatingsCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_status", length = 20)
    private AlertStatus alertStatus;

    @Column(name = "alert_triggered_at")
    private LocalDateTime alertTriggeredAt;

    @Column(name = "alert_count", nullable = false)
    @Builder.Default
    private Integer alertCount = 0;

    @Column(name = "last_alert_severity", length = 20)
    @Enumerated(EnumType.STRING)
    private AlertSeverity lastAlertSeverity;

    @Column(name = "consecutive_negative_feedback")
    @Builder.Default
    private Integer consecutiveNegativeFeedback = 0;

    @Column(name = "last_positive_feedback_at")
    private LocalDateTime lastPositiveFeedbackAt;

    @Column(name = "last_negative_feedback_at")
    private LocalDateTime lastNegativeFeedbackAt;

    @Column(name = "last_feedback_at")
    private LocalDateTime lastFeedbackAt;

    @Column(name = "ema_alpha", nullable = false)
    @Builder.Default
    private Double emaAlpha = 0.2; // Smoothing factor for EMA

    @Column(name = "stats_calculation_version")
    @Builder.Default
    private Integer statsCalculationVersion = 1;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    /**
     * Alert status for driver
     */
    public enum AlertStatus {
        NORMAL,           // No issues
        WARNING,          // Approaching threshold
        CRITICAL,         // Below threshold, action needed
        UNDER_REVIEW,     // Being reviewed by manager
        RESOLVED          // Issue resolved
    }

    /**
     * Alert severity levels
     */
    public enum AlertSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    /**
     * Update EMA score with new sentiment
     */
    public void updateEmaScore(Double newSentimentScore) {
        this.previousEmaScore = this.emaScore;
        this.emaScore = (emaAlpha * newSentimentScore) + ((1 - emaAlpha) * this.emaScore);
    }

    /**
     * Increment feedback counts based on sentiment
     */
    public void incrementFeedbackCount(Feedback.SentimentLabel sentimentLabel) {
        this.totalFeedbackCount++;
        this.lastFeedbackAt = LocalDateTime.now();

        switch (sentimentLabel) {
            case VERY_POSITIVE:
                this.veryPositiveCount++;
                this.positiveFeedbackCount++;
                this.consecutiveNegativeFeedback = 0;
                this.lastPositiveFeedbackAt = LocalDateTime.now();
                break;
            case POSITIVE:
                this.positiveFeedbackCount++;
                this.consecutiveNegativeFeedback = 0;
                this.lastPositiveFeedbackAt = LocalDateTime.now();
                break;
            case NEUTRAL:
                this.neutralFeedbackCount++;
                this.consecutiveNegativeFeedback = 0;
                break;
            case NEGATIVE:
                this.negativeFeedbackCount++;
                this.consecutiveNegativeFeedback++;
                this.lastNegativeFeedbackAt = LocalDateTime.now();
                break;
            case VERY_NEGATIVE:
                this.veryNegativeCount++;
                this.negativeFeedbackCount++;
                this.consecutiveNegativeFeedback++;
                this.lastNegativeFeedbackAt = LocalDateTime.now();
                break;
        }
    }

    /**
     * Update average rating
     */
    public void updateAverageRating(Integer newRating) {
        if (newRating != null) {
            if (this.averageRating == null) {
                this.averageRating = newRating.doubleValue();
            } else {
                this.averageRating = ((this.averageRating * this.totalRatingsCount) + newRating) 
                                    / (this.totalRatingsCount + 1);
            }
            this.totalRatingsCount++;
        }
    }

    /**
     * Trigger alert
     */
    public void triggerAlert(AlertSeverity severity) {
        this.alertStatus = AlertStatus.CRITICAL;
        this.alertTriggeredAt = LocalDateTime.now();
        this.alertCount++;
        this.lastAlertSeverity = severity;
    }

    /**
     * Resolve alert
     */
    public void resolveAlert() {
        this.alertStatus = AlertStatus.RESOLVED;
    }

    /**
     * Set warning status
     */
    public void setWarningStatus() {
        this.alertStatus = AlertStatus.WARNING;
    }

    /**
     * Clear alert status
     */
    public void clearAlert() {
        this.alertStatus = AlertStatus.NORMAL;
    }

    /**
     * Check if driver needs attention
     */
    public boolean needsAttention() {
        return this.alertStatus == AlertStatus.CRITICAL || 
               this.alertStatus == AlertStatus.WARNING ||
               this.consecutiveNegativeFeedback >= 3;
    }

    /**
     * Get sentiment trend
     */
    public String getSentimentTrend() {
        if (this.previousEmaScore == null) return "STABLE";
        
        double delta = this.emaScore - this.previousEmaScore;
        if (Math.abs(delta) < 0.05) return "STABLE";
        return delta > 0 ? "IMPROVING" : "DECLINING";
    }

    /**
     * Calculate positive feedback percentage
     */
    public double getPositiveFeedbackPercentage() {
        if (this.totalFeedbackCount == 0) return 0.0;
        return (this.positiveFeedbackCount * 100.0) / this.totalFeedbackCount;
    }

    /**
     * Calculate negative feedback percentage
     */
    public double getNegativeFeedbackPercentage() {
        if (this.totalFeedbackCount == 0) return 0.0;
        return (this.negativeFeedbackCount * 100.0) / this.totalFeedbackCount;
    }
}
