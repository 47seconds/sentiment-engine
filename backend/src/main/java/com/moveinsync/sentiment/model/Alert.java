package com.moveinsync.sentiment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Alert entity for tracking low sentiment alerts and notifications
 */
@Entity
@Table(name = "alerts", indexes = {
    @Index(name = "idx_alert_driver", columnList = "driver_id"),
    @Index(name = "idx_alert_type", columnList = "alert_type"),
    @Index(name = "idx_alert_severity", columnList = "severity"),
    @Index(name = "idx_alert_status", columnList = "status"),
    @Index(name = "idx_alert_created", columnList = "created_at"),
    @Index(name = "idx_alert_resolved", columnList = "resolved_at"),
    @Index(name = "idx_alert_cooldown", columnList = "cooldown_expires_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Driver ID is required")
    private Long driverId;

    @Enumerated(EnumType.STRING)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    private AlertSeverity severity;

    @Column(name = "current_ema_score", nullable = false)
    private Double currentEmaScore;

    @Column(name = "previous_ema_score")
    private Double previousEmaScore;

    private Double scoreDrop;

    @Column(name = "threshold_value")
    private Double thresholdValue;

    @Column(name = "alert_message", columnDefinition = "TEXT")
    private String alertMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommended_action", length = 30)
    private RecommendedAction recommendedAction;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AlertStatus status = AlertStatus.ACTIVE;

    @Column(name = "assigned_to")
    private Long assignedTo;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "acknowledged_by")
    private Long acknowledgedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_by")
    private Long resolvedBy;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "cooldown_expires_at")
    private LocalDateTime cooldownExpiresAt;

    @Column(name = "notification_sent", nullable = false)
    @Builder.Default
    private Boolean notificationSent = false;

    @Column(name = "notification_sent_at")
    private LocalDateTime notificationSentAt;

    @Column(name = "related_feedback_ids", columnDefinition = "TEXT")
    private String relatedFeedbackIds;

    @Column(name = "metadata", columnDefinition = "TEXT")
    @Convert(converter = MapToJsonConverter.class)
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Alert types
     */
    public enum AlertType {
        LOW_SENTIMENT_SCORE,           // EMA below threshold
        SUDDEN_SCORE_DROP,             // Rapid decrease in EMA
        CONSECUTIVE_NEGATIVE_FEEDBACK, // Multiple negative feedbacks in a row
        HIGH_NEGATIVE_PERCENTAGE,      // Too many negative feedbacks
        VERY_NEGATIVE_FEEDBACK,        // Single very negative feedback
        REPEATED_COMPLAINTS,           // Same issue reported multiple times
        LOW_RATING_TREND               // Declining rating pattern
    }

    /**
     * Alert severity levels
     */
    public enum AlertSeverity {
        LOW,        // Minor issue, monitor
        MEDIUM,     // Needs attention soon
        HIGH,       // Immediate attention required
        CRITICAL    // Urgent action needed
    }

    /**
     * Alert status
     */
    public enum AlertStatus {
        ACTIVE,         // Alert raised
        ACKNOWLEDGED,   // Seen by manager
        IN_PROGRESS,    // Being addressed
        RESOLVED,       // Issue resolved
        DISMISSED,      // False positive or no action needed
        ESCALATED       // Escalated to higher management
    }

    /**
     * Recommended actions
     */
    public enum RecommendedAction {
        REVIEW_DRIVER_PROFILE,
        CONTACT_DRIVER,
        SCHEDULE_TRAINING,
        ASSIGN_MENTOR,
        SUSPEND_TEMPORARILY,
        INVESTIGATE_FURTHER,
        MONITOR_CLOSELY,
        NO_ACTION_NEEDED
    }

    /**
     * Acknowledge alert
     */
    public void acknowledge(Long managerId) {
        this.acknowledgedAt = LocalDateTime.now();
        this.acknowledgedBy = managerId;
        this.status = AlertStatus.ACKNOWLEDGED;
    }

    /**
     * Assign alert to manager
     */
    public void assignTo(Long managerId) {
        this.assignedTo = managerId;
        this.status = AlertStatus.IN_PROGRESS;
    }

    /**
     * Resolve alert
     */
    public void resolve(Long managerId, String notes) {
        this.resolvedAt = LocalDateTime.now();
        this.resolvedBy = managerId;
        this.resolutionNotes = notes;
        this.status = AlertStatus.RESOLVED;
    }

    /**
     * Dismiss alert
     */
    public void dismiss(Long managerId, String reason) {
        this.resolvedAt = LocalDateTime.now();
        this.resolvedBy = managerId;
        this.resolutionNotes = "Dismissed: " + reason;
        this.status = AlertStatus.DISMISSED;
    }

    /**
     * Escalate alert
     */
    public void escalate(String reason) {
        this.status = AlertStatus.ESCALATED;
        this.addMetadata("escalation_reason", reason);
        this.addMetadata("escalated_at", LocalDateTime.now().toString());
    }

    /**
     * Mark notification as sent
     */
    public void markNotificationSent() {
        this.notificationSent = true;
        this.notificationSentAt = LocalDateTime.now();
    }

    /**
     * Check if alert is in cooldown period
     */
    public boolean isInCooldown() {
        return this.cooldownExpiresAt != null && 
               LocalDateTime.now().isBefore(this.cooldownExpiresAt);
    }

    /**
     * Check if alert is active
     */
    public boolean isActive() {
        return this.status == AlertStatus.ACTIVE || 
               this.status == AlertStatus.ACKNOWLEDGED ||
               this.status == AlertStatus.IN_PROGRESS;
    }

    /**
     * Check if alert is resolved
     */
    public boolean isResolved() {
        return this.status == AlertStatus.RESOLVED || 
               this.status == AlertStatus.DISMISSED;
    }

    /**
     * Get alert age in hours
     */
    public long getAlertAgeInHours() {
        return java.time.Duration.between(this.createdAt, LocalDateTime.now()).toHours();
    }

    /**
     * Check if alert is overdue (active for more than 24 hours)
     */
    public boolean isOverdue() {
        return isActive() && getAlertAgeInHours() > 24;
    }

    /**
     * Add metadata entry
     */
    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }

    /**
     * Generate human-readable alert message
     */
    public static String generateAlertMessage(AlertType type, AlertSeverity severity, Double emaScore) {
        return switch (severity) {
            case CRITICAL -> String.format("CRITICAL: %s - Driver sentiment score is critically low (%.2f). Immediate action required!", 
                                          type.toString().replace("_", " "), emaScore);
            case HIGH -> String.format("HIGH PRIORITY: %s - Driver sentiment score is low (%.2f). Urgent attention needed.", 
                                       type.toString().replace("_", " "), emaScore);
            case MEDIUM -> String.format("MEDIUM: %s - Driver sentiment score dropped to %.2f. Please review.", 
                                         type.toString().replace("_", " "), emaScore);
            case LOW -> String.format("LOW: %s - Minor sentiment score change to %.2f. Monitor closely.", 
                                      type.toString().replace("_", " "), emaScore);
        };
    }
}
