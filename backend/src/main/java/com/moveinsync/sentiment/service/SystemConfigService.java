package com.moveinsync.sentiment.service;

import com.moveinsync.sentiment.dto.AdminConfigurationRequest;
import com.moveinsync.sentiment.dto.AdminConfigurationResponse;
import com.moveinsync.sentiment.model.SystemConfig;
import com.moveinsync.sentiment.repository.SystemConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing system configuration
 */
@Slf4j
@Service
public class SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

    @Autowired
    public SystemConfigService(SystemConfigRepository systemConfigRepository) {
        this.systemConfigRepository = systemConfigRepository;
    }

    /**
     * Get current admin configuration
     */
    public AdminConfigurationResponse getAdminConfiguration() {
        log.debug("Retrieving admin configuration");

        Map<String, String> configMap = getAllConfigAsMap();

        return AdminConfigurationResponse.builder()
                // Threshold settings
                .criticalThreshold(getDoubleConfig(configMap, "alert.threshold.critical", -0.6))
                .warningThreshold(getDoubleConfig(configMap, "alert.threshold.warning", -0.3))
                .cooldownPeriod(getIntConfig(configMap, "alert.cooldown.minutes", 120))
                
                // Feature flags
                .driverFeedbackEnabled(getBooleanConfig(configMap, "driver.feedback.enabled", true))
                .tripFeedbackEnabled(getBooleanConfig(configMap, "trip.feedback.enabled", false))
                .appFeedbackEnabled(getBooleanConfig(configMap, "app.feedback.enabled", false))
                .marshalFeedbackEnabled(getBooleanConfig(configMap, "marshal.feedback.enabled", false))
                
                // Alert settings
                .maxAlertsPerDriver(getIntConfig(configMap, "alert.max.per.driver", 5))
                .alertRetentionDays(getIntConfig(configMap, "alert.retention.days", 30))
                .autoEscalationEnabled(getBooleanConfig(configMap, "alert.auto.escalation.enabled", true))
                
                // Notification settings
                .emailNotificationsEnabled(getBooleanConfig(configMap, "notification.email.enabled", true))
                .smsNotificationsEnabled(getBooleanConfig(configMap, "notification.sms.enabled", false))
                .build();
    }

    /**
     * Save admin configuration
     */
    @Transactional
    public AdminConfigurationResponse saveAdminConfiguration(AdminConfigurationRequest request, Long updatedBy) {
        log.info("Saving admin configuration updated by user: {}", updatedBy);

        try {
            // Save threshold settings
            saveConfig("alert.threshold.critical", request.getCriticalThreshold().toString(), 
                      SystemConfig.DataType.FLOAT, "EMA score threshold for critical alerts", updatedBy);
            saveConfig("alert.threshold.warning", request.getWarningThreshold().toString(), 
                      SystemConfig.DataType.FLOAT, "EMA score threshold for warning alerts", updatedBy);
            saveConfig("alert.cooldown.minutes", request.getCooldownPeriod().toString(), 
                      SystemConfig.DataType.INT, "Cooldown period between alerts for same driver", updatedBy);

            // Save feature flags
            saveConfig("driver.feedback.enabled", request.getDriverFeedbackEnabled().toString(), 
                      SystemConfig.DataType.BOOLEAN, "Allow feedback submission for drivers", updatedBy);
            saveConfig("trip.feedback.enabled", request.getTripFeedbackEnabled().toString(), 
                      SystemConfig.DataType.BOOLEAN, "Allow feedback submission for trips", updatedBy);
            saveConfig("app.feedback.enabled", request.getAppFeedbackEnabled().toString(), 
                      SystemConfig.DataType.BOOLEAN, "Allow feedback submission for app experience", updatedBy);
            saveConfig("marshal.feedback.enabled", request.getMarshalFeedbackEnabled().toString(), 
                      SystemConfig.DataType.BOOLEAN, "Allow feedback submission for marshals", updatedBy);

            // Save alert settings
            saveConfig("alert.max.per.driver", request.getMaxAlertsPerDriver().toString(), 
                      SystemConfig.DataType.INT, "Maximum active alerts per driver", updatedBy);
            saveConfig("alert.retention.days", request.getAlertRetentionDays().toString(), 
                      SystemConfig.DataType.INT, "How long to keep resolved alerts", updatedBy);
            saveConfig("alert.auto.escalation.enabled", request.getAutoEscalationEnabled().toString(), 
                      SystemConfig.DataType.BOOLEAN, "Automatically escalate unresolved alerts", updatedBy);

            // Save notification settings
            saveConfig("notification.email.enabled", request.getEmailNotificationsEnabled().toString(), 
                      SystemConfig.DataType.BOOLEAN, "Send email alerts to managers", updatedBy);
            saveConfig("notification.sms.enabled", request.getSmsNotificationsEnabled().toString(), 
                      SystemConfig.DataType.BOOLEAN, "Send SMS alerts for critical issues", updatedBy);

            log.info("Admin configuration saved successfully");

            // Return the updated configuration
            return getAdminConfiguration();

        } catch (Exception e) {
            log.error("Error saving admin configuration", e);
            throw new RuntimeException("Failed to save configuration", e);
        }
    }

    /**
     * Save individual configuration
     */
    @Transactional
    public void saveConfig(String key, String value, SystemConfig.DataType dataType, String description, Long updatedBy) {
        Optional<SystemConfig> existingConfig = systemConfigRepository.findByConfigKey(key);
        
        SystemConfig config;
        if (existingConfig.isPresent()) {
            config = existingConfig.get();
            config.setConfigValue(value);
            config.setUpdatedBy(updatedBy);
            config.setUpdatedAt(LocalDateTime.now());
        } else {
            config = SystemConfig.builder()
                    .configKey(key)
                    .configValue(value)
                    .dataType(dataType)
                    .description(description)
                    .updatedBy(updatedBy)
                    .updatedAt(LocalDateTime.now())
                    .build();
        }
        
        systemConfigRepository.save(config);
    }

    /**
     * Get configuration value by key
     */
    public Optional<String> getConfigValue(String key) {
        return systemConfigRepository.findByConfigKey(key)
                .map(SystemConfig::getConfigValue);
    }

    /**
     * Get all configurations as a map
     */
    public Map<String, String> getAllConfigAsMap() {
        List<SystemConfig> allConfigs = systemConfigRepository.findAll();
        Map<String, String> configMap = new HashMap<>();
        
        for (SystemConfig config : allConfigs) {
            configMap.put(config.getConfigKey(), config.getConfigValue());
        }
        
        return configMap;
    }

    // Helper methods for type conversion with defaults
    private Double getDoubleConfig(Map<String, String> configMap, String key, Double defaultValue) {
        String value = configMap.get(key);
        try {
            return value != null ? Double.parseDouble(value) : defaultValue;
        } catch (NumberFormatException e) {
            log.warn("Invalid double value for config key {}: {}", key, value);
            return defaultValue;
        }
    }

    private Integer getIntConfig(Map<String, String> configMap, String key, Integer defaultValue) {
        String value = configMap.get(key);
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            log.warn("Invalid integer value for config key {}: {}", key, value);
            return defaultValue;
        }
    }

    private Boolean getBooleanConfig(Map<String, String> configMap, String key, Boolean defaultValue) {
        String value = configMap.get(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
}