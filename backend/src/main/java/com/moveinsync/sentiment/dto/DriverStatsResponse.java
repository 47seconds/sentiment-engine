package com.moveinsync.sentiment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for driver statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverStatsResponse {

    private Long id;
    private Long driverId;
    private Double emaScore;
    private Double previousEmaScore;
    private Integer totalFeedbackCount;
    private Integer positiveFeedbackCount;
    private Integer negativeFeedbackCount;
    private Integer neutralFeedbackCount;
    private Double averageRating;
    private String alertStatus;
    private String lastAlertSeverity;
    private Integer consecutiveNegativeFeedback;
    private String sentimentTrend;
    private Double positiveFeedbackPercentage;
    private Double negativeFeedbackPercentage;
}
