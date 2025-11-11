package com.moveinsync.sentiment.service;

import com.moveinsync.sentiment.model.DriverStats;
import com.moveinsync.sentiment.model.Feedback;
import com.moveinsync.sentiment.model.event.DriverStatsUpdatedEvent;
import com.moveinsync.sentiment.repository.DriverStatsRepository;
import com.moveinsync.sentiment.repository.FeedbackRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Driver Stats Service
 * 
 * Manages driver sentiment statistics:
 * - Calculate EMA (Exponential Moving Average) scores
 * - Track feedback counts and trends
 * - Detect alert conditions
 * - Publish stats updates to Kafka
 * 
 * EMA Formula: EMA(t) = α * sentiment(t) + (1 - α) * EMA(t-1)
 * where α (alpha) is the smoothing factor (default: 0.3)
 */
@Slf4j
@Service
public class DriverStatsService {

    // EMA smoothing factor (0.0 to 1.0)
    // Higher = more weight on recent feedback
    // Lower = smoother, less reactive to recent changes
    private static final double ALPHA = 0.3;

    // Alert thresholds
    private static final double CRITICAL_THRESHOLD = -0.6;  // EMA below this = CRITICAL
    private static final double WARNING_THRESHOLD = -0.3;   // EMA below this = WARNING
    private static final int CONSECUTIVE_NEGATIVE_THRESHOLD = 3;  // Consecutive negative feedback = WARNING

    private final DriverStatsRepository driverStatsRepository;
    private final FeedbackRepository feedbackRepository;

    public DriverStatsService(
            DriverStatsRepository driverStatsRepository,
            FeedbackRepository feedbackRepository) {
        this.driverStatsRepository = driverStatsRepository;
        this.feedbackRepository = feedbackRepository;
    }

    /**
     * Update driver stats after new feedback is processed
     * 
     * @param driverId Driver ID
     * @param feedback Processed feedback
     * @return Updated driver stats
     */
    @Transactional
    @CacheEvict(value = {"driverStats", "driverStatsById"}, allEntries = true)
    public DriverStats updateDriverStats(Long driverId, Feedback feedback) {
        log.info("Updating driver stats: driverId={}, feedbackId={}, sentimentScore={}", 
                driverId, feedback.getId(), feedback.getSentimentScore());
        
        // Get or create driver stats
        DriverStats stats = driverStatsRepository.findByDriverId(driverId)
                .orElseGet(() -> createNewDriverStats(driverId));
        
        // Store previous EMA for comparison
        stats.setPreviousEmaScore(stats.getEmaScore());
        
        // Update EMA score
        double newEmaScore = calculateEMA(stats.getEmaScore(), feedback.getSentimentScore());
        stats.setEmaScore(newEmaScore);
        
        // Update feedback counts
        stats.setTotalFeedbackCount(stats.getTotalFeedbackCount() + 1);
        
        if (feedback.getSentimentLabel() == Feedback.SentimentLabel.NEGATIVE || 
            feedback.getSentimentLabel() == Feedback.SentimentLabel.VERY_NEGATIVE) {
            stats.setNegativeFeedbackCount(stats.getNegativeFeedbackCount() + 1);
            stats.setConsecutiveNegativeFeedback(stats.getConsecutiveNegativeFeedback() + 1);
        } else if (feedback.getSentimentLabel() == Feedback.SentimentLabel.POSITIVE || 
                   feedback.getSentimentLabel() == Feedback.SentimentLabel.VERY_POSITIVE) {
            stats.setPositiveFeedbackCount(stats.getPositiveFeedbackCount() + 1);
            stats.setConsecutiveNegativeFeedback(0); // Reset consecutive negative counter
        } else {
            stats.setNeutralFeedbackCount(stats.getNeutralFeedbackCount() + 1);
            stats.setConsecutiveNegativeFeedback(0); // Reset consecutive negative counter
        }
        
        // Update average rating if rating is provided
        if (feedback.getRating() != null) {
            double currentTotal = stats.getAverageRating() * (stats.getTotalFeedbackCount() - 1);
            stats.setAverageRating((currentTotal + feedback.getRating()) / stats.getTotalFeedbackCount());
        }
        
        // Update timestamps
        stats.setLastFeedbackAt(feedback.getCreatedAt());
        stats.setLastUpdatedAt(LocalDateTime.now());
        
        // Evaluate alert status
        DriverStats.AlertStatus previousAlertStatus = stats.getAlertStatus();
        evaluateAlertStatus(stats);
        
        // Check if alert should be triggered
        boolean alertTriggered = shouldTriggerAlert(stats, previousAlertStatus);
        if (alertTriggered) {
            stats.setAlertTriggeredAt(LocalDateTime.now());
            stats.setAlertCount(stats.getAlertCount() + 1);
        }
        
        // Save stats
        DriverStats savedStats = driverStatsRepository.save(stats);
        
        // TODO: Publish driver stats updated event
        // Note: Event models need to be updated to use Long instead of UUID
        /*
        DriverStatsUpdatedEvent event = DriverStatsUpdatedEvent.builder()
                .driverId(driverId)
                .emaScore(newEmaScore)
                .previousEmaScore(stats.getPreviousEmaScore())
                .totalFeedbackCount(stats.getTotalFeedbackCount())
                .negativeFeedbackCount(stats.getNegativeFeedbackCount())
                .positiveFeedbackCount(stats.getPositiveFeedbackCount())
                .consecutiveNegativeFeedback(stats.getConsecutiveNegativeFeedback())
                .alertStatus(stats.getAlertStatus().name())
                .alertSeverity(stats.getLastAlertSeverity() != null ? stats.getLastAlertSeverity().name() : null)
                .alertTriggered(alertTriggered)
                .timestamp(LocalDateTime.now())
                .build();
        
        kafkaProducerService.publishDriverStatsUpdated(event);
        */
        
        log.info("Driver stats updated: driverId={}, emaScore={}, alertStatus={}, alertTriggered={}", 
                driverId, newEmaScore, stats.getAlertStatus(), alertTriggered);
        
        return savedStats;
    }

