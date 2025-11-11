package com.moveinsync.sentiment.repository;

import com.moveinsync.sentiment.model.DriverStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for DriverStats entity
 */
@Repository
public interface DriverStatsRepository extends JpaRepository<DriverStats, Long> {

    /**
     * Find stats by driver ID
     */
    Optional<DriverStats> findByDriverId(Long driverId);

    /**
     * Check if stats exist for driver
     */
    boolean existsByDriverId(Long driverId);

    /**
     * Find drivers with EMA score below threshold
     */
    @Query("SELECT ds FROM DriverStats ds WHERE ds.emaScore < :threshold")
    List<DriverStats> findByEmaScoreBelow(@Param("threshold") Double threshold);

    /**
     * Find drivers with EMA score above threshold
     */
    @Query("SELECT ds FROM DriverStats ds WHERE ds.emaScore > :threshold")
    List<DriverStats> findByEmaScoreAbove(@Param("threshold") Double threshold);

    /**
     * Find drivers by alert status
     */
    List<DriverStats> findByAlertStatus(DriverStats.AlertStatus alertStatus);

    /**
     * Find drivers with critical alerts
     */
    @Query("SELECT ds FROM DriverStats ds WHERE ds.alertStatus = 'CRITICAL'")
    List<DriverStats> findDriversWithCriticalAlerts();

    /**
     * Find drivers with warnings
     */
    @Query("SELECT ds FROM DriverStats ds WHERE ds.alertStatus = 'WARNING'")
    List<DriverStats> findDriversWithWarnings();

    /**
     * Find drivers needing attention
     */
    @Query("SELECT ds FROM DriverStats ds WHERE ds.alertStatus IN ('CRITICAL', 'WARNING') OR ds.consecutiveNegativeFeedback >= 3")
    List<DriverStats> findDriversNeedingAttention();

    /**
     * Find top performing drivers by EMA score
     */
    @Query("SELECT ds FROM DriverStats ds ORDER BY ds.emaScore DESC")
    List<DriverStats> findTopPerformingDrivers(@Param("limit") int limit);

    /**
     * Find bottom performing drivers by EMA score
     */
    @Query("SELECT ds FROM DriverStats ds ORDER BY ds.emaScore ASC")
    List<DriverStats> findBottomPerformingDrivers(@Param("limit") int limit);

    /**
     * Find drivers with consecutive negative feedback
     */
    @Query("SELECT ds FROM DriverStats ds WHERE ds.consecutiveNegativeFeedback >= :count")
    List<DriverStats> findByConsecutiveNegativeFeedbackGreaterThanEqual(@Param("count") Integer count);

    /**
     * Find drivers with recent alerts
     */
    @Query("SELECT ds FROM DriverStats ds WHERE ds.alertTriggeredAt >= :since")
    List<DriverStats> findDriversWithRecentAlerts(@Param("since") LocalDateTime since);

    /**
     * Find drivers with no recent feedback
     */
    @Query("SELECT ds FROM DriverStats ds WHERE ds.lastFeedbackAt < :date OR ds.lastFeedbackAt IS NULL")
    List<DriverStats> findDriversWithNoRecentFeedback(@Param("date") LocalDateTime date);

    /**
     * Find drivers with high negative percentage
     */
    @Query("SELECT ds FROM DriverStats ds WHERE (ds.negativeFeedbackCount * 100.0 / NULLIF(ds.totalFeedbackCount, 0)) > :percentage")
    List<DriverStats> findDriversWithHighNegativePercentage(@Param("percentage") Double percentage);

    /**
     * Get average EMA score across all drivers
     */
    @Query("SELECT AVG(ds.emaScore) FROM DriverStats ds")
    Optional<Double> getAverageEmaScore();

    /**
     * Get total feedback count across all drivers
     */
    @Query("SELECT SUM(ds.totalFeedbackCount) FROM DriverStats ds")
    Optional<Long> getTotalFeedbackCount();

    /**
     * Count drivers by alert status
     */
    long countByAlertStatus(DriverStats.AlertStatus alertStatus);

    /**
     * Find drivers with improving sentiment
     */
    @Query("SELECT ds FROM DriverStats ds WHERE ds.previousEmaScore IS NOT NULL AND ds.emaScore > ds.previousEmaScore")
    List<DriverStats> findDriversWithImprovingSentiment();

    /**
     * Find drivers with declining sentiment
     */
    @Query("SELECT ds FROM DriverStats ds WHERE ds.previousEmaScore IS NOT NULL AND ds.emaScore < ds.previousEmaScore")
    List<DriverStats> findDriversWithDecliningSentiment();

    /**
     * Find drivers with high average rating
     */
    @Query("SELECT ds FROM DriverStats ds WHERE ds.averageRating >= :minRating ORDER BY ds.averageRating DESC")
    List<DriverStats> findDriversWithHighRating(@Param("minRating") Double minRating);

    /**
     * Find drivers updated after a specific date
     */
    List<DriverStats> findByLastUpdatedAtAfter(LocalDateTime date);

    /**
     * Get EMA score distribution
     */
    @Query("""
        SELECT 
            CASE 
                WHEN ds.emaScore >= 0.6 THEN 'EXCELLENT'
                WHEN ds.emaScore >= 0.2 THEN 'GOOD'
                WHEN ds.emaScore >= -0.2 THEN 'AVERAGE'
                WHEN ds.emaScore >= -0.6 THEN 'POOR'
                ELSE 'CRITICAL'
            END as category,
            COUNT(ds)
        FROM DriverStats ds
        GROUP BY 
            CASE 
                WHEN ds.emaScore >= 0.6 THEN 'EXCELLENT'
                WHEN ds.emaScore >= 0.2 THEN 'GOOD'
                WHEN ds.emaScore >= -0.2 THEN 'AVERAGE'
                WHEN ds.emaScore >= -0.6 THEN 'POOR'
                ELSE 'CRITICAL'
            END
    """)
    List<Object[]> getEmaScoreDistribution();

    /**
     * Find drivers with specific alert severity
     */
    List<DriverStats> findByLastAlertSeverity(DriverStats.AlertSeverity severity);

    /**
     * Find drivers with alert count above threshold
     */
    @Query("SELECT ds FROM DriverStats ds WHERE ds.alertCount >= :count")
    List<DriverStats> findDriversWithAlertCountAbove(@Param("count") Integer count);
}
