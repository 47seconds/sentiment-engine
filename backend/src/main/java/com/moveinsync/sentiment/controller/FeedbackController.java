package com.moveinsync.sentiment.controller;

import com.moveinsync.sentiment.dto.*;
import com.moveinsync.sentiment.model.Feedback;
import com.moveinsync.sentiment.service.FeedbackService;
import com.moveinsync.sentiment.security.UserDetailsServiceImpl;
import com.moveinsync.sentiment.util.EntityMapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Feedback operations
 * 
 * Endpoints:
 * - POST   /api/feedback                  - Submit new feedback
 * - POST   /api/feedback/{id}/process     - Process feedback with sentiment analysis
 * - GET    /api/feedback/{id}             - Get feedback by ID
 * - GET    /api/feedback/driver/{driverId} - Get feedback for driver (paginated)
 * - GET    /api/feedback/requiring-attention - Get feedback requiring attention
 * - POST   /api/feedback/{id}/review      - Mark feedback as reviewed
 * - GET    /api/feedback/stats/driver/{driverId} - Get feedback statistics for driver
 */
@Slf4j
@RestController
@RequestMapping("/feedback")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final EntityMapper entityMapper;

    public FeedbackController(FeedbackService feedbackService, EntityMapper entityMapper) {
        this.feedbackService = feedbackService;
        this.entityMapper = entityMapper;
    }

    /**
     * Submit new feedback
     * 
     * POST /api/feedback
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FeedbackResponse>> submitFeedback(
            @Valid @RequestBody FeedbackSubmitRequest request) {
        log.info("Submitting feedback for driver: {}", request.getDriverId());
        
        Feedback feedback = entityMapper.toFeedback(request);
        Feedback savedFeedback = feedbackService.submitFeedback(feedback);
        FeedbackResponse response = entityMapper.toFeedbackResponse(savedFeedback);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Feedback submitted successfully", response));
    }

    /**
     * Process feedback with sentiment analysis
     * 
     * POST /api/feedback/{id}/process
     */
    @PostMapping("/{id}/process")
    public ResponseEntity<ApiResponse<FeedbackResponse>> processFeedback(
            @PathVariable Long id) {
        log.info("Processing feedback: {}", id);
        
        Feedback processedFeedback = feedbackService.processFeedback(id);
        FeedbackResponse response = entityMapper.toFeedbackResponse(processedFeedback);
        
        return ResponseEntity.ok(ApiResponse.success("Feedback processed successfully", response));
    }

    /**
     * Get feedback by ID
     * 
     * GET /api/feedback/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FeedbackResponse>> getFeedback(@PathVariable Long id) {
        log.debug("Getting feedback: {}", id);
        
        Feedback feedback = feedbackService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found: " + id));
        
        FeedbackResponse response = entityMapper.toFeedbackResponse(feedback);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get feedback for driver (paginated)
     * 
     * GET /api/feedback/driver/{driverId}?page=0&size=20&sort=createdAt,desc
     */
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<ApiResponse<Page<FeedbackResponse>>> getFeedbackByDriver(
            @PathVariable Long driverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {
        log.debug("Getting feedback for driver: {}, page: {}, size: {}", driverId, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                sort.length > 1 && "desc".equals(sort[1]) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sort[0]
        ));
        
        Page<Feedback> feedbackPage = feedbackService.getFeedbackByDriver(driverId, pageable);
        Page<FeedbackResponse> responsePage = feedbackPage.map(entityMapper::toFeedbackResponse);
        
        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }

    /**
     * Get recent feedback for driver
     * 
     * GET /api/feedback/driver/{driverId}/recent?limit=10
     */
    @GetMapping("/driver/{driverId}/recent")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getRecentFeedback(
            @PathVariable Long driverId,
            @RequestParam(defaultValue = "10") int limit) {
        log.debug("Getting recent feedback for driver: {}, limit: {}", driverId, limit);
        
        List<Feedback> feedbackList = feedbackService.getRecentFeedback(driverId, limit);
        List<FeedbackResponse> responseList = feedbackList.stream()
                .map(entityMapper::toFeedbackResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * Get negative feedback for driver
     * 
     * GET /api/feedback/driver/{driverId}/negative
     */
    @GetMapping("/driver/{driverId}/negative")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getNegativeFeedback(
            @PathVariable Long driverId) {
        log.debug("Getting negative feedback for driver: {}", driverId);
        
        List<Feedback> feedbackList = feedbackService.getNegativeFeedback(driverId);
        List<FeedbackResponse> responseList = feedbackList.stream()
                .map(entityMapper::toFeedbackResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * Get positive feedback for driver
     * 
     * GET /api/feedback/driver/{driverId}/positive
     */
    @GetMapping("/driver/{driverId}/positive")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getPositiveFeedback(
            @PathVariable Long driverId) {
        log.debug("Getting positive feedback for driver: {}", driverId);
        
        List<Feedback> feedbackList = feedbackService.getPositiveFeedback(driverId);
        List<FeedbackResponse> responseList = feedbackList.stream()
                .map(entityMapper::toFeedbackResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * Get feedback requiring attention
     * 
     * GET /api/feedback/requiring-attention?page=0&size=20
     */
    @GetMapping("/requiring-attention")
    public ResponseEntity<ApiResponse<Page<FeedbackResponse>>> getFeedbackRequiringAttention(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Getting feedback requiring attention: page={}, size={}", page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Feedback> feedbackPage = feedbackService.getFeedbackRequiringAttention(pageable);
        Page<FeedbackResponse> responsePage = feedbackPage.map(entityMapper::toFeedbackResponse);
        
        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }

    /**
     * Get recent feedback (across all drivers)
     * 
     * GET /api/feedback/recent?limit=10
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getRecentFeedbackAll(
            @RequestParam(defaultValue = "10") int limit) {
        log.debug("Getting recent feedback for all drivers, limit: {}", limit);
        
        // Use feedbackRepository to get all feedback and sort/limit
        List<Feedback> feedbackList = feedbackService.getFeedbackRequiringAttention()
                .stream()
                .sorted((f1, f2) -> f2.getCreatedAt().compareTo(f1.getCreatedAt()))
                .limit(limit)
                .collect(Collectors.toList());
        
        List<FeedbackResponse> responseList = feedbackList.stream()
                .map(entityMapper::toFeedbackResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * Get feedback submitted by a specific user
     * 
     * GET /api/feedback/user/{userId}?page=0&size=20&sort=createdAt,desc
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<FeedbackResponse>>> getFeedbackByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {
        log.debug("Getting feedback submitted by user: userId={}, page={}, size={}", userId, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                sort.length > 1 && "desc".equals(sort[1]) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sort[0]
        ));
        
        Page<Feedback> feedbackPage = feedbackService.getFeedbackByUser(userId, pageable);
        Page<FeedbackResponse> responsePage = feedbackPage.map(entityMapper::toFeedbackResponse);
        
        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }

    /**
     * Get feedback submitted by current authenticated user
     * 
     * GET /api/feedback/my-feedback?page=0&size=20&sort=createdAt,desc
     */
    @GetMapping("/my-feedback")
    public ResponseEntity<ApiResponse<Page<FeedbackResponse>>> getMyFeedback(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {
        
        // Get current user from authentication
        UserDetailsServiceImpl.UserPrincipal userPrincipal = (UserDetailsServiceImpl.UserPrincipal) authentication.getPrincipal();
        Long currentUserId = userPrincipal.getId();
        
        log.debug("Getting feedback submitted by current user: userId={}, page={}, size={}", currentUserId, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                sort.length > 1 && "desc".equals(sort[1]) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sort[0]
        ));
        
        Page<Feedback> feedbackPage = feedbackService.getFeedbackByUser(currentUserId, pageable);
        Page<FeedbackResponse> responsePage = feedbackPage.map(entityMapper::toFeedbackResponse);
        
        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }

    /**
     * Get unprocessed feedback
     * 
     * GET /api/feedback/unprocessed
     */
    @GetMapping("/unprocessed")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getUnprocessedFeedback() {
        log.debug("Getting unprocessed feedback");
        
        List<Feedback> feedbackList = feedbackService.getUnprocessedFeedback();
        List<FeedbackResponse> responseList = feedbackList.stream()
                .map(entityMapper::toFeedbackResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * Mark feedback as reviewed
     * 
     * POST /api/feedback/{id}/review
     */
    @PostMapping("/{id}/review")
    public ResponseEntity<ApiResponse<Void>> markAsReviewed(
            @PathVariable Long id,
            @RequestParam Long reviewerId,
            @RequestParam(required = false) String notes) {
        log.info("Marking feedback as reviewed: feedbackId={}, reviewerId={}", id, reviewerId);
        
        feedbackService.markAsReviewed(id, reviewerId, notes);
        
        return ResponseEntity.ok(ApiResponse.success("Feedback marked as reviewed", null));
    }

    /**
     * Get feedback statistics for driver
     * 
     * GET /api/feedback/stats/driver/{driverId}
     */
    @GetMapping("/stats/driver/{driverId}")
    public ResponseEntity<ApiResponse<FeedbackService.FeedbackStatistics>> getFeedbackStatistics(
            @PathVariable Long driverId) {
        log.debug("Getting feedback statistics for driver: {}", driverId);
        
        FeedbackService.FeedbackStatistics stats = feedbackService.getFeedbackStatistics(driverId);
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Search feedback by text
     * 
     * GET /api/feedback/search?q=keyword
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> searchFeedback(
            @RequestParam String q) {
        log.debug("Searching feedback with query: {}", q);
        
        List<Feedback> feedbackList = feedbackService.searchFeedback(q);
        List<FeedbackResponse> responseList = feedbackList.stream()
                .map(entityMapper::toFeedbackResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * Get feedback in date range
     * 
     * GET /api/feedback/driver/{driverId}/date-range?startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59
     */
    @GetMapping("/driver/{driverId}/date-range")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getFeedbackInDateRange(
            @PathVariable Long driverId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        log.debug("Getting feedback for driver: {} in date range: {} to {}", driverId, startDate, endDate);
        
        List<Feedback> feedbackList = feedbackService.getFeedbackInDateRange(driverId, startDate, endDate);
        List<FeedbackResponse> responseList = feedbackList.stream()
                .map(entityMapper::toFeedbackResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }
}
