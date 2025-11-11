package com.moveinsync.sentiment.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for submitting feedback
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackSubmitRequest {

    // Legacy field - kept for backward compatibility (maps to entityId for EMPLOYEE type)
    private Long driverId;

    // New flexible fields for different entity types
    private String entityType;  // EMPLOYEE, TRIP, MOBILE_APP, MARSHAL
    private Long entityId;      // ID of the entity being given feedback about

    private Long tripId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Feedback type is required")
    private String feedbackType;

    @NotBlank(message = "Feedback text is required")
    @Size(min = 10, max = 2000, message = "Feedback must be between 10 and 2000 characters")
    private String feedbackText;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @NotBlank(message = "Source is required")
    private String source;
    
    /**
     * Get the entity ID based on entity type
     * This handles backward compatibility with driverId field
     */
    public Long getEntityIdForType() {
        if (entityId != null) {
            return entityId;
        }
        // Fall back to driverId for backward compatibility
        if ("EMPLOYEE".equalsIgnoreCase(entityType) || "DRIVER".equalsIgnoreCase(entityType)) {
            return driverId;
        }
        return driverId; // Default fallback
    }
}
