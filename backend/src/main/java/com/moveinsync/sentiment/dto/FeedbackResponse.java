package com.moveinsync.sentiment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for feedback
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {

    private Long id;
    private Long driverId;
    private Long tripId;
    private Long userId;
    private String feedbackType;
    private String feedbackText;
    private Integer rating;
    private String source;
    private Double sentimentScore;
    private String sentimentLabel;
    private Double confidence;
    private List<String> keywords;
    private Boolean requiresAttention;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
