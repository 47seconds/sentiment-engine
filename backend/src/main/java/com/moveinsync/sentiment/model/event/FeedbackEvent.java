package com.moveinsync.sentiment.model.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Feedback Event
 * 
 * Represents a feedback submission event sent to Kafka.
 * This is published to 'feedback.submitted' topic when a user submits feedback.
 * 
 * Lifecycle:
 * 1. User submits feedback via API
 * 2. FeedbackController publishes FeedbackEvent to Kafka
 * 3. FeedbackConsumer processes event and analyzes sentiment
 * 4. FeedbackProcessedEvent is published with sentiment scores
 * 
 * Event Schema Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackEvent {

    /**
     * Unique identifier for this feedback
     */
    private UUID feedbackId;

    /**
     * ID of the driver being reviewed (for DRIVER feedback)
     * Null for TRIP, APP, MARSHAL feedback
     */
    private UUID driverId;

    /**
     * ID of the trip (for TRIP feedback)
     * Null for DRIVER, APP, MARSHAL feedback
     */
    private UUID tripId;

    /**
     * User who submitted the feedback
     */
    private UUID userId;

    /**
     * Type of feedback: DRIVER, TRIP, APP, MARSHAL
     */
    private String feedbackType;

    /**
     * Raw feedback text (1-1000 characters)
     */
    private String feedbackText;

    /**
     * Numerical rating (1-5 stars)
     */
    private Integer rating;

    /**
     * When the feedback was submitted
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime submittedAt;

    /**
     * Source of the feedback (MOBILE_APP, WEB_PORTAL, VOICE_CALL)
     */
    private String source;

    /**
     * Optional: Trip route for context
     */
    private String route;

    /**
     * Optional: Metadata (JSON string with additional context)
     * Example: {"shift": "morning", "weather": "rainy", "delay_minutes": 15}
     */
    private String metadata;

    /**
     * Event tracking information
     */
    private String eventId;
    private String eventVersion;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventTimestamp;

    /**
     * Factory method to create event with tracking info
     */
    public static FeedbackEvent createEvent(
            UUID feedbackId,
            UUID driverId,
            UUID tripId,
            UUID userId,
            String feedbackType,
            String feedbackText,
            Integer rating,
            LocalDateTime submittedAt,
            String source,
            String route,
            String metadata) {
        
        return FeedbackEvent.builder()
                .feedbackId(feedbackId)
                .driverId(driverId)
                .tripId(tripId)
                .userId(userId)
                .feedbackType(feedbackType)
                .feedbackText(feedbackText)
                .rating(rating)
                .submittedAt(submittedAt)
                .source(source)
                .route(route)
                .metadata(metadata)
                .eventId(UUID.randomUUID().toString())
                .eventVersion("1.0")
                .eventTimestamp(LocalDateTime.now())
                .build();
    }
}