    /**
     * Calculate EMA score
     * 
     * @param currentEma Current EMA score
     * @param newSentiment New sentiment score
     * @return Updated EMA score
     */
    private double calculateEMA(Double currentEma, Double newSentiment) {
        if (currentEma == null) {
            // First feedback - initialize EMA with sentiment score
            return newSentiment != null ? newSentiment : 0.0;
        }
        
        if (newSentiment == null) {
            return currentEma;
        }
        
        // EMA formula: EMA(t) = α * sentiment(t) + (1 - α) * EMA(t-1)
        return ALPHA * newSentiment + (1 - ALPHA) * currentEma;
    }

    /**
     * Create new driver stats
     * 
     * @param driverId Driver ID
     * @return New driver stats
     */
    private DriverStats createNewDriverStats(Long driverId) {
        log.info("Creating new driver stats: driverId={}", driverId);
        
        DriverStats stats = new DriverStats();
        stats.setDriverId(driverId);
        stats.setEmaScore(0.0);
        stats.setTotalFeedbackCount(0);
        stats.setPositiveFeedbackCount(0);
        stats.setNegativeFeedbackCount(0);
        stats.setNeutralFeedbackCount(0);
        stats.setConsecutiveNegativeFeedback(0);
        stats.setAverageRating(0.0);
        stats.setAlertStatus(DriverStats.AlertStatus.NORMAL);
        stats.setAlertCount(0);
        
        return stats;
    }

    /**
     * Evaluate alert status based on EMA score and consecutive negative feedback
     * 
     * @param stats Driver stats to evaluate
     */
    private void evaluateAlertStatus(DriverStats stats) {
        DriverStats.AlertSeverity severity;
        DriverStats.AlertStatus status;
        
        if (stats.getEmaScore() <= CRITICAL_THRESHOLD) {
            severity = DriverStats.AlertSeverity.CRITICAL;
            status = DriverStats.AlertStatus.CRITICAL;
        } else if (stats.getEmaScore() <= WARNING_THRESHOLD || 
                   stats.getConsecutiveNegativeFeedback() >= CONSECUTIVE_NEGATIVE_THRESHOLD) {
            severity = DriverStats.AlertSeverity.HIGH;
            status = DriverStats.AlertStatus.WARNING;
        } else if (stats.getEmaScore() < 0.0) {
            severity = DriverStats.AlertSeverity.MEDIUM;
            status = DriverStats.AlertStatus.WARNING;  // Changed from WATCH to WARNING
        } else {
            severity = DriverStats.AlertSeverity.LOW;
            status = DriverStats.AlertStatus.NORMAL;
        }
        
        stats.setAlertStatus(status);
        stats.setLastAlertSeverity(severity);
    }

