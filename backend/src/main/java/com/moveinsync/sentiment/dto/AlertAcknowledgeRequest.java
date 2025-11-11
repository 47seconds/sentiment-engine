package com.moveinsync.sentiment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for acknowledging an alert
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertAcknowledgeRequest {

    @NotNull(message = "Acknowledged by user ID is required")
    private Long acknowledgedBy;

    private String notes;
}
