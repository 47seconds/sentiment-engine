package com.moveinsync.sentiment.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Topics Configuration
 * 
 * Defines all Kafka topics used in the sentiment engine:
 * 1. feedback.submitted - Raw feedback events from users
 * 2. feedback.processed - Feedback after sentiment analysis
 * 3. driver.stats.updated - Driver sentiment statistics updates
 * 4. alert.triggered - Low sentiment alerts
 * 
 * Each topic is configured with:
 * - Partitions: For parallel processing
 * - Replication: For fault tolerance (1 in dev, 3 in prod)
 * - Retention: How long to keep messages
 */
@Configuration
public class KafkaTopics {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Topic Names - Centralized constants
     */
    public static final String FEEDBACK_SUBMITTED = "feedback.submitted";
    public static final String FEEDBACK_PROCESSED = "feedback.processed";
    public static final String DRIVER_STATS_UPDATED = "driver.stats.updated";
    public static final String ALERT_TRIGGERED = "alert.triggered";

    /**
     * Kafka Admin Bean
     * Used to create topics programmatically
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    /**
     * Topic: feedback.submitted
     * 
     * Purpose: Receives raw feedback from users (driver, trip, app feedback)
     * Partitions: 3 (for parallel processing)
     * Replication: 1 (dev), 3 (prod)
     * 
     * Flow: API → Kafka → FeedbackConsumer → SentimentAnalysis
     */
    @Bean
    public NewTopic feedbackSubmittedTopic() {
        return new NewTopic(FEEDBACK_SUBMITTED, 3, (short) 1)
            .configs(Map.of(
                "retention.ms", "604800000",  // 7 days
                "compression.type", "snappy",
                "cleanup.policy", "delete"
            ));
    }

    /**
     * Topic: feedback.processed
     * 
     * Purpose: Feedback after sentiment analysis (with scores)
     * Partitions: 3
     * Replication: 1 (dev), 3 (prod)
     * 
     * Flow: SentimentService → Kafka → DriverStatsService
     */
    @Bean
    public NewTopic feedbackProcessedTopic() {
        return new NewTopic(FEEDBACK_PROCESSED, 3, (short) 1)
            .configs(Map.of(
                "retention.ms", "2592000000",  // 30 days
                "compression.type", "snappy",
                "cleanup.policy", "delete"
            ));
    }

    /**
     * Topic: driver.stats.updated
     * 
     * Purpose: Driver sentiment statistics updates (EMA, feedback count)
     * Partitions: 2 (lower volume than feedback)
     * Replication: 1 (dev), 3 (prod)
     * 
     * Flow: DriverStatsService → Kafka → AlertService
     */
    @Bean
    public NewTopic driverStatsUpdatedTopic() {
        return new NewTopic(DRIVER_STATS_UPDATED, 2, (short) 1)
            .configs(Map.of(
                "retention.ms", "2592000000",  // 30 days
                "compression.type", "snappy",
                "cleanup.policy", "delete"
            ));
    }

    /**
     * Topic: alert.triggered
     * 
     * Purpose: Low sentiment alerts for drivers
     * Partitions: 1 (low volume, ordered processing)
     * Replication: 1 (dev), 3 (prod)
     * 
     * Flow: AlertService → Kafka → NotificationService (future)
     */
    @Bean
    public NewTopic alertTriggeredTopic() {
        return new NewTopic(ALERT_TRIGGERED, 1, (short) 1)
            .configs(Map.of(
                "retention.ms", "7776000000",  // 90 days (compliance)
                "compression.type", "snappy",
                "cleanup.policy", "delete"
            ));
    }
}
