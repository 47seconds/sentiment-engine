package com.moveinsync.sentiment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for alerts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponse {

    private Long id;
    private Long driverId;
    private String alertType;
    private String severity;
    private Double currentEmaScore;
    private Double previousEmaScore;
    private Double scoreDrop;
    private String recommendedAction;
    private String status;
    private Long assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime resolvedAt;
    private String resolutionNotes;
    private Boolean notificationSent;
    private String relatedFeedbackIds;
}
