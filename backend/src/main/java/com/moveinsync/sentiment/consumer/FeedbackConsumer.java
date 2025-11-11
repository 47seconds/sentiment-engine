package com.moveinsync.sentiment.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moveinsync.sentiment.model.Feedback;
import com.moveinsync.sentiment.service.FeedbackService;
import com.moveinsync.sentiment.service.SentimentAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka consumer for processing feedback events
 * 
 * Listens to the 'feedback-topic' and processes incoming feedback:
 * 1. Receives feedback creation events
 * 2. Performs sentiment analysis
 * 3. Updates driver statistics
 * 4. Generates alerts if needed
 * 
 * Topic: feedback-topic
 * Group: sentiment-engine-group
 */
@Slf4j
@Component
public class FeedbackConsumer {

    private final FeedbackService feedbackService;
    private final SentimentAnalysisService sentimentAnalysisService;
    private final ObjectMapper objectMapper;

    public FeedbackConsumer(
            FeedbackService feedbackService,
            SentimentAnalysisService sentimentAnalysisService,
            ObjectMapper objectMapper) {
        this.feedbackService = feedbackService;
        this.sentimentAnalysisService = sentimentAnalysisService;
        this.objectMapper = objectMapper;
    }

    /**
     * Process feedback creation events
     * 
     * @param message Feedback event payload
     * @param partition Kafka partition
     * @param offset Message offset
     */
    // TEMPORARILY DISABLED FOR TESTING
    //@KafkaListener(
    //    topics = "${kafka.topics.feedback}",
    //    groupId = "${kafka.consumer.group-id}",
    //    containerFactory = "kafkaListenerContainerFactory"
    //)
    public void consumeFeedbackEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        log.info("Received feedback event from partition {} at offset {}", partition, offset);
        
        try {
            // Parse the event payload
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String eventType = (String) event.get("eventType");
            Long feedbackId = ((Number) event.get("feedbackId")).longValue();
            
            log.debug("Processing event: type={}, feedbackId={}", eventType, feedbackId);
            
            switch (eventType) {
                case "FEEDBACK_CREATED":
                    handleFeedbackCreated(feedbackId);
                    break;
                case "FEEDBACK_UPDATED":
                    handleFeedbackUpdated(feedbackId);
                    break;
                case "SENTIMENT_REQUESTED":
                    handleSentimentAnalysis(feedbackId);
                    break;
                default:
                    log.warn("Unknown event type: {}", eventType);
            }
            
        } catch (Exception e) {
            log.error("Error processing feedback event: {}", e.getMessage(), e);
            // In production, consider:
            // - Sending to dead letter queue (DLQ)
            // - Implementing retry logic with exponential backoff
            // - Alerting on repeated failures
        }
    }

    /**
     * Handle FEEDBACK_CREATED event
     * Automatically triggers sentiment analysis for new feedback
     */
    private void handleFeedbackCreated(Long feedbackId) {
        log.info("Processing FEEDBACK_CREATED event for feedbackId={}", feedbackId);
        
        try {
            // Process sentiment analysis asynchronously
            Feedback feedback = feedbackService.processFeedback(feedbackId);
            log.info("Sentiment analysis completed for feedbackId={}: score={}, label={}",
                    feedbackId, feedback.getSentimentScore(), feedback.getSentimentLabel());
                    
        } catch (Exception e) {
            log.error("Failed to process sentiment for feedbackId={}: {}", feedbackId, e.getMessage(), e);
            throw e; // Rethrow to trigger Kafka retry/DLQ
        }
    }

    /**
     * Handle FEEDBACK_UPDATED event
     * Re-analyzes sentiment if feedback text changed
     */
    private void handleFeedbackUpdated(Long feedbackId) {
        log.info("Processing FEEDBACK_UPDATED event for feedbackId={}", feedbackId);
        
        try {
            Feedback feedback = feedbackService.findById(feedbackId)
                    .orElseThrow(() -> new IllegalArgumentException("Feedback not found: " + feedbackId));
            
            // Only re-analyze if feedback text was changed and status is not already processed
            if (feedback.getStatus() != Feedback.FeedbackStatus.PROCESSED) {
                feedbackService.processFeedback(feedbackId);
                log.info("Re-analyzed sentiment for updated feedbackId={}", feedbackId);
            } else {
                log.debug("Skipping sentiment re-analysis for already processed feedbackId={}", feedbackId);
            }
            
        } catch (Exception e) {
            log.error("Failed to handle updated feedback for feedbackId={}: {}", feedbackId, e.getMessage(), e);
        }
    }

    /**
     * Handle SENTIMENT_REQUESTED event
     * Allows manual triggering of sentiment analysis
     */
    private void handleSentimentAnalysis(Long feedbackId) {
        log.info("Processing SENTIMENT_REQUESTED event for feedbackId={}", feedbackId);
        
        try {
            Feedback feedback = feedbackService.processFeedback(feedbackId);
            log.info("Manual sentiment analysis completed for feedbackId={}: score={}, label={}",
                    feedbackId, feedback.getSentimentScore(), feedback.getSentimentLabel());
                    
        } catch (Exception e) {
            log.error("Failed to process manual sentiment request for feedbackId={}: {}", 
                    feedbackId, e.getMessage(), e);
        }
    }
}
