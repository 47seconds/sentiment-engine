package com.moveinsync.sentiment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Feedback entity representing employee/trip/app/marshal feedback submissions
 */
@Entity
@Table(name = "feedback", indexes = {
    @Index(name = "idx_feedback_driver", columnList = "driver_id"),  // Keep column name for DB compatibility
    @Index(name = "idx_feedback_trip", columnList = "trip_id"),
    @Index(name = "idx_feedback_user", columnList = "user_id"),
    @Index(name = "idx_feedback_type", columnList = "feedback_type"),
    @Index(name = "idx_feedback_status", columnList = "status"),
    @Index(name = "idx_feedback_sentiment", columnList = "sentiment_label"),
    @Index(name = "idx_feedback_created", columnList = "created_at"),
    @Index(name = "idx_feedback_attention", columnList = "requires_attention")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "driver_id", nullable = false)
    private Long driverId;

    @Column(name = "trip_id")
    private Long tripId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type", nullable = false, length = 30)
    private FeedbackType feedbackType;

    @NotBlank(message = "Feedback text is required")
    @Size(min = 10, max = 2000, message = "Feedback must be between 10 and 2000 characters")
    @Column(name = "feedback_text", nullable = false, columnDefinition = "TEXT")
    private String feedbackText;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Column(name = "rating")
    private Integer rating;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    @Builder.Default
    private FeedbackSource source = FeedbackSource.MOBILE_APP;

    @Column(name = "sentiment_score")
    private Double sentimentScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment_label", length = 20)
    private SentimentLabel sentimentLabel;

    @Column(name = "confidence")
    private Double confidence;

    @ElementCollection
    @CollectionTable(name = "feedback_keywords", joinColumns = @JoinColumn(name = "feedback_id"))
    @Column(name = "keyword")
    @Builder.Default
    private List<String> keywords = List.of();

    @Column(name = "requires_attention", nullable = false)
    @Builder.Default
    private Boolean requiresAttention = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private FeedbackStatus status = FeedbackStatus.SUBMITTED;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

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
     * Feedback types
     */
    public enum FeedbackType {
        EMPLOYEE,             // Employee conduct, professionalism (renamed from DRIVER_BEHAVIOR)
        DRIVING_SAFETY,       // Speed, braking, route choice
        VEHICLE_CONDITION,    // Cleanliness, AC, seat comfort
        ROUTE_NAVIGATION,     // Route accuracy, delays
        TRIP,                 // Trip-specific feedback, delays, route issues
        MOBILE_APP,           // App experience, bugs, features
        MARSHAL,              // Marshal conduct, helpfulness, professionalism
        GENERAL_EXPERIENCE,   // Overall experience
        POSITIVE_PRAISE,      // Compliments and appreciation
        COMPLAINT             // Issues and problems
    }

    /**
     * Feedback sources
     */
    public enum FeedbackSource {
        MOBILE_APP,
        WEB_PORTAL,
        EMAIL,
        CALL_CENTER,
        SMS,
        CHATBOT,
        IN_APP_SURVEY
    }

    /**
     * Sentiment labels
     */
    public enum SentimentLabel {
        VERY_POSITIVE,    // Score > 0.6
        POSITIVE,         // Score 0.2 to 0.6
        NEUTRAL,          // Score -0.2 to 0.2
        NEGATIVE,         // Score -0.6 to -0.2
        VERY_NEGATIVE     // Score < -0.6
    }

    /**
     * Feedback processing status
     */
    public enum FeedbackStatus {
        SUBMITTED,        // Just received
        PROCESSING,       // Sentiment analysis in progress
        PROCESSED,        // Analysis complete
        UNDER_REVIEW,     // Being reviewed by human
        REVIEWED,         // Review complete
        ACTIONED,         // Action taken based on feedback
        CLOSED            // No further action needed
    }

    /**
     * Update sentiment analysis results
     */
    public void updateSentimentAnalysis(Double score, SentimentLabel label, Double confidence, List<String> keywords) {
        this.sentimentScore = score;
        this.sentimentLabel = label;
        this.confidence = confidence;
        this.keywords = keywords;
        this.processedAt = LocalDateTime.now();
        this.status = FeedbackStatus.PROCESSED;
        
        // Flag for attention if very negative or low confidence
        this.requiresAttention = label == SentimentLabel.VERY_NEGATIVE || 
                                 label == SentimentLabel.NEGATIVE || 
                                 (confidence != null && confidence < 0.7);
    }

    /**
     * Mark as reviewed
     */
    public void markAsReviewed(Long reviewerId, String notes) {
        this.reviewedAt = LocalDateTime.now();
        this.reviewedBy = reviewerId;
        this.reviewNotes = notes;
        this.status = FeedbackStatus.REVIEWED;
    }

    /**
     * Check if feedback is negative
     */
    public boolean isNegative() {
        return sentimentLabel == SentimentLabel.NEGATIVE || 
               sentimentLabel == SentimentLabel.VERY_NEGATIVE;
    }

    /**
     * Check if feedback is positive
     */
    public boolean isPositive() {
        return sentimentLabel == SentimentLabel.POSITIVE || 
               sentimentLabel == SentimentLabel.VERY_POSITIVE;
    }

    /**
     * Get sentiment category (for aggregation)
     */
    public String getSentimentCategory() {
        if (sentimentLabel == null) return "UNKNOWN";
        return switch (sentimentLabel) {
            case VERY_POSITIVE, POSITIVE -> "POSITIVE";
            case NEGATIVE, VERY_NEGATIVE -> "NEGATIVE";
            case NEUTRAL -> "NEUTRAL";
        };
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
}
