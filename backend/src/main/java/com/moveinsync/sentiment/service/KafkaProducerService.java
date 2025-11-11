package com.moveinsync.sentiment.service;

import com.moveinsync.sentiment.config.KafkaTopics;
import com.moveinsync.sentiment.model.event.AlertTriggeredEvent;
import com.moveinsync.sentiment.model.event.DriverStatsUpdatedEvent;
import com.moveinsync.sentiment.model.event.FeedbackEvent;
import com.moveinsync.sentiment.model.event.FeedbackProcessedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka Producer Service
 * 
 * Centralized service for publishing events to Kafka topics.
 * Handles error logging and provides async publishing with callbacks.
 * 
 * Usage:
 * - publishFeedbackSubmitted() - When user submits feedback
 * - publishFeedbackProcessed() - After sentiment analysis
 * - publishDriverStatsUpdated() - After EMA calculation
 * - publishAlertTriggered() - When alert threshold crossed
 */
@Slf4j
@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publish Feedback Submitted Event
     * 
     * Called when user submits feedback via API.
     * Uses driverId or tripId as partition key for ordered processing.
     * 
     * @param event FeedbackEvent
     */
    public void publishFeedbackSubmitted(FeedbackEvent event) {
        String key = getPartitionKey(event.getDriverId(), event.getTripId());
        
        log.info("Publishing feedback submitted event: feedbackId={}, type={}, driverId={}", 
                event.getFeedbackId(), event.getFeedbackType(), event.getDriverId());
        
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(KafkaTopics.FEEDBACK_SUBMITTED, key, event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Feedback submitted event published successfully: feedbackId={}, partition={}, offset={}", 
                        event.getFeedbackId(), 
                        result.getRecordMetadata().partition(), 
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish feedback submitted event: feedbackId={}, error={}", 
                        event.getFeedbackId(), ex.getMessage(), ex);
            }
        });
    }

    /**
     * Publish Feedback Processed Event
     * 
     * Called after sentiment analysis is complete.
     * Triggers driver stats recalculation.
     * 
     * @param event FeedbackProcessedEvent
     */
    public void publishFeedbackProcessed(FeedbackProcessedEvent event) {
        String key = getPartitionKey(event.getDriverId(), event.getTripId());
        
        log.info("Publishing feedback processed event: feedbackId={}, sentimentScore={}, label={}", 
                event.getFeedbackId(), event.getSentimentScore(), event.getSentimentLabel());
        
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(KafkaTopics.FEEDBACK_PROCESSED, key, event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Feedback processed event published successfully: feedbackId={}, partition={}, offset={}", 
                        event.getFeedbackId(), 
                        result.getRecordMetadata().partition(), 
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish feedback processed event: feedbackId={}, error={}", 
                        event.getFeedbackId(), ex.getMessage(), ex);
            }
        });
    }

    /**
     * Publish Driver Stats Updated Event
     * 
     * Called after driver EMA score is recalculated.
     * Triggers alert evaluation.
     * 
     * @param event DriverStatsUpdatedEvent
     */
    public void publishDriverStatsUpdated(DriverStatsUpdatedEvent event) {
        String key = event.getDriverId().toString();
        
        log.info("Publishing driver stats updated event: driverId={}, emaScore={}, alertTriggered={}", 
                event.getDriverId(), event.getEmaScore(), event.getAlertTriggered());
        
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(KafkaTopics.DRIVER_STATS_UPDATED, key, event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Driver stats updated event published successfully: driverId={}, partition={}, offset={}", 
                        event.getDriverId(), 
                        result.getRecordMetadata().partition(), 
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish driver stats updated event: driverId={}, error={}", 
                        event.getDriverId(), ex.getMessage(), ex);
            }
        });
    }

    /**
     * Publish Alert Triggered Event
     * 
     * Called when driver sentiment falls below threshold.
     * Triggers notification to admins/managers.
     * 
     * @param event AlertTriggeredEvent
     */
    public void publishAlertTriggered(AlertTriggeredEvent event) {
        String key = event.getDriverId().toString();
        
        log.warn("Publishing alert triggered event: alertId={}, driverId={}, severity={}, emaScore={}", 
                event.getAlertId(), event.getDriverId(), event.getSeverity(), event.getCurrentEmaScore());
        
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(KafkaTopics.ALERT_TRIGGERED, key, event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Alert triggered event published successfully: alertId={}, partition={}, offset={}", 
                        event.getAlertId(), 
                        result.getRecordMetadata().partition(), 
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish alert triggered event: alertId={}, error={}", 
                        event.getAlertId(), ex.getMessage(), ex);
                // TODO: Store failed alerts in database for retry
            }
        });
    }

    /**
     * Helper method to generate partition key
     * Ensures related events go to same partition (ordered processing)
     * 
     * @param driverId Driver UUID
     * @param tripId Trip UUID
     * @return Partition key (driverId or tripId as string)
     */
    private String getPartitionKey(java.util.UUID driverId, java.util.UUID tripId) {
        if (driverId != null) {
            return driverId.toString();
        } else if (tripId != null) {
            return tripId.toString();
        } else {
            return "default";
        }
    }

    /**
     * Synchronous publish for critical events (use sparingly)
     * Blocks until event is published or fails
     * 
     * @param topic Kafka topic
     * @param key Partition key
     * @param event Event object
     * @return true if published successfully
     */
    public boolean publishSync(String topic, String key, Object event) {
        try {
            log.info("Publishing event synchronously to topic: {}, key: {}", topic, key);
            SendResult<String, Object> result = kafkaTemplate.send(topic, key, event).get();
            log.info("Event published successfully to topic: {}, partition: {}, offset: {}", 
                    topic, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            return true;
        } catch (Exception e) {
            log.error("Failed to publish event synchronously to topic: {}, error: {}", topic, e.getMessage(), e);
            return false;
        }
    }
}
