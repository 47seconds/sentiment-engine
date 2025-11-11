package com.moveinsync.sentiment.dto;

import lombok.*;

/**
 * DTO for Admin Configuration Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminConfigurationResponse {

    // Threshold settings
    private Double criticalThreshold;
    private Double warningThreshold;
    private Integer cooldownPeriod; // minutes

    // Feature flags
    private Boolean driverFeedbackEnabled;
    private Boolean tripFeedbackEnabled;
    private Boolean appFeedbackEnabled;
    private Boolean marshalFeedbackEnabled;

    // Alert settings
    private Integer maxAlertsPerDriver;
    private Integer alertRetentionDays;
    private Boolean autoEscalationEnabled;

    // Notification settings
    private Boolean emailNotificationsEnabled;
    private Boolean smsNotificationsEnabled;
}