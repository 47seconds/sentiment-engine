package com.moveinsync.sentiment.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis configuration for caching and distributed storage
 */
@Configuration
@EnableCaching
@Slf4j
public class RedisConfig implements CachingConfigurer {

    /**
     * Cache names used in the application
     */
    public static final String CACHE_USER = "users";
    public static final String CACHE_DRIVER_STATS = "driverStats";
    public static final String CACHE_FEEDBACK = "feedback";
    public static final String CACHE_ALERT = "alerts";
    public static final String CACHE_DRIVER_FEEDBACK_LIST = "driverFeedbackList";
    public static final String CACHE_DRIVER_ALERTS_LIST = "driverAlertsList";
    public static final String CACHE_SENTIMENT_ANALYSIS = "sentimentAnalysis";
    public static final String CACHE_STATISTICS = "statistics";

    /**
     * Configure Redis cache manager with different TTLs for different caches
     */
    @Bean
    @Override
    public CacheManager cacheManager() {
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer()))
            .disableCachingNullValues();

        // Specific cache configurations with custom TTLs
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // User cache - 1 hour TTL
        cacheConfigurations.put(CACHE_USER, defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Driver stats cache - 15 minutes TTL (frequently updated)
        cacheConfigurations.put(CACHE_DRIVER_STATS, defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // Feedback cache - 30 minutes TTL
        cacheConfigurations.put(CACHE_FEEDBACK, defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Alert cache - 10 minutes TTL (time-sensitive)
        cacheConfigurations.put(CACHE_ALERT, defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        // Feedback list cache - 5 minutes TTL (changes frequently)
        cacheConfigurations.put(CACHE_DRIVER_FEEDBACK_LIST, defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // Alerts list cache - 5 minutes TTL (changes frequently)
        cacheConfigurations.put(CACHE_DRIVER_ALERTS_LIST, defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // Sentiment analysis cache - 2 hours TTL (rarely changes once analyzed)
        cacheConfigurations.put(CACHE_SENTIMENT_ANALYSIS, defaultConfig.entryTtl(Duration.ofHours(2)));
        
        // Statistics cache - 1 hour TTL
        cacheConfigurations.put(CACHE_STATISTICS, defaultConfig.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.builder(redisConnectionFactory())
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware()
            .build();
    }

    /**
     * Redis template for general purpose Redis operations
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        
        // Use String serialization for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Use JSON serialization for values
        template.setValueSerializer(jsonSerializer());
        template.setHashValueSerializer(jsonSerializer());
        
        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();
        
        return template;
    }

    /**
     * JSON serializer for Redis with proper type handling
     */
    private GenericJackson2JsonRedisSerializer jsonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Register Java 8 time module for LocalDateTime, etc.
        objectMapper.registerModule(new JavaTimeModule());
        
        // Enable polymorphic type handling for proper deserialization
        objectMapper.activateDefaultTyping(
            BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build(),
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    /**
     * Custom key generator for cache keys
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getSimpleName()).append(":");
            sb.append(method.getName()).append(":");
            
            for (Object param : params) {
                if (param != null) {
                    sb.append(param.toString()).append(":");
                }
            }
            
            return sb.toString();
        };
    }

    /**
     * Custom cache error handler to prevent cache failures from breaking the application
     */
    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                log.error("Cache GET error in cache '{}' for key '{}': {}", 
                    cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, org.springframework.cache.Cache cache, Object key, Object value) {
                log.error("Cache PUT error in cache '{}' for key '{}': {}", 
                    cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                log.error("Cache EVICT error in cache '{}' for key '{}': {}", 
                    cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, org.springframework.cache.Cache cache) {
                log.error("Cache CLEAR error in cache '{}': {}", 
                    cache.getName(), exception.getMessage());
            }
        };
    }

    /**
     * Redis connection factory (autowired from application properties)
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory();
    }
}
