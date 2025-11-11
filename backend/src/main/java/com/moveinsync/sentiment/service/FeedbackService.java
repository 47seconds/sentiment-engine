package com.moveinsync.sentiment.service;

import com.moveinsync.sentiment.model.Feedback;
import com.moveinsync.sentiment.producer.FeedbackEventProducer;
import com.moveinsync.sentiment.repository.FeedbackRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Feedback Service
 * 
 * Handles feedback submission, processing, and retrieval:
 * - Submit feedback from users
 * - Process feedback with sentiment analysis
 * - Publish events to Kafka
 * - Retrieve and filter feedback
 * - Mark feedback for attention
 */
@Slf4j
@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final SentimentAnalysisService sentimentAnalysisService;
    private final DriverStatsService driverStatsService;
    
    @Autowired(required = false)
    private FeedbackEventProducer feedbackEventProducer;

    public FeedbackService(
            FeedbackRepository feedbackRepository,
            SentimentAnalysisService sentimentAnalysisService,
            DriverStatsService driverStatsService) {
        this.feedbackRepository = feedbackRepository;
        this.sentimentAnalysisService = sentimentAnalysisService;
        this.driverStatsService = driverStatsService;
    }

    /**
     * Submit new feedback
     * 
     * @param feedback Feedback to submit
     * @return Submitted feedback with initial processing
     */
    @Transactional
    @CacheEvict(value = "feedbackByDriver", allEntries = true)
    public Feedback submitFeedback(Feedback feedback) {
        log.info("Submitting new feedback: driverId={}, tripId={}, type={}", 
                feedback.getDriverId(), feedback.getTripId(), feedback.getFeedbackType());
        
        // Set initial status
        feedback.setStatus(Feedback.FeedbackStatus.SUBMITTED);
        
        // Save feedback
        Feedback savedFeedback = feedbackRepository.save(feedback);
        
        // Automatically process sentiment analysis (since Kafka consumer is disabled)
        try {
            log.info("🤖 Auto-processing sentiment analysis for feedback: {}", savedFeedback.getId());
            savedFeedback = processFeedback(savedFeedback.getId());
            log.info("✅ Sentiment analysis completed: score={}, label={}", 
                    savedFeedback.getSentimentScore(), savedFeedback.getSentimentLabel());
            
            // Update driver stats after feedback is processed
            if (savedFeedback.getDriverId() != null) {
                try {
                    log.info("📊 Updating driver stats for driver: {}", savedFeedback.getDriverId());
                    driverStatsService.recalculateDriverStats(savedFeedback.getDriverId());
                    log.info("✅ Driver stats updated successfully");
                } catch (Exception e) {
                    log.error("❌ Failed to update driver stats for driver {}: {}", 
                            savedFeedback.getDriverId(), e.getMessage(), e);
                    // Don't fail the feedback submission if stats update fails
                }
            }
        } catch (Exception e) {
            log.error("❌ Failed to auto-process sentiment for feedback {}: {}", 
                    savedFeedback.getId(), e.getMessage(), e);
            // Don't fail the submission if sentiment analysis fails
        }
        
        // Publish feedback created event to Kafka (if Kafka is enabled)
        if (feedbackEventProducer != null) {
            try {
                feedbackEventProducer.publishFeedbackCreated(
                        savedFeedback.getId(), 
                        savedFeedback.getDriverId()
                );
                log.debug("Published FEEDBACK_CREATED event for feedbackId={}", savedFeedback.getId());
            } catch (Exception e) {
                log.warn("Failed to publish feedback event (Kafka may be disabled): {}", e.getMessage());
                // Continue processing even if Kafka fails
            }
        }
        
        log.info("Feedback submitted successfully: id={}", savedFeedback.getId());
        return savedFeedback;
    }

    /**
     * Process feedback with sentiment analysis
     * 
     * @param feedbackId Feedback ID to process
     * @return Processed feedback
     */
    @Transactional
    @CacheEvict(value = {"feedbackByDriver", "feedbackById"}, allEntries = true)
    public Feedback processFeedback(Long feedbackId) {
        log.info("Processing feedback: feedbackId={}", feedbackId);
        
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found: " + feedbackId));
        
        // Update status
        feedback.setStatus(Feedback.FeedbackStatus.PROCESSING);
        feedbackRepository.save(feedback);
        
        try {
            // Analyze sentiment
            SentimentAnalysisService.SentimentResult result = 
                    sentimentAnalysisService.analyzeSentiment(feedback.getFeedbackText());
            
            // Update feedback with analysis results
            feedback.setSentimentScore(result.sentimentScore());
            feedback.setSentimentLabel(result.label());
            feedback.setConfidence(result.confidence());
            feedback.setKeywords(result.keywords());
            feedback.setProcessedAt(LocalDateTime.now());
            
            // Auto-correct feedback type based on sentiment
            feedback.setFeedbackType(determineFeedbackType(result.label(), result.sentimentScore()));
            
            // Mark for attention if negative sentiment or low confidence
            if (result.label() == Feedback.SentimentLabel.VERY_NEGATIVE || 
                result.label() == Feedback.SentimentLabel.NEGATIVE ||
                result.confidence() < 0.5) {
                feedback.setRequiresAttention(true);
            }
            
            feedback.setStatus(Feedback.FeedbackStatus.PROCESSED);
            
            Feedback processedFeedback = feedbackRepository.save(feedback);
            
            // TODO: Publish feedback processed event to Kafka
            // Note: Event models need to be updated to use Long instead of UUID
            /*
            FeedbackProcessedEvent event = FeedbackProcessedEvent.builder()
                    .feedbackId(processedFeedback.getId())
                    .driverId(processedFeedback.getDriverId())
                    .tripId(processedFeedback.getTripId())
                    .sentimentScore(processedFeedback.getSentimentScore())
                    .sentimentLabel(processedFeedback.getSentimentLabel().name())
                    .confidence(processedFeedback.getConfidence())
                    .keywords(processedFeedback.getKeywords())
                    .requiresAttention(processedFeedback.getRequiresAttention())
                    .timestamp(processedFeedback.getProcessedAt())
                    .build();
            
            kafkaProducerService.publishFeedbackProcessed(event);
            */
            
            log.info("Feedback processed successfully: id={}, sentimentScore={}, label={}", 
                    processedFeedback.getId(), result.sentimentScore(), result.label());
            
            return processedFeedback;
            
        } catch (Exception e) {
            log.error("Error processing feedback: feedbackId={}, error={}", feedbackId, e.getMessage(), e);
            feedback.setStatus(Feedback.FeedbackStatus.SUBMITTED); // Reset to submitted instead of ERROR
            feedbackRepository.save(feedback);
            throw new RuntimeException("Failed to process feedback: " + e.getMessage(), e);
        }
    }
    
    /**
     * Determine appropriate feedback type based on sentiment analysis
     * 
     * @param sentimentLabel Analyzed sentiment label
     * @param sentimentScore Sentiment score (-1.0 to 1.0)
     * @return Appropriate feedback type
     */
    private Feedback.FeedbackType determineFeedbackType(Feedback.SentimentLabel sentimentLabel, double sentimentScore) {
        // Auto-correct feedback type based on sentiment
        if (sentimentLabel == Feedback.SentimentLabel.POSITIVE || 
            sentimentLabel == Feedback.SentimentLabel.VERY_POSITIVE || 
            sentimentScore > 0.2) {
            return Feedback.FeedbackType.POSITIVE_PRAISE;
        } else if (sentimentLabel == Feedback.SentimentLabel.NEGATIVE || 
                   sentimentLabel == Feedback.SentimentLabel.VERY_NEGATIVE || 
                   sentimentScore < -0.2) {
            return Feedback.FeedbackType.COMPLAINT;
        } else {
            return Feedback.FeedbackType.GENERAL_EXPERIENCE; // For neutral feedback
        }
    }

    /**
     * Find feedback by ID (cached)
     * 
     * @param id Feedback ID
     * @return Feedback if found
     */
    @Cacheable(value = "feedbackById", key = "#id")
    public Optional<Feedback> findById(Long id) {
        log.debug("Finding feedback by id: {}", id);
        return feedbackRepository.findById(id);
    }

    /**
     * Get all feedback for a driver (cached)
     * 
     * @param driverId Driver ID
     * @return List of feedback
     */
    @Cacheable(value = "feedbackByDriver", key = "#driverId")
    public List<Feedback> getFeedbackByDriver(Long driverId) {
        log.debug("Getting feedback for driver: {}", driverId);
        return feedbackRepository.findByDriverId(driverId);
    }

    /**
     * Get feedback for driver with pagination
     * 
     * @param driverId Driver ID
     * @param pageable Pagination parameters
     * @return Page of feedback
     */
    public Page<Feedback> getFeedbackByDriver(Long driverId, Pageable pageable) {
        log.debug("Getting paginated feedback for driver: {}, page={}", driverId, pageable.getPageNumber());
        return feedbackRepository.findByDriverId(driverId, pageable);
    }

    /**
     * Get recent feedback for driver
     * 
     * @param driverId Driver ID
     * @param limit Maximum number of results
     * @return List of recent feedback
     */
    public List<Feedback> getRecentFeedback(Long driverId, int limit) {
        log.debug("Getting recent feedback for driver: {}, limit={}", driverId, limit);
        return feedbackRepository.findRecentFeedbackByDriverId(driverId, 
                Pageable.ofSize(limit));
    }

    /**
     * Get negative feedback for driver
     * 
     * @param driverId Driver ID
     * @return List of negative feedback
     */
    public List<Feedback> getNegativeFeedback(Long driverId) {
        log.debug("Getting negative feedback for driver: {}", driverId);
        return feedbackRepository.findNegativeFeedbackByDriverId(driverId);
    }

    /**
     * Get positive feedback for driver
     * 
     * @param driverId Driver ID
     * @return List of positive feedback
     */
    public List<Feedback> getPositiveFeedback(Long driverId) {
        log.debug("Getting positive feedback for driver: {}", driverId);
        return feedbackRepository.findPositiveFeedbackByDriverId(driverId);
    }

    /**
     * Get feedback in date range
     * 
     * @param driverId Driver ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of feedback in date range
     */
    public List<Feedback> getFeedbackInDateRange(Long driverId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Getting feedback for driver: {}, startDate={}, endDate={}", driverId, startDate, endDate);
        return feedbackRepository.findByDriverIdAndDateRange(driverId, startDate, endDate);
    }

    /**
     * Get feedback requiring attention
     * 
     * @return List of feedback requiring attention
     */
    public List<Feedback> getFeedbackRequiringAttention() {
        log.debug("Getting feedback requiring attention");
        return feedbackRepository.findByRequiresAttentionTrue();
    }

    /**
     * Get feedback requiring attention with pagination
     * 
     * @param pageable Pagination parameters
     * @return Page of feedback requiring attention
     */
    public Page<Feedback> getFeedbackRequiringAttention(Pageable pageable) {
        log.debug("Getting paginated feedback requiring attention");
        return feedbackRepository.findByRequiresAttentionTrue(pageable);
    }

    /**
     * Get unprocessed feedback
     * 
     * @return List of unprocessed feedback
     */
    public List<Feedback> getUnprocessedFeedback() {
        log.debug("Getting unprocessed feedback");
        return feedbackRepository.findUnprocessedFeedback();
    }

    /**
     * Mark feedback as reviewed
     * 
     * @param feedbackId Feedback ID
     * @param reviewerId Reviewer user ID
     * @param reviewNotes Review notes
     */
    @Transactional
    @CacheEvict(value = {"feedbackByDriver", "feedbackById"}, allEntries = true)
    public void markAsReviewed(Long feedbackId, Long reviewerId, String reviewNotes) {
        log.info("Marking feedback as reviewed: feedbackId={}, reviewerId={}", feedbackId, reviewerId);
        
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found: " + feedbackId));
        
        feedback.setReviewedBy(reviewerId);
        feedback.setReviewedAt(LocalDateTime.now());
        feedback.setReviewNotes(reviewNotes);
        feedback.setRequiresAttention(false);
        feedback.setStatus(Feedback.FeedbackStatus.REVIEWED);
        
        feedbackRepository.save(feedback);
        log.info("Feedback marked as reviewed: feedbackId={}", feedbackId);
    }

    /**
     * Get feedback statistics for driver
     * 
     * @param driverId Driver ID
     * @return Feedback statistics
     */
    public FeedbackStatistics getFeedbackStatistics(Long driverId) {
        log.debug("Getting feedback statistics for driver: {}", driverId);
        
        long totalCount = feedbackRepository.countByDriverId(driverId);
        long processedCount = feedbackRepository.countByDriverIdAndStatus(driverId, Feedback.FeedbackStatus.PROCESSED);
        long negativeCount = feedbackRepository.countByDriverIdAndSentimentLabel(driverId, Feedback.SentimentLabel.NEGATIVE);
        long veryNegativeCount = feedbackRepository.countByDriverIdAndSentimentLabel(driverId, Feedback.SentimentLabel.VERY_NEGATIVE);
        long positiveCount = feedbackRepository.countByDriverIdAndSentimentLabel(driverId, Feedback.SentimentLabel.POSITIVE);
        long veryPositiveCount = feedbackRepository.countByDriverIdAndSentimentLabel(driverId, Feedback.SentimentLabel.VERY_POSITIVE);
        
        Optional<Double> avgSentiment = feedbackRepository.getAverageSentimentScoreByDriverId(driverId);
        Optional<Double> avgRating = feedbackRepository.getAverageRatingByDriverId(driverId);
        
        return new FeedbackStatistics(
            totalCount,
            processedCount,
            negativeCount + veryNegativeCount,
            positiveCount + veryPositiveCount,
            avgSentiment.orElse(0.0),
            avgRating.orElse(0.0)
        );
    }

    /**
     * Get sentiment distribution for driver
     * 
     * @param driverId Driver ID
     * @return Map of sentiment labels to counts
     */
    public List<Object[]> getSentimentDistribution(Long driverId) {
        log.debug("Getting sentiment distribution for driver: {}", driverId);
        return feedbackRepository.getSentimentDistributionByDriverId(driverId);
    }

    /**
     * Search feedback by text
     * 
     * @param searchText Text to search for
     * @return List of matching feedback
     */
    public List<Feedback> searchFeedback(String searchText) {
        log.debug("Searching feedback by text: {}", searchText);
        return feedbackRepository.searchByFeedbackText(searchText);
    }

    /**
     * Delete feedback
     * 
     * @param feedbackId Feedback ID
     */
    @Transactional
    @CacheEvict(value = {"feedbackByDriver", "feedbackById"}, allEntries = true)
    public void deleteFeedback(Long feedbackId) {
        log.info("Deleting feedback: feedbackId={}", feedbackId);
        
        if (!feedbackRepository.existsById(feedbackId)) {
            throw new IllegalArgumentException("Feedback not found: " + feedbackId);
        }
        
        feedbackRepository.deleteById(feedbackId);
        log.info("Feedback deleted successfully: feedbackId={}", feedbackId);
    }

    /**
     * Get all feedback submitted by a specific user
     * 
     * @param userId The ID of the user who submitted the feedback
     * @return List of feedback submitted by the user
     */
    @Cacheable(value = "feedbackByUser", key = "#userId")
    @Transactional(readOnly = true)
    public List<Feedback> getFeedbackByUser(Long userId) {
        log.debug("Getting feedback submitted by user: userId={}", userId);
        return feedbackRepository.findByUserId(userId);
    }

    /**
     * Get feedback submitted by a specific user with pagination
     * 
     * @param userId The ID of the user who submitted the feedback
     * @param pageable Pagination information
     * @return Page of feedback submitted by the user
     */
    @Transactional(readOnly = true)
    public Page<Feedback> getFeedbackByUser(Long userId, Pageable pageable) {
        log.debug("Getting paginated feedback submitted by user: userId={}, page={}, size={}", 
                  userId, pageable.getPageNumber(), pageable.getPageSize());
        return feedbackRepository.findByUserId(userId, pageable);
    }

    /**
     * Feedback statistics POJO
     */
    public record FeedbackStatistics(
        long totalCount,
        long processedCount,
        long negativeCount,
        long positiveCount,
        double averageSentiment,
        double averageRating
    ) {}
}
