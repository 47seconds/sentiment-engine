package com.moveinsync.sentiment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for resolving an alert
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResolveRequest {

    @NotNull(message = "Resolved by user ID is required")
    private Long resolvedBy;

    @NotBlank(message = "Resolution notes are required")
    private String resolutionNotes;
}
