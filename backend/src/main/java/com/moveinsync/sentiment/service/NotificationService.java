package com.moveinsync.sentiment.service;

import com.moveinsync.sentiment.model.Alert;
import com.moveinsync.sentiment.model.User;
import com.moveinsync.sentiment.repository.AlertRepository;
import com.moveinsync.sentiment.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Notification Service
 * 
 * Handles sending notifications for alerts and events:
 * - Alert notifications to managers
 * - Email notifications (future)
 * - SMS notifications (future)
 * - Push notifications (future)
 * - In-app notifications (future)
 * 
 * Currently uses Kafka events for notifications.
 * Future: Integrate with email service, SMS gateway, push notification service.
 */
@Slf4j
@Service
public class NotificationService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final KafkaProducerService kafkaProducerService;

    public NotificationService(
            AlertRepository alertRepository,
            UserRepository userRepository,
            KafkaProducerService kafkaProducerService) {
        this.alertRepository = alertRepository;
        this.userRepository = userRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    /**
     * Send alert notification
     * 
     * @param alertId Alert ID
     */
    @Transactional
    public void sendAlertNotification(Long alertId) {
        log.info("Sending alert notification: alertId={}", alertId);
        
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));
        
        // Get driver information
        User driver = userRepository.findByDriverId(alert.getDriverId())
                .orElse(null);
        
        // Get assigned manager if exists
        User manager = null;
        if (alert.getAssignedTo() != null) {
            manager = userRepository.findById(alert.getAssignedTo()).orElse(null);
        }
        
        // Build notification message
        String message = buildAlertNotificationMessage(alert, driver);
        
        // Send notification
        sendNotification(alert, manager, message);
        
        // Mark notification as sent
        alert.setNotificationSent(true);
        alertRepository.save(alert);
        
        log.info("Alert notification sent: alertId={}", alertId);
    }

    /**
     * Send notification to managers about unassigned critical alerts
     */
    public void sendUnassignedCriticalAlertsNotification() {
        log.info("Sending unassigned critical alerts notification");
        
        List<Alert> unassignedAlerts = alertRepository.findUnassignedAlerts()
                .stream()
                .filter(a -> a.getSeverity() == Alert.AlertSeverity.CRITICAL)
                .toList();
        
        if (unassignedAlerts.isEmpty()) {
            log.info("No unassigned critical alerts found");
            return;
        }
        
        // Get all managers
        List<User> managers = userRepository.findByRole(User.UserRole.MANAGER);
        
        String message = String.format("There are %d unassigned critical alerts requiring immediate attention.", 
                unassignedAlerts.size());
        
        for (User manager : managers) {
            sendNotificationToUser(manager, "Unassigned Critical Alerts", message);
        }
        
        log.info("Unassigned critical alerts notification sent to {} managers", managers.size());
    }

    /**
     * Send daily summary to managers
     */
    public void sendDailySummary() {
        log.info("Sending daily summary to managers");
        
        // Get statistics
        long activeAlerts = alertRepository.countActiveAlerts();
        long criticalAlerts = alertRepository.countActiveAlertsBySeverity(Alert.AlertSeverity.CRITICAL);
        long highPriorityAlerts = alertRepository.countActiveAlertsBySeverity(Alert.AlertSeverity.HIGH);
        long unacknowledgedAlerts = alertRepository.findUnacknowledgedAlerts().size();
        
        String message = String.format(
            "Daily Summary:\n" +
            "- Active Alerts: %d\n" +
            "- Critical: %d\n" +
            "- High Priority: %d\n" +
            "- Unacknowledged: %d",
            activeAlerts, criticalAlerts, highPriorityAlerts, unacknowledgedAlerts
        );
        
        // Send to all managers
        List<User> managers = userRepository.findByRole(User.UserRole.MANAGER);
        for (User manager : managers) {
            sendNotificationToUser(manager, "Daily Alert Summary", message);
        }
        
        log.info("Daily summary sent to {} managers", managers.size());
    }

    /**
     * Send overdue alert reminders
     */
    public void sendOverdueAlertReminders() {
        log.info("Sending overdue alert reminders");
        
        List<Alert> overdueAlerts = alertRepository.findOverdueAlerts(
                java.time.LocalDateTime.now().minusHours(24)
        );
        
        if (overdueAlerts.isEmpty()) {
            log.info("No overdue alerts found");
            return;
        }
        
        // Group alerts by assigned manager
        Map<Long, List<Alert>> alertsByManager = new HashMap<>();
        for (Alert alert : overdueAlerts) {
            if (alert.getAssignedTo() != null) {
                alertsByManager.computeIfAbsent(alert.getAssignedTo(), k -> new java.util.ArrayList<>())
                        .add(alert);
            }
        }
        
        // Send reminders to each manager
        for (Map.Entry<Long, List<Alert>> entry : alertsByManager.entrySet()) {
            User manager = userRepository.findById(entry.getKey()).orElse(null);
            if (manager != null) {
                String message = String.format(
                    "You have %d overdue alerts (active for more than 24 hours) requiring attention.",
                    entry.getValue().size()
                );
                sendNotificationToUser(manager, "Overdue Alert Reminder", message);
            }
        }
        
        log.info("Overdue alert reminders sent");
    }

    /**
     * Build alert notification message
     * 
     * @param alert Alert
     * @param driver Driver user
     * @return Notification message
     */
    private String buildAlertNotificationMessage(Alert alert, User driver) {
        String driverName = driver != null ? driver.getName() : "Unknown Driver";
        
        return String.format(
            "🚨 %s Alert for %s (ID: %d)\n" +
            "Severity: %s\n" +
            "Type: %s\n" +
            "Current EMA Score: %.2f\n" +
            "Recommended Action: %s\n" +
            "Alert ID: %d",
            alert.getSeverity(),
            driverName,
            alert.getDriverId(),
            alert.getSeverity(),
            alert.getAlertType(),
            alert.getCurrentEmaScore(),
            alert.getRecommendedAction(),
            alert.getId()
        );
    }

    /**
     * Send notification (currently logs, future: email/SMS/push)
     * 
     * @param alert Alert
     * @param manager Manager to notify
     * @param message Notification message
     */
    private void sendNotification(Alert alert, User manager, String message) {
        // Currently just log
        // Future: Integrate with email service, SMS gateway, push notifications
        
        if (manager != null) {
            log.info("Notification for manager {}: {}", manager.getEmail(), message);
            sendNotificationToUser(manager, "New Alert", message);
        } else {
            // Send to all managers if not assigned
            List<User> managers = userRepository.findByRole(User.UserRole.MANAGER);
            for (User m : managers) {
                sendNotificationToUser(m, "New Alert", message);
            }
        }
    }

    /**
     * Send notification to specific user
     * 
     * @param user User to notify
     * @param subject Notification subject
     * @param message Notification message
     */
    private void sendNotificationToUser(User user, String subject, String message) {
        log.info("Notification to {}: {} - {}", user.getEmail(), subject, message);
        
        // TODO: Implement actual notification sending
        // - Email via SMTP/SendGrid/AWS SES
        // - SMS via Twilio/AWS SNS
        // - Push notifications via FCM/APNS
        // - In-app notifications via WebSocket
        
        // For now, just log the notification
        // The Kafka events already provide a notification mechanism
    }

    /**
     * Send test notification to verify notification system
     * 
     * @param userId User ID to send test notification to
     */
    public void sendTestNotification(Long userId) {
        log.info("Sending test notification to user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        String message = String.format(
            "This is a test notification for %s at %s",
            user.getName(),
            java.time.LocalDateTime.now()
        );
        
        sendNotificationToUser(user, "Test Notification", message);
        
        log.info("Test notification sent to user: {}", userId);
    }

    /**
     * Send welcome notification to new drivers
     * 
     * @param driverId Driver ID
     */
    public void sendWelcomeNotification(Long driverId) {
        log.info("Sending welcome notification to driver: {}", driverId);
        
        User driver = userRepository.findByDriverId(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + driverId));
        
        String message = String.format(
            "Welcome to the Sentiment Monitoring System, %s!\n\n" +
            "Your feedback and performance will be tracked to help improve your driving experience. " +
            "We'll notify you of any concerns and provide support when needed.",
            driver.getName()
        );
        
        sendNotificationToUser(driver, "Welcome to Sentiment Monitoring", message);
        
        log.info("Welcome notification sent to driver: {}", driverId);
    }

    /**
     * Send feedback acknowledgment to user who submitted feedback
     * 
     * @param userId User ID who submitted feedback
     * @param feedbackId Feedback ID
     */
    public void sendFeedbackAcknowledgment(Long userId, Long feedbackId) {
        log.info("Sending feedback acknowledgment: userId={}, feedbackId={}", userId, feedbackId);
        
        User user = userRepository.findById(userId)
                .orElse(null);
        
        if (user != null) {
            String message = String.format(
                "Thank you for your feedback! Your submission (ID: %d) has been received and is being processed.",
                feedbackId
            );
            sendNotificationToUser(user, "Feedback Received", message);
        }
        
        log.info("Feedback acknowledgment sent");
    }

    /**
     * Send alert resolution notification to driver
     * 
     * @param alertId Alert ID
     */
    public void sendAlertResolutionNotification(Long alertId) {
        log.info("Sending alert resolution notification: alertId={}", alertId);
        
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));
        
        User driver = userRepository.findByDriverId(alert.getDriverId())
                .orElse(null);
        
        if (driver != null) {
            String message = String.format(
                "Good news! The alert (ID: %d) regarding your performance has been resolved.\n" +
                "Resolution notes: %s\n" +
                "Keep up the good work!",
                alertId,
                alert.getResolutionNotes() != null ? alert.getResolutionNotes() : "No notes provided"
            );
            sendNotificationToUser(driver, "Alert Resolved", message);
        }
        
        log.info("Alert resolution notification sent");
    }

    /**
     * Send performance improvement notification to driver
     * 
     * @param driverId Driver ID
     * @param newEmaScore New EMA score
     * @param previousEmaScore Previous EMA score
     */
    public void sendPerformanceImprovementNotification(Long driverId, double newEmaScore, double previousEmaScore) {
        log.info("Sending performance improvement notification: driverId={}", driverId);
        
        User driver = userRepository.findByDriverId(driverId)
                .orElse(null);
        
        if (driver != null && newEmaScore > previousEmaScore) {
            double improvement = ((newEmaScore - previousEmaScore) / Math.abs(previousEmaScore)) * 100;
            
            String message = String.format(
                "Great job! Your sentiment score has improved by %.1f%%!\n" +
                "Previous score: %.2f\n" +
                "Current score: %.2f\n" +
                "Keep up the excellent work!",
                improvement,
                previousEmaScore,
                newEmaScore
            );
            sendNotificationToUser(driver, "Performance Improvement", message);
        }
        
        log.info("Performance improvement notification sent");
    }
}
