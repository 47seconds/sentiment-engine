package com.moveinsync.sentiment.service;

import com.moveinsync.sentiment.model.Alert;
import com.moveinsync.sentiment.model.DriverStats;
import com.moveinsync.sentiment.model.Feedback;
import com.moveinsync.sentiment.model.event.AlertTriggeredEvent;
import com.moveinsync.sentiment.repository.AlertRepository;
import com.moveinsync.sentiment.repository.FeedbackRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Alert Service
 * 
 * Manages sentiment alerts for drivers:
 * - Generate alerts based on driver stats
 * - Manage alert lifecycle (acknowledge, resolve, dismiss)
 * - Assign alerts to managers
 * - Track alert resolution
 * - Publish alert events to Kafka
 */
@Slf4j
@Service
public class AlertService {

    // Alert cooldown period - prevent alert spam
    private static final int COOLDOWN_HOURS = 24;

    private final AlertRepository alertRepository;
    private final FeedbackRepository feedbackRepository;
    private final KafkaProducerService kafkaProducerService;

    public AlertService(
            AlertRepository alertRepository,
            FeedbackRepository feedbackRepository,
            KafkaProducerService kafkaProducerService) {
        this.alertRepository = alertRepository;
        this.feedbackRepository = feedbackRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    /**
     * Create alert for driver based on stats
     * 
     * @param driverId Driver ID
     * @param stats Driver stats that triggered the alert
     * @return Created alert
     */
    @Transactional
    @CacheEvict(value = {"alerts", "alertsByDriver"}, allEntries = true)
    public Alert createAlert(Long driverId, DriverStats stats) {
        log.info("Creating alert for driver: driverId={}, emaScore={}, alertStatus={}", 
                driverId, stats.getEmaScore(), stats.getAlertStatus());
        
        // Check if driver is in cooldown period
        if (isInCooldown(driverId)) {
            log.info("Driver in cooldown period, skipping alert creation: driverId={}", driverId);
            return null;
        }
        
        // Check if active alert already exists
        if (alertRepository.hasActiveAlert(driverId)) {
            log.info("Active alert already exists for driver: driverId={}", driverId);
            return null;
        }
        
        // Determine alert type and severity
        Alert.AlertType alertType = determineAlertType(stats);
        Alert.AlertSeverity severity = convertAlertSeverity(stats.getLastAlertSeverity());
        
        // Get recent negative feedback IDs for context
        String relatedFeedbackIds = getRecentNegativeFeedbackIds(driverId);
        
        // Calculate score drop if previous EMA exists
        Double scoreDrop = null;
        if (stats.getPreviousEmaScore() != null) {
            scoreDrop = stats.getPreviousEmaScore() - stats.getEmaScore();
        }
        
        // Determine recommended action
        Alert.RecommendedAction recommendedAction = determineRecommendedAction(stats);
        
        // Create alert
        Alert alert = new Alert();
        alert.setDriverId(driverId);
        alert.setAlertType(alertType);
        alert.setSeverity(severity);
        alert.setStatus(Alert.AlertStatus.ACTIVE);
        alert.setCurrentEmaScore(stats.getEmaScore());
        alert.setPreviousEmaScore(stats.getPreviousEmaScore());
        alert.setScoreDrop(scoreDrop);
        alert.setThresholdValue(getThresholdForSeverity(severity));
        alert.setRelatedFeedbackIds(relatedFeedbackIds);
        alert.setRecommendedAction(recommendedAction);
        alert.setNotificationSent(false);
        alert.setCooldownExpiresAt(LocalDateTime.now().plusHours(COOLDOWN_HOURS));
        
        Alert savedAlert = alertRepository.save(alert);
        
        // TODO: Publish alert triggered event to Kafka
        // Note: Event models need to be updated to use Long instead of UUID
        /*
        AlertTriggeredEvent event = AlertTriggeredEvent.builder()
                .alertId(savedAlert.getId())
                .driverId(driverId)
                .alertType(alertType.name())
                .severity(severity.name())
                .currentEmaScore(stats.getEmaScore())
                .previousEmaScore(stats.getPreviousEmaScore())
                .scoreDrop(scoreDrop)
                .consecutiveNegativeFeedback(stats.getConsecutiveNegativeFeedback())
                .recommendedAction(recommendedAction.name())
                .timestamp(savedAlert.getCreatedAt())
                .build();
        
        kafkaProducerService.publishAlertTriggered(event);
        */
        
        log.info("Alert created successfully: alertId={}, driverId={}, severity={}, type={}", 
                savedAlert.getId(), driverId, severity, alertType);
        
        return savedAlert;
    }

    /**
     * Check if driver is in cooldown period
     * 
     * @param driverId Driver ID
     * @return true if in cooldown
     */
    private boolean isInCooldown(Long driverId) {
        LocalDateTime now = LocalDateTime.now();
        List<Alert> cooldownAlerts = alertRepository.findDriverAlertsNotInCooldown(driverId, now);
        return cooldownAlerts.isEmpty() && 
               !alertRepository.findAlertsInCooldown(now).stream()
                       .filter(a -> a.getDriverId().equals(driverId))
                       .toList()
                       .isEmpty();
    }

    /**
     * Determine alert type based on stats
     * 
     * @param stats Driver stats
     * @return Alert type
     */
    private Alert.AlertType determineAlertType(DriverStats stats) {
        if (stats.getConsecutiveNegativeFeedback() >= 3) {
            return Alert.AlertType.CONSECUTIVE_NEGATIVE_FEEDBACK;
        } else if (stats.getPreviousEmaScore() != null && 
                   stats.getEmaScore() < stats.getPreviousEmaScore() - 0.3) {
            return Alert.AlertType.SUDDEN_SCORE_DROP;
        } else {
            return Alert.AlertType.LOW_SENTIMENT_SCORE;
        }
    }

    /**
     * Convert DriverStats severity to Alert severity
     * 
     * @param severity DriverStats severity
     * @return Alert severity
     */
    private Alert.AlertSeverity convertAlertSeverity(DriverStats.AlertSeverity severity) {
        if (severity == null) {
            return Alert.AlertSeverity.LOW;
        }
        
        return switch (severity) {
            case CRITICAL -> Alert.AlertSeverity.CRITICAL;
            case HIGH -> Alert.AlertSeverity.HIGH;
            case MEDIUM -> Alert.AlertSeverity.MEDIUM;
            case LOW -> Alert.AlertSeverity.LOW;
        };
    }

    /**
     * Get threshold value for severity level
     * 
     * @param severity Alert severity
     * @return Threshold value
     */
    private double getThresholdForSeverity(Alert.AlertSeverity severity) {
        return switch (severity) {
            case CRITICAL -> -0.6;
            case HIGH -> -0.3;
            case MEDIUM -> 0.0;
            case LOW -> 0.3;
        };
    }

    /**
     * Determine recommended action based on stats
     * 
     * @param stats Driver stats
     * @return Recommended action
     */
    private Alert.RecommendedAction determineRecommendedAction(DriverStats stats) {
        if (stats.getAlertStatus() == DriverStats.AlertStatus.CRITICAL) {
            return Alert.RecommendedAction.INVESTIGATE_FURTHER;
        } else if (stats.getConsecutiveNegativeFeedback() >= 5) {
            return Alert.RecommendedAction.SCHEDULE_TRAINING;
        } else if (stats.getConsecutiveNegativeFeedback() >= 3) {
            return Alert.RecommendedAction.REVIEW_DRIVER_PROFILE;
        } else {
            return Alert.RecommendedAction.MONITOR_CLOSELY;
        }
    }

    /**
     * Get recent negative feedback IDs for driver
     * 
     * @param driverId Driver ID
     * @return Comma-separated feedback IDs
     */
    private String getRecentNegativeFeedbackIds(Long driverId) {
        List<Feedback> negativeFeedback = feedbackRepository.findNegativeFeedbackByDriverId(driverId);
        
        return negativeFeedback.stream()
                .sorted((f1, f2) -> f2.getCreatedAt().compareTo(f1.getCreatedAt()))
                .limit(5)
                .map(f -> f.getId().toString())
                .collect(Collectors.joining(","));
    }

    /**
     * Acknowledge alert
     * 
     * @param alertId Alert ID
     * @param acknowledgedBy Manager user ID
     * @param notes Acknowledgment notes
     * @return Updated alert
     */
    @Transactional
    @CacheEvict(value = {"alerts", "alertsByDriver", "alertById"}, allEntries = true)
    public Alert acknowledgeAlert(Long alertId, Long acknowledgedBy, String notes) {
        log.info("Acknowledging alert: alertId={}, acknowledgedBy={}", alertId, acknowledgedBy);
        
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));
        
