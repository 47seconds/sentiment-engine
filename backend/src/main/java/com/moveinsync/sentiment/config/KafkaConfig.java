package com.moveinsync.sentiment.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Configuration Class
 * 
 * Configures:
 * 1. Kafka Producer Factory - for sending feedback events
 * 2. Kafka Consumer Factory - for processing feedback events
 * 3. Kafka Listener Container - for async message processing
 * 4. Error handling and retry policies
 * 
 * Producer Flow:
 * Service → KafkaTemplate → Topic → Consumer
 * 
 * Consumer Flow:
 * Topic → Listener → Service → Database
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String consumerGroupId;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    /**
     * Producer Configuration
     * 
     * Configured for reliability:
     * - acks=all: Wait for all replicas to acknowledge
     * - retries=3: Retry failed sends up to 3 times
     * - idempotence=true: Prevent duplicate messages
     */
    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // Reliability settings
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        // Performance settings
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        
        return props;
    }

    /**
     * Producer Factory Bean
     * Creates Kafka producers with JSON serialization
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    /**
     * KafkaTemplate Bean
     * Used by services to send messages to Kafka topics
     * 
     * Usage:
     * kafkaTemplate.send("topic-name", key, eventObject);
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Consumer Configuration
     * 
     * Configured for reliability:
     * - manual commit: Commit only after successful processing
     * - error handling: Wrap deserializer to handle malformed messages
     * - trusted packages: Allow all packages for deserialization
     */
    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        
        // Key deserializer
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        
        // Value deserializer with error handling wrapper
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        
        // Manual commit mode for better control
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        // Fetch settings
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
        
        return props;
    }

    /**
     * Consumer Factory Bean
     * Creates Kafka consumers with JSON deserialization
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    /**
     * Kafka Listener Container Factory
     * 
     * Configures:
     * - Concurrency: 3 threads per listener (parallel processing)
     * - Ack mode: Manual immediate (commit after each message)
     * - Error handling: Log and continue
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(consumerFactory());
        
        // Concurrency - number of threads per listener
        factory.setConcurrency(3);
        
        // Acknowledgment mode - manual commit
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        // Error handler - log and continue processing
        factory.setCommonErrorHandler(new org.springframework.kafka.listener.DefaultErrorHandler());
        
        return factory;
    }

    /**
     * Separate Consumer Factory for Feedback Events
     * Uses specific type for better type safety
     */
    @Bean
    public ConsumerFactory<String, com.moveinsync.sentiment.model.event.FeedbackEvent> feedbackConsumerFactory() {
        Map<String, Object> props = new HashMap<>(consumerConfigs());
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.moveinsync.sentiment.model.event.FeedbackEvent");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Kafka Listener for Feedback Events
     * Separate factory for type-safe feedback processing
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, com.moveinsync.sentiment.model.event.FeedbackEvent> 
        feedbackKafkaListenerContainerFactory() {
        
        ConcurrentKafkaListenerContainerFactory<String, com.moveinsync.sentiment.model.event.FeedbackEvent> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(feedbackConsumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(new org.springframework.kafka.listener.DefaultErrorHandler());
        
        return factory;
    }
}
