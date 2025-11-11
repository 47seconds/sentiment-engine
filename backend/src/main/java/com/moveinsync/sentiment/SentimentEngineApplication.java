package com.moveinsync.sentiment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import java.util.Arrays;

/**
 * Main Spring Boot Application class for Driver Sentiment Engine
 * 
 * Enables:
 * - Spring Boot auto-configuration
 * - Kafka message processing
 * - Redis caching
 * - Async/threaded operations
 * - CORS for frontend communication
 */
@SpringBootApplication
@EnableKafka
@EnableCaching
@EnableAsync
public class SentimentEngineApplication {

    /**
     * Application entry point
     */
    public static void main(String[] args) {
        SpringApplication.run(SentimentEngineApplication.class, args);
    }

    /**
     * Configure CORS (Cross-Origin Resource Sharing)
     * Allows React frontend on localhost:3000 to call API on localhost:8080
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",      // React frontend (dev)
            "http://localhost:3001",   
            "http://localhost:3002",    // React frontend (dev - alternate port)
            "http://localhost:8080",       // Backend (dev)
            "http://127.0.0.1:3000",
            "http://127.0.0.1:3001",
            "http://127.0.0.1:8080"
        ));
        corsConfiguration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        corsConfiguration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "Accept", "Origin"
        ));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        
        return new CorsFilter(source);
    }
}
