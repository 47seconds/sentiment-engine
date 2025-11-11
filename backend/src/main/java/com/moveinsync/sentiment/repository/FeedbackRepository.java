package com.moveinsync.sentiment.repository;

import com.moveinsync.sentiment.model.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Feedback entity
 */
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    /**
     * Find all feedback for a specific driver
     */
    List<Feedback> findByDriverId(Long driverId);

    /**
     * Find feedback for a driver with pagination
     */
    Page<Feedback> findByDriverId(Long driverId, Pageable pageable);

    /**
     * Find feedback by trip ID
     */
    List<Feedback> findByTripId(Long tripId);

    /**
     * Find feedback by user ID
     */
    List<Feedback> findByUserId(Long userId);

    /**
     * Find feedback by user ID with pagination
     */
    Page<Feedback> findByUserId(Long userId, Pageable pageable);

    /**
     * Find feedback by type
     */
    List<Feedback> findByFeedbackType(Feedback.FeedbackType feedbackType);

    /**
     * Find feedback by sentiment label
     */
    List<Feedback> findBySentimentLabel(Feedback.SentimentLabel sentimentLabel);

    /**
     * Find feedback by status
     */
    List<Feedback> findByStatus(Feedback.FeedbackStatus status);

    /**
     * Find feedback requiring attention
     */
    List<Feedback> findByRequiresAttentionTrue();

    /**
     * Find feedback requiring attention with pagination
     */
    Page<Feedback> findByRequiresAttentionTrue(Pageable pageable);

    /**
     * Find negative feedback for a driver
     */
    @Query("SELECT f FROM Feedback f WHERE f.driverId = :driverId AND f.sentimentLabel IN ('NEGATIVE', 'VERY_NEGATIVE')")
    List<Feedback> findNegativeFeedbackByDriverId(@Param("driverId") Long driverId);

    /**
     * Find positive feedback for a driver
     */
    @Query("SELECT f FROM Feedback f WHERE f.driverId = :driverId AND f.sentimentLabel IN ('POSITIVE', 'VERY_POSITIVE')")
    List<Feedback> findPositiveFeedbackByDriverId(@Param("driverId") Long driverId);

    /**
     * Find feedback for driver in date range
     */
    @Query("SELECT f FROM Feedback f WHERE f.driverId = :driverId AND f.createdAt BETWEEN :startDate AND :endDate")
    List<Feedback> findByDriverIdAndDateRange(
        @Param("driverId") Long driverId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find recent feedback for driver
     */
    @Query("SELECT f FROM Feedback f WHERE f.driverId = :driverId ORDER BY f.createdAt DESC")
    List<Feedback> findRecentFeedbackByDriverId(@Param("driverId") Long driverId, Pageable pageable);

    /**
     * Count feedback by driver
     */
    long countByDriverId(Long driverId);

    /**
     * Count feedback by driver and sentiment label
     */
    long countByDriverIdAndSentimentLabel(Long driverId, Feedback.SentimentLabel sentimentLabel);

    /**
     * Count feedback by driver and status
     */
    long countByDriverIdAndStatus(Long driverId, Feedback.FeedbackStatus status);

    /**
     * Find unprocessed feedback
     */
    @Query("SELECT f FROM Feedback f WHERE f.status = 'SUBMITTED' OR f.status = 'PROCESSING'")
    List<Feedback> findUnprocessedFeedback();

    /**
     * Find feedback with low confidence scores
     */
    @Query("SELECT f FROM Feedback f WHERE f.confidence IS NOT NULL AND f.confidence < :threshold")
    List<Feedback> findLowConfidenceFeedback(@Param("threshold") Double threshold);

    /**
     * Get average sentiment score for driver
     */
    @Query("SELECT AVG(f.sentimentScore) FROM Feedback f WHERE f.driverId = :driverId AND f.sentimentScore IS NOT NULL")
    Optional<Double> getAverageSentimentScoreByDriverId(@Param("driverId") Long driverId);

    /**
     * Get average rating for driver
     */
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.driverId = :driverId AND f.rating IS NOT NULL")
    Optional<Double> getAverageRatingByDriverId(@Param("driverId") Long driverId);

    /**
     * Find feedback created after a specific date
     */
    List<Feedback> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Search feedback by text (case-insensitive)
     */
    @Query("SELECT f FROM Feedback f WHERE LOWER(f.feedbackText) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<Feedback> searchByFeedbackText(@Param("searchText") String searchText);

    /**
     * Find feedback by driver and type
     */
    List<Feedback> findByDriverIdAndFeedbackType(Long driverId, Feedback.FeedbackType feedbackType);

    /**
     * Count total feedback in date range
     */
    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.createdAt BETWEEN :startDate AND :endDate")
    long countFeedbackInDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Get sentiment distribution for driver
     */
    @Query("SELECT f.sentimentLabel, COUNT(f) FROM Feedback f WHERE f.driverId = :driverId GROUP BY f.sentimentLabel")
    List<Object[]> getSentimentDistributionByDriverId(@Param("driverId") Long driverId);

    /**
     * Find feedback needing review (processed but not reviewed)
     */
    @Query("SELECT f FROM Feedback f WHERE f.status = 'PROCESSED' AND f.requiresAttention = true AND f.reviewedAt IS NULL")
    List<Feedback> findFeedbackNeedingReview();

    /**
     * Get most recent feedback for driver
     */
    Optional<Feedback> findFirstByDriverIdOrderByCreatedAtDesc(Long driverId);
}
