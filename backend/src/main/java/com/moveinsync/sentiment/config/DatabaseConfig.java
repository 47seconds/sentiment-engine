package com.moveinsync.sentiment.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Database Configuration Class
 * 
 * Responsibilities:
 * 1. Enable JPA repositories
 * 2. Enable transaction management
 * 3. Setup entity scanning
 * 
 * Note: DataSource and HikariCP are auto-configured by Spring Boot
 * from application.properties settings:
 * - spring.datasource.url
 * - spring.datasource.username
 * - spring.datasource.password
 * - spring.datasource.hikari.*
 * 
 * Connection Pool Benefits:
 * - Reuses database connections instead of creating new ones
 * - Reduces latency by 50-70%
 * - Prevents connection exhaustion
 * - Automatic connection validation
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.moveinsync.sentiment.repository")
public class DatabaseConfig {
    // Spring Boot auto-configures DataSource and TransactionManager
    // from application.properties - no manual bean needed
}