    /**
     * Determine if alert should be triggered
     * 
     * @param stats Current driver stats
     * @param previousStatus Previous alert status
     * @return true if alert should be triggered
     */
    private boolean shouldTriggerAlert(DriverStats stats, DriverStats.AlertStatus previousStatus) {
        // Trigger alert if:
        // 1. Status changed from NORMAL/WATCH to WARNING/CRITICAL
        // 2. Status changed from WARNING to CRITICAL
        
        if (previousStatus == null) {
            return false;
        }
        
        if (stats.getAlertStatus() == DriverStats.AlertStatus.CRITICAL && 
            previousStatus != DriverStats.AlertStatus.CRITICAL) {
            return true;
        }
        
        if (stats.getAlertStatus() == DriverStats.AlertStatus.WARNING && 
            (previousStatus == DriverStats.AlertStatus.NORMAL)) {
            return true;
        }
        
        return false;
    }

    /**
     * Recalculate driver stats from all feedback (use for corrections)
     * 
     * @param driverId Driver ID
     * @return Recalculated driver stats
     */
    @Transactional
    @CacheEvict(value = {"driverStats", "driverStatsById"}, allEntries = true)
    public DriverStats recalculateDriverStats(Long driverId) {
        log.info("Recalculating driver stats from scratch: driverId={}", driverId);
        
        // Get all processed feedback for driver, ordered by created date
        List<Feedback> feedbackList = feedbackRepository.findByDriverIdAndDateRange(
                driverId, 
                LocalDateTime.now().minusYears(1), // Last year
                LocalDateTime.now()
        );
        
        // Create or reset stats
        DriverStats stats = driverStatsRepository.findByDriverId(driverId)
                .orElseGet(() -> createNewDriverStats(driverId));
        
        // Reset counters
        stats.setEmaScore(0.0);
        stats.setPreviousEmaScore(null);
        stats.setTotalFeedbackCount(0);
        stats.setPositiveFeedbackCount(0);
        stats.setNegativeFeedbackCount(0);
        stats.setNeutralFeedbackCount(0);
        stats.setConsecutiveNegativeFeedback(0);
        stats.setAverageRating(0.0);
        stats.setAlertCount(0);
        
        // Process each feedback in chronological order
        double totalRating = 0.0;
        int ratingCount = 0;
        int consecutiveNegative = 0;
        
        for (Feedback feedback : feedbackList) {
            if (feedback.getSentimentScore() != null) {
                stats.setEmaScore(calculateEMA(stats.getEmaScore(), feedback.getSentimentScore()));
            }
            
            stats.setTotalFeedbackCount(stats.getTotalFeedbackCount() + 1);
            
            if (feedback.getSentimentLabel() == Feedback.SentimentLabel.NEGATIVE || 
                feedback.getSentimentLabel() == Feedback.SentimentLabel.VERY_NEGATIVE) {
                stats.setNegativeFeedbackCount(stats.getNegativeFeedbackCount() + 1);
                consecutiveNegative++;
            } else if (feedback.getSentimentLabel() == Feedback.SentimentLabel.POSITIVE || 
                       feedback.getSentimentLabel() == Feedback.SentimentLabel.VERY_POSITIVE) {
                stats.setPositiveFeedbackCount(stats.getPositiveFeedbackCount() + 1);
                consecutiveNegative = 0;
            } else {
                stats.setNeutralFeedbackCount(stats.getNeutralFeedbackCount() + 1);
                consecutiveNegative = 0;
            }
            
            if (feedback.getRating() != null) {
                totalRating += feedback.getRating();
                ratingCount++;
            }
        }
        
        stats.setConsecutiveNegativeFeedback(consecutiveNegative);
        
        if (ratingCount > 0) {
            stats.setAverageRating(totalRating / ratingCount);
        }
        
        if (!feedbackList.isEmpty()) {
            stats.setLastFeedbackAt(feedbackList.get(feedbackList.size() - 1).getCreatedAt());
        }
        
        stats.setLastUpdatedAt(LocalDateTime.now());
        evaluateAlertStatus(stats);
        
        DriverStats savedStats = driverStatsRepository.save(stats);
        
        log.info("Driver stats recalculated: driverId={}, emaScore={}, totalFeedback={}", 
                driverId, stats.getEmaScore(), stats.getTotalFeedbackCount());
        
        return savedStats;
    }

    /**
     * Get driver stats by driver ID (cached)
     * 
     * @param driverId Driver ID
     * @return Driver stats if found
     */
    @Cacheable(value = "driverStatsById", key = "#driverId")
    public Optional<DriverStats> getDriverStats(Long driverId) {
        log.debug("Getting driver stats: driverId={}", driverId);
        return driverStatsRepository.findByDriverId(driverId);
    }

    /**
     * Get or create driver stats
     * 
     * @param driverId Driver ID
     * @return Driver stats
     */
    @Transactional
    public DriverStats getOrCreateDriverStats(Long driverId) {
        return driverStatsRepository.findByDriverId(driverId)
                .orElseGet(() -> {
                    DriverStats stats = createNewDriverStats(driverId);
                    return driverStatsRepository.save(stats);
                });
    }

    /**
     * Get all driver stats
     * 
     * @return List of all driver stats
     */
    public List<DriverStats> getAllDriverStats() {
        log.debug("Getting all driver stats");
        return driverStatsRepository.findAll();
    }

    /**
     * Get drivers needing attention
     * 
     * @return List of drivers with WARNING or CRITICAL status
     */
    public List<DriverStats> getDriversNeedingAttention() {
        log.debug("Getting drivers needing attention");
        return driverStatsRepository.findDriversNeedingAttention();
    }

    /**
     * Get drivers with critical alerts
     * 
     * @return List of drivers with CRITICAL status
     */
    public List<DriverStats> getDriversWithCriticalAlerts() {
        log.debug("Getting drivers with critical alerts");
        return driverStatsRepository.findDriversWithCriticalAlerts();
    }

    /**
     * Get drivers with improving sentiment
     * 
     * @return List of drivers with improving EMA scores
     */
    public List<DriverStats> getDriversWithImprovingSentiment() {
        log.debug("Getting drivers with improving sentiment");
        return driverStatsRepository.findDriversWithImprovingSentiment();
    }

    /**
     * Get drivers with declining sentiment
     * 
     * @return List of drivers with declining EMA scores
     */
    public List<DriverStats> getDriversWithDecliningSentiment() {
        log.debug("Getting drivers with declining sentiment");
        return driverStatsRepository.findDriversWithDecliningSentiment();
    }

    /**
     * Get EMA score distribution
     * 
     * @return List of [category, count] pairs
     */
    public List<Object[]> getEmaScoreDistribution() {
        log.debug("Getting EMA score distribution");
        return driverStatsRepository.getEmaScoreDistribution();
    }

    /**
     * Get overall statistics
     * 
     * @return Overall statistics
     */
    public OverallStatistics getOverallStatistics() {
        log.debug("Getting overall statistics");
        
        long totalDrivers = driverStatsRepository.count();
        long criticalCount = driverStatsRepository.countByAlertStatus(DriverStats.AlertStatus.CRITICAL);
        long warningCount = driverStatsRepository.countByAlertStatus(DriverStats.AlertStatus.WARNING);
        long underReviewCount = driverStatsRepository.countByAlertStatus(DriverStats.AlertStatus.UNDER_REVIEW);
        long normalCount = driverStatsRepository.countByAlertStatus(DriverStats.AlertStatus.NORMAL);
        
        Optional<Double> avgEmaScore = driverStatsRepository.getAverageEmaScore();
        Optional<Long> totalFeedback = driverStatsRepository.getTotalFeedbackCount();
        
        return new OverallStatistics(
            totalDrivers,
            criticalCount,
            warningCount,
            underReviewCount,
            normalCount,
            avgEmaScore.orElse(0.0),
            totalFeedback.orElse(0L)
        );
    }

    /**
     * Overall statistics POJO
     */
    public record OverallStatistics(
        long totalDrivers,
        long criticalCount,
        long warningCount,
        long underReviewCount,
        long normalCount,
        double averageEmaScore,
        long totalFeedbackCount
    ) {}
}
