package com.moveinsync.sentiment.model.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Feedback Processed Event
 * 
 * Represents a feedback after sentiment analysis is complete.
 * Published to 'feedback.processed' topic after sentiment scoring.
 * 
 * Flow:
 * FeedbackEvent → SentimentAnalysis → FeedbackProcessedEvent → DriverStatsUpdate
 * 
 * Event Schema Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackProcessedEvent {

    /**
     * Original feedback ID
     */
    private UUID feedbackId;

    /**
     * Driver ID (null if not driver feedback)
     */
    private UUID driverId;

    /**
     * Trip ID (null if not trip feedback)
     */
    private UUID tripId;

    /**
     * User who submitted feedback
     */
    private UUID userId;

    /**
     * Type of feedback: DRIVER, TRIP, APP, MARSHAL
     */
    private String feedbackType;

    /**
     * Original feedback text
     */
    private String feedbackText;

    /**
     * Numerical rating (1-5)
     */
    private Integer rating;

    /**
     * Sentiment Analysis Results
     */
    
    /**
     * Sentiment score (-1.0 to +1.0)
     * -1.0 = Very Negative, 0 = Neutral, +1.0 = Very Positive
     */
    private Double sentimentScore;

    /**
     * Sentiment label: POSITIVE, NEUTRAL, NEGATIVE
     */
    private String sentimentLabel;

    /**
     * Confidence level (0.0 to 1.0)
     * How confident the model is in the sentiment classification
     */
    private Double confidence;

    /**
     * Detected keywords (comma-separated)
     * Example: "rude,late,unsafe"
     */
    private String keywords;

    /**
     * Whether this feedback requires immediate attention
     */
    private Boolean requiresAttention;

    /**
     * Timestamps
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime submittedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processedAt;

    /**
     * Event tracking
     */
    private String eventId;
    private String eventVersion;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventTimestamp;

    /**
     * Factory method to create processed event
     */
    public static FeedbackProcessedEvent createFromFeedback(
            FeedbackEvent feedback,
            Double sentimentScore,
            String sentimentLabel,
            Double confidence,
            String keywords,
            Boolean requiresAttention) {
        
        return FeedbackProcessedEvent.builder()
                .feedbackId(feedback.getFeedbackId())
                .driverId(feedback.getDriverId())
                .tripId(feedback.getTripId())
                .userId(feedback.getUserId())
                .feedbackType(feedback.getFeedbackType())
                .feedbackText(feedback.getFeedbackText())
                .rating(feedback.getRating())
                .sentimentScore(sentimentScore)
                .sentimentLabel(sentimentLabel)
                .confidence(confidence)
                .keywords(keywords)
                .requiresAttention(requiresAttention)
                .submittedAt(feedback.getSubmittedAt())
                .processedAt(LocalDateTime.now())
                .eventId(UUID.randomUUID().toString())
                .eventVersion("1.0")
                .eventTimestamp(LocalDateTime.now())
                .build();
    }
}
