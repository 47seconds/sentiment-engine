package com.moveinsync.sentiment.util;

import com.moveinsync.sentiment.dto.*;
import com.moveinsync.sentiment.model.*;
import org.springframework.stereotype.Component;

/**
 * Utility class for mapping between entities and DTOs
 */
@Component
public class EntityMapper {

    /**
     * Convert Feedback entity to FeedbackResponse DTO
     */
    public FeedbackResponse toFeedbackResponse(Feedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .driverId(feedback.getDriverId())
                .tripId(feedback.getTripId())
                .userId(feedback.getUserId())
                .feedbackType(feedback.getFeedbackType() != null ? feedback.getFeedbackType().name() : null)
                .feedbackText(feedback.getFeedbackText())
                .rating(feedback.getRating())
                .source(feedback.getSource() != null ? feedback.getSource().name() : null)
                .sentimentScore(feedback.getSentimentScore())
                .sentimentLabel(feedback.getSentimentLabel() != null ? feedback.getSentimentLabel().name() : null)
                .confidence(feedback.getConfidence())
                .keywords(feedback.getKeywords())
                .requiresAttention(feedback.getRequiresAttention())
                .status(feedback.getStatus() != null ? feedback.getStatus().name() : null)
                .createdAt(feedback.getCreatedAt())
                .processedAt(feedback.getProcessedAt())
                .build();
    }

    /**
     * Convert FeedbackSubmitRequest DTO to Feedback entity
     */
    public Feedback toFeedback(FeedbackSubmitRequest request) {
        // Use the flexible entityId pattern, falling back to driverId for compatibility
        Long targetEntityId = request.getEntityIdForType();
        
        return Feedback.builder()
                .driverId(targetEntityId != null ? targetEntityId : request.getDriverId())
                .tripId(request.getTripId())
                .userId(request.getUserId())
                .feedbackType(Feedback.FeedbackType.valueOf(request.getFeedbackType()))
                .feedbackText(request.getFeedbackText())
                .rating(request.getRating())
                .source(Feedback.FeedbackSource.valueOf(request.getSource()))
                .build();
    }

    /**
     * Convert DriverStats entity to DriverStatsResponse DTO
     */
    public DriverStatsResponse toDriverStatsResponse(DriverStats stats) {
        return DriverStatsResponse.builder()
                .id(stats.getId())
                .driverId(stats.getDriverId())
                .emaScore(stats.getEmaScore())
                .previousEmaScore(stats.getPreviousEmaScore())
                .totalFeedbackCount(stats.getTotalFeedbackCount())
                .positiveFeedbackCount(stats.getPositiveFeedbackCount())
                .negativeFeedbackCount(stats.getNegativeFeedbackCount())
                .neutralFeedbackCount(stats.getNeutralFeedbackCount())
                .averageRating(stats.getAverageRating())
                .alertStatus(stats.getAlertStatus() != null ? stats.getAlertStatus().name() : null)
                .lastAlertSeverity(stats.getLastAlertSeverity() != null ? stats.getLastAlertSeverity().name() : null)
                .consecutiveNegativeFeedback(stats.getConsecutiveNegativeFeedback())
                .sentimentTrend(stats.getSentimentTrend())
                .positiveFeedbackPercentage(stats.getPositiveFeedbackPercentage())
                .negativeFeedbackPercentage(stats.getNegativeFeedbackPercentage())
                .build();
    }

    /**
     * Convert Alert entity to AlertResponse DTO
     */
    public AlertResponse toAlertResponse(Alert alert) {
        return AlertResponse.builder()
                .id(alert.getId())
                .driverId(alert.getDriverId())
                .alertType(alert.getAlertType() != null ? alert.getAlertType().name() : null)
                .severity(alert.getSeverity() != null ? alert.getSeverity().name() : null)
                .currentEmaScore(alert.getCurrentEmaScore())
                .previousEmaScore(alert.getPreviousEmaScore())
                .scoreDrop(alert.getScoreDrop())
                .recommendedAction(alert.getRecommendedAction() != null ? alert.getRecommendedAction().name() : null)
                .status(alert.getStatus() != null ? alert.getStatus().name() : null)
                .assignedTo(alert.getAssignedTo())
                .createdAt(alert.getCreatedAt())
                .acknowledgedAt(alert.getAcknowledgedAt())
                .resolvedAt(alert.getResolvedAt())
                .resolutionNotes(alert.getResolutionNotes())
                .notificationSent(alert.getNotificationSent())
                .relatedFeedbackIds(alert.getRelatedFeedbackIds())
                .build();
    }
}