        if (alert.getStatus() != Alert.AlertStatus.ACTIVE) {
            throw new IllegalStateException("Cannot acknowledge alert with status: " + alert.getStatus());
        }
        
        alert.setStatus(Alert.AlertStatus.ACKNOWLEDGED);
        alert.setAcknowledgedBy(acknowledgedBy);
        alert.setAcknowledgedAt(LocalDateTime.now());
        // Store notes in resolution_notes field since acknowledgment_notes doesn't exist
        if (notes != null && !notes.isEmpty()) {
            String currentNotes = alert.getResolutionNotes() != null ? alert.getResolutionNotes() + "\n" : "";
            alert.setResolutionNotes(currentNotes + "Acknowledged: " + notes);
        }
        
        Alert savedAlert = alertRepository.save(alert);
        log.info("Alert acknowledged: alertId={}", alertId);
        
        return savedAlert;
    }

    /**
     * Assign alert to manager
     * 
     * @param alertId Alert ID
     * @param managerId Manager user ID
     * @return Updated alert
     */
    @Transactional
    @CacheEvict(value = {"alerts", "alertsByDriver", "alertById"}, allEntries = true)
    public Alert assignAlert(Long alertId, Long managerId) {
        log.info("Assigning alert: alertId={}, managerId={}", alertId, managerId);
        
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));
        
        alert.setAssignedTo(managerId);
        
        Alert savedAlert = alertRepository.save(alert);
        log.info("Alert assigned: alertId={}, managerId={}", alertId, managerId);
        
        return savedAlert;
    }

    /**
     * Mark alert as in progress
     * 
     * @param alertId Alert ID
     * @param actionNotes Action notes
     * @return Updated alert
     */
    @Transactional
    @CacheEvict(value = {"alerts", "alertsByDriver", "alertById"}, allEntries = true)
    public Alert markInProgress(Long alertId, String actionNotes) {
        log.info("Marking alert in progress: alertId={}", alertId);
        
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));
        
        if (alert.getStatus() == Alert.AlertStatus.ACTIVE || 
            alert.getStatus() == Alert.AlertStatus.ACKNOWLEDGED) {
            alert.setStatus(Alert.AlertStatus.IN_PROGRESS);
            alert.setResolutionNotes(actionNotes);
            
            Alert savedAlert = alertRepository.save(alert);
            log.info("Alert marked in progress: alertId={}", alertId);
            return savedAlert;
        } else {
            throw new IllegalStateException("Cannot mark alert in progress from status: " + alert.getStatus());
        }
    }

    /**
     * Resolve alert
     * 
     * @param alertId Alert ID
     * @param resolvedBy Manager user ID
     * @param resolutionNotes Resolution notes
     * @return Updated alert
     */
    @Transactional
    @CacheEvict(value = {"alerts", "alertsByDriver", "alertById"}, allEntries = true)
    public Alert resolveAlert(Long alertId, Long resolvedBy, String resolutionNotes) {
        log.info("Resolving alert: alertId={}, resolvedBy={}", alertId, resolvedBy);
        
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));
        
        alert.setStatus(Alert.AlertStatus.RESOLVED);
        alert.setResolvedBy(resolvedBy);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolutionNotes(resolutionNotes);
        
        Alert savedAlert = alertRepository.save(alert);
        log.info("Alert resolved: alertId={}", alertId);
        
        return savedAlert;
    }

    /**
     * Dismiss alert
     * 
     * @param alertId Alert ID
     * @param dismissedBy Manager user ID
     * @param dismissalReason Dismissal reason
     * @return Updated alert
     */
    @Transactional
    @CacheEvict(value = {"alerts", "alertsByDriver", "alertById"}, allEntries = true)
    public Alert dismissAlert(Long alertId, Long dismissedBy, String dismissalReason) {
        log.info("Dismissing alert: alertId={}, dismissedBy={}", alertId, dismissedBy);
        
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));
        
        alert.setStatus(Alert.AlertStatus.DISMISSED);
        alert.setResolvedBy(dismissedBy);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolutionNotes(dismissalReason);
        
        Alert savedAlert = alertRepository.save(alert);
        log.info("Alert dismissed: alertId={}", alertId);
        
        return savedAlert;
    }

    /**
     * Escalate alert
     * 
     * @param alertId Alert ID
     * @param escalationNotes Escalation notes
     * @return Updated alert
     */
    @Transactional
    @CacheEvict(value = {"alerts", "alertsByDriver", "alertById"}, allEntries = true)
    public Alert escalateAlert(Long alertId, String escalationNotes) {
        log.info("Escalating alert: alertId={}", alertId);
        
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));
        
        alert.setStatus(Alert.AlertStatus.ESCALATED);
        alert.setSeverity(Alert.AlertSeverity.CRITICAL);
        alert.setResolutionNotes(escalationNotes);
        
        Alert savedAlert = alertRepository.save(alert);
        log.info("Alert escalated: alertId={}", alertId);
        
        return savedAlert;
    }

    /**
     * Find alert by ID (cached)
     * 
     * @param alertId Alert ID
     * @return Alert if found
     */
    @Cacheable(value = "alertById", key = "#alertId")
    public Optional<Alert> findById(Long alertId) {
        log.debug("Finding alert by id: {}", alertId);
        return alertRepository.findById(alertId);
    }

    /**
     * Get alerts for driver (cached)
     * 
     * @param driverId Driver ID
     * @return List of alerts
     */
    @Cacheable(value = "alertsByDriver", key = "#driverId")
    public List<Alert> getAlertsByDriver(Long driverId) {
        log.debug("Getting alerts for driver: {}", driverId);
        return alertRepository.findByDriverId(driverId);
    }

    /**
     * Get alerts for driver with pagination
     * 
     * @param driverId Driver ID
     * @param pageable Pagination parameters
     * @return Page of alerts
     */
    public Page<Alert> getAlertsByDriver(Long driverId, Pageable pageable) {
        log.debug("Getting paginated alerts for driver: {}, page={}", driverId, pageable.getPageNumber());
        return alertRepository.findByDriverId(driverId, pageable);
    }

    /**
     * Get active alerts for driver
     * 
     * @param driverId Driver ID
     * @return List of active alerts
     */
    public List<Alert> getActiveAlertsByDriver(Long driverId) {
        log.debug("Getting active alerts for driver: {}", driverId);
        return alertRepository.findActiveAlertsByDriverId(driverId);
    }

    /**
     * Get all active alerts
     * 
     * @return List of active alerts
     */
    public List<Alert> getActiveAlerts() {
        log.debug("Getting all active alerts");
        return alertRepository.findActiveAlerts();
    }

    /**
     * Get active alerts with pagination
     * 
     * @param pageable Pagination parameters
     * @return Page of active alerts
     */
    public Page<Alert> getActiveAlerts(Pageable pageable) {
        log.debug("Getting paginated active alerts");
        return alertRepository.findActiveAlerts(pageable);
    }

    /**
     * Get critical alerts
     * 
     * @return List of critical alerts
     */
    public List<Alert> getCriticalAlerts() {
        log.debug("Getting critical alerts");
        return alertRepository.findCriticalAlerts();
    }

    /**
     * Get high priority alerts
     * 
     * @return List of high priority alerts
     */
    public List<Alert> getHighPriorityAlerts() {
        log.debug("Getting high priority alerts");
        return alertRepository.findHighPriorityAlerts();
    }

    /**
     * Get unacknowledged alerts
     * 
     * @return List of unacknowledged alerts
     */
    public List<Alert> getUnacknowledgedAlerts() {
        log.debug("Getting unacknowledged alerts");
        return alertRepository.findUnacknowledgedAlerts();
    }

    /**
     * Get alerts assigned to manager
     * 
     * @param managerId Manager user ID
     * @return List of assigned alerts
     */
    public List<Alert> getAlertsByManager(Long managerId) {
        log.debug("Getting alerts for manager: {}", managerId);
        return alertRepository.findByAssignedTo(managerId);
    }

    /**
     * Get unassigned alerts
     * 
     * @return List of unassigned alerts
     */
    public List<Alert> getUnassignedAlerts() {
        log.debug("Getting unassigned alerts");
        return alertRepository.findUnassignedAlerts();
    }

    /**
     * Get overdue alerts (active > 24 hours)
     * 
     * @return List of overdue alerts
     */
    public List<Alert> getOverdueAlerts() {
        log.debug("Getting overdue alerts");
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        return alertRepository.findOverdueAlerts(threshold);
    }

    /**
     * Get alert statistics
     * 
     * @return Alert statistics
     */
    public AlertStatistics getAlertStatistics() {
        log.debug("Getting alert statistics");
        
        long totalActive = alertRepository.countActiveAlerts();
        long criticalCount = alertRepository.countActiveAlertsBySeverity(Alert.AlertSeverity.CRITICAL);
        long highCount = alertRepository.countActiveAlertsBySeverity(Alert.AlertSeverity.HIGH);
        long mediumCount = alertRepository.countActiveAlertsBySeverity(Alert.AlertSeverity.MEDIUM);
        long lowCount = alertRepository.countActiveAlertsBySeverity(Alert.AlertSeverity.LOW);
        
        List<Alert> unacknowledged = alertRepository.findUnacknowledgedAlerts();
        List<Alert> unassigned = alertRepository.findUnassignedAlerts();
        
        return new AlertStatistics(
            totalActive,
            criticalCount,
            highCount,
            mediumCount,
            lowCount,
            unacknowledged.size(),
            unassigned.size()
        );
    }

    /**
     * Alert statistics POJO
     */
    public record AlertStatistics(
        long totalActive,
        long criticalCount,
        long highCount,
        long mediumCount,
        long lowCount,
        long unacknowledgedCount,
        long unassignedCount
    ) {}
}
