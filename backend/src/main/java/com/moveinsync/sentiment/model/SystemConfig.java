package com.moveinsync.sentiment.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * System Configuration Entity
 * Maps to the system_config table for storing application settings
 */
@Entity
@Table(name = "system_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SystemConfig {

    @Id
    @Column(name = "config_key", length = 100)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT", nullable = false)
    private String configValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", length = 20, nullable = false)
    private DataType dataType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Data type enum for configuration values
     */
    public enum DataType {
        STRING, INT, FLOAT, BOOLEAN
    }

    /**
     * Helper method to get value as String
     */
    public String getStringValue() {
        return configValue;
    }

    /**
     * Helper method to get value as Integer
     */
    public Integer getIntValue() {
        if (dataType != DataType.INT) {
            throw new IllegalStateException("Configuration value is not an integer: " + configKey);
        }
        return Integer.parseInt(configValue);
    }

    /**
     * Helper method to get value as Double
     */
    public Double getDoubleValue() {
        if (dataType != DataType.FLOAT) {
            throw new IllegalStateException("Configuration value is not a float: " + configKey);
        }
        return Double.parseDouble(configValue);
    }

    /**
     * Helper method to get value as Boolean
     */
    public Boolean getBooleanValue() {
        if (dataType != DataType.BOOLEAN) {
            throw new IllegalStateException("Configuration value is not a boolean: " + configKey);
        }
        return Boolean.parseBoolean(configValue);
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}