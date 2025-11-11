package com.moveinsync.sentiment.repository;

import com.moveinsync.sentiment.model.Alert;
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
 * Repository for Alert entity
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    /**
     * Find all alerts for a specific driver
     */
    List<Alert> findByDriverId(Long driverId);

    /**
     * Find alerts for a driver with pagination
     */
    Page<Alert> findByDriverId(Long driverId, Pageable pageable);

    /**
     * Find alerts by status
     */
    List<Alert> findByStatus(Alert.AlertStatus status);

    /**
     * Find active alerts
     */
    @Query("SELECT a FROM Alert a WHERE a.status IN ('ACTIVE', 'ACKNOWLEDGED', 'IN_PROGRESS')")
    List<Alert> findActiveAlerts();

    /**
     * Find active alerts with pagination
     */
    @Query("SELECT a FROM Alert a WHERE a.status IN ('ACTIVE', 'ACKNOWLEDGED', 'IN_PROGRESS')")
    Page<Alert> findActiveAlerts(Pageable pageable);

    /**
     * Find active alerts for driver
     */
    @Query("SELECT a FROM Alert a WHERE a.driverId = :driverId AND a.status IN ('ACTIVE', 'ACKNOWLEDGED', 'IN_PROGRESS')")
    List<Alert> findActiveAlertsByDriverId(@Param("driverId") Long driverId);

    /**
     * Find alerts by type
     */
    List<Alert> findByAlertType(Alert.AlertType alertType);

    /**
     * Find alerts by severity
     */
    List<Alert> findBySeverity(Alert.AlertSeverity severity);

    /**
     * Find critical alerts
     */
    @Query("SELECT a FROM Alert a WHERE a.severity = 'CRITICAL' AND a.status IN ('ACTIVE', 'ACKNOWLEDGED', 'IN_PROGRESS')")
    List<Alert> findCriticalAlerts();

    /**
     * Find high priority alerts
     */
    @Query("SELECT a FROM Alert a WHERE a.severity IN ('CRITICAL', 'HIGH') AND a.status IN ('ACTIVE', 'ACKNOWLEDGED', 'IN_PROGRESS')")
    List<Alert> findHighPriorityAlerts();

    /**
     * Find unacknowledged alerts
     */
    @Query("SELECT a FROM Alert a WHERE a.status = 'ACTIVE' AND a.acknowledgedAt IS NULL")
    List<Alert> findUnacknowledgedAlerts();

    /**
     * Find alerts assigned to manager
     */
    List<Alert> findByAssignedTo(Long managerId);

    /**
     * Find unassigned alerts
     */
    @Query("SELECT a FROM Alert a WHERE a.assignedTo IS NULL AND a.status IN ('ACTIVE', 'ACKNOWLEDGED')")
    List<Alert> findUnassignedAlerts();

    /**
     * Find alerts in cooldown
     */
    @Query("SELECT a FROM Alert a WHERE a.cooldownExpiresAt > :now")
    List<Alert> findAlertsInCooldown(@Param("now") LocalDateTime now);

    /**
     * Find driver alerts not in cooldown
     */
    @Query("SELECT a FROM Alert a WHERE a.driverId = :driverId AND (a.cooldownExpiresAt IS NULL OR a.cooldownExpiresAt <= :now)")
    List<Alert> findDriverAlertsNotInCooldown(@Param("driverId") Long driverId, @Param("now") LocalDateTime now);

    /**
     * Check if driver has active alert
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Alert a WHERE a.driverId = :driverId AND a.status IN ('ACTIVE', 'ACKNOWLEDGED', 'IN_PROGRESS')")
    boolean hasActiveAlert(@Param("driverId") Long driverId);

    /**
     * Find overdue alerts (active for more than 24 hours)
     */
    @Query("SELECT a FROM Alert a WHERE a.status IN ('ACTIVE', 'ACKNOWLEDGED', 'IN_PROGRESS') AND a.createdAt < :threshold")
    List<Alert> findOverdueAlerts(@Param("threshold") LocalDateTime threshold);

    /**
     * Find alerts created in date range
     */
    @Query("SELECT a FROM Alert a WHERE a.createdAt BETWEEN :startDate AND :endDate")
    List<Alert> findByCreatedAtBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find resolved alerts
     */
    @Query("SELECT a FROM Alert a WHERE a.status IN ('RESOLVED', 'DISMISSED')")
    List<Alert> findResolvedAlerts();

    /**
     * Find recent alerts for driver
     */
    @Query("SELECT a FROM Alert a WHERE a.driverId = :driverId ORDER BY a.createdAt DESC")
    List<Alert> findRecentAlertsByDriverId(@Param("driverId") Long driverId, Pageable pageable);

    /**
     * Count active alerts
     */
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.status IN ('ACTIVE', 'ACKNOWLEDGED', 'IN_PROGRESS')")
    long countActiveAlerts();

    /**
     * Count active alerts by severity
     */
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.status IN ('ACTIVE', 'ACKNOWLEDGED', 'IN_PROGRESS') AND a.severity = :severity")
    long countActiveAlertsBySeverity(@Param("severity") Alert.AlertSeverity severity);

    /**
     * Count alerts for driver
     */
    long countByDriverId(Long driverId);

    /**
     * Count alerts by driver and status
     */
    long countByDriverIdAndStatus(Long driverId, Alert.AlertStatus status);

    /**
     * Get most recent alert for driver
     */
    Optional<Alert> findFirstByDriverIdOrderByCreatedAtDesc(Long driverId);

    /**
     * Find alerts without notification sent
     */
    @Query("SELECT a FROM Alert a WHERE a.notificationSent = false AND a.status = 'ACTIVE'")
    List<Alert> findAlertsWithoutNotification();

    /**
     * Get alert statistics by type
     */
    @Query("SELECT a.alertType, COUNT(a) FROM Alert a WHERE a.createdAt >= :since GROUP BY a.alertType")
    List<Object[]> getAlertStatsByType(@Param("since") LocalDateTime since);

    /**
     * Get alert statistics by severity
     */
    @Query("SELECT a.severity, COUNT(a) FROM Alert a WHERE a.createdAt >= :since GROUP BY a.severity")
    List<Object[]> getAlertStatsBySeverity(@Param("since") LocalDateTime since);

    /**
     * Find escalated alerts
     */
    @Query("SELECT a FROM Alert a WHERE a.status = 'ESCALATED'")
    List<Alert> findEscalatedAlerts();

    /**
     * Get average resolution time
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, a.createdAt, a.resolvedAt)) FROM Alert a WHERE a.resolvedAt IS NOT NULL AND a.createdAt >= :since")
    Optional<Double> getAverageResolutionTimeInHours(@Param("since") LocalDateTime since);

    /**
     * Find alerts by recommended action
     */
    List<Alert> findByRecommendedAction(Alert.RecommendedAction action);
}
