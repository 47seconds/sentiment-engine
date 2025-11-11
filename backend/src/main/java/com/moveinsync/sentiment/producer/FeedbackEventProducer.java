package com.moveinsync.sentiment.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer service for publishing feedback events
 * 
 * Publishes events to Kafka topics for:
 * - Feedback creation
 * - Feedback updates
 * - Sentiment analysis requests
 * - Alert notifications
 */
@Slf4j
@Service
public class FeedbackEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.feedback}")
    private String feedbackTopic;

    @Value("${kafka.topics.alerts}")
    private String alertsTopic;

    public FeedbackEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Publish FEEDBACK_CREATED event
     * 
     * @param feedbackId ID of the created feedback
     * @param driverId ID of the driver
     */
    public void publishFeedbackCreated(Long feedbackId, Long driverId) {
        Map<String, Object> event = createFeedbackEvent("FEEDBACK_CREATED", feedbackId, driverId);
        publishEvent(feedbackTopic, feedbackId.toString(), event);
    }

    /**
     * Publish FEEDBACK_UPDATED event
     * 
     * @param feedbackId ID of the updated feedback
     * @param driverId ID of the driver
     */
    public void publishFeedbackUpdated(Long feedbackId, Long driverId) {
        Map<String, Object> event = createFeedbackEvent("FEEDBACK_UPDATED", feedbackId, driverId);
        publishEvent(feedbackTopic, feedbackId.toString(), event);
    }

    /**
     * Publish SENTIMENT_REQUESTED event
     * 
     * @param feedbackId ID of the feedback to analyze
     * @param driverId ID of the driver
     */
    public void publishSentimentRequested(Long feedbackId, Long driverId) {
        Map<String, Object> event = createFeedbackEvent("SENTIMENT_REQUESTED", feedbackId, driverId);
        publishEvent(feedbackTopic, feedbackId.toString(), event);
    }

    /**
     * Publish ALERT_CREATED event
     * 
     * @param alertId ID of the created alert
     * @param driverId ID of the driver
     * @param severity Alert severity level
     */
    public void publishAlertCreated(Long alertId, Long driverId, String severity) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "ALERT_CREATED");
        event.put("alertId", alertId);
        event.put("driverId", driverId);
        event.put("severity", severity);
        event.put("timestamp", LocalDateTime.now().toString());
        
        publishEvent(alertsTopic, driverId.toString(), event);
    }

    /**
     * Create a feedback event payload
     */
    private Map<String, Object> createFeedbackEvent(String eventType, Long feedbackId, Long driverId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("feedbackId", feedbackId);
        event.put("driverId", driverId);
        event.put("timestamp", LocalDateTime.now().toString());
        return event;
    }

    /**
     * Publish event to Kafka topic
     * 
     * @param topic Kafka topic name
     * @param key Message key (used for partitioning)
     * @param payload Event payload
     */
    private void publishEvent(String topic, String key, Map<String, Object> payload) {
        try {
            log.debug("Publishing event to topic={}, key={}: {}", topic, key, payload.get("eventType"));
            
            CompletableFuture<SendResult<String, Object>> future = 
                    kafkaTemplate.send(topic, key, payload);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Event published successfully: topic={}, partition={}, offset={}, key={}",
                            topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            key);
                } else {
                    log.error("Failed to publish event to topic={}, key={}: {}",
                            topic, key, ex.getMessage(), ex);
                }
            });
            
        } catch (Exception e) {
            log.error("Error serializing event payload: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    /**
     * Publish event synchronously (blocking)
     * Use for critical events that must be confirmed
     * 
     * @param topic Kafka topic
     * @param key Message key
     * @param payload Event payload
     * @return true if published successfully
     */
    public boolean publishEventSync(String topic, String key, Map<String, Object> payload) {
        try {
            SendResult<String, Object> result = kafkaTemplate.send(topic, key, payload).get();
            
            log.info("Event published synchronously: topic={}, partition={}, offset={}",
                    topic,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            
            return true;
            
        } catch (Exception e) {
            log.error("Failed to publish event synchronously: {}", e.getMessage(), e);
            return false;
        }
    }
}
