package com.moveinsync.sentiment.controller;

import com.moveinsync.sentiment.dto.AdminConfigurationRequest;
import com.moveinsync.sentiment.dto.AdminConfigurationResponse;
import com.moveinsync.sentiment.dto.ApiResponse;
import com.moveinsync.sentiment.service.SystemConfigService;
import com.moveinsync.sentiment.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Admin Configuration operations
 * 
 * Endpoints:
 * - GET    /api/admin/config    - Get current system configuration
 * - PUT    /api/admin/config    - Save system configuration
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfigController {

    private final SystemConfigService systemConfigService;
    private final UserService userService;

    @Autowired
    public AdminConfigController(SystemConfigService systemConfigService, UserService userService) {
        this.systemConfigService = systemConfigService;
        this.userService = userService;
    }

    /**
     * Get current system configuration
     * 
     * GET /api/admin/config
     */
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<AdminConfigurationResponse>> getConfiguration() {
        log.debug("Getting current system configuration");
        
        try {
            AdminConfigurationResponse config = systemConfigService.getAdminConfiguration();
            return ResponseEntity.ok(ApiResponse.success(config));
            
        } catch (Exception e) {
            log.error("Error retrieving system configuration", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve system configuration"));
        }
    }

    /**
     * Save system configuration
     * 
     * PUT /api/admin/config
     */
    @PutMapping("/config")
    public ResponseEntity<ApiResponse<AdminConfigurationResponse>> saveConfiguration(
            @Valid @RequestBody AdminConfigurationRequest request) {
        log.info("Saving system configuration");
        
        try {
            // Validate configuration
            if (request.getCriticalThreshold() >= request.getWarningThreshold()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Critical threshold must be less than warning threshold"));
            }
            
            if (request.getCooldownPeriod() < 1) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Cooldown period must be at least 1 minute"));
            }

            if (request.getMaxAlertsPerDriver() < 1) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Max alerts per driver must be at least 1"));
            }

            if (request.getAlertRetentionDays() < 1) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Alert retention days must be at least 1"));
            }

            // Get current user ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = authentication.getName();
            Long currentUserId = userService.findByEmail(currentUserEmail)
                    .map(user -> user.getId())
                    .orElse(null);

            // Save configuration
            AdminConfigurationResponse savedConfig = systemConfigService.saveAdminConfiguration(request, currentUserId);
            
            return ResponseEntity.ok(ApiResponse.success("Configuration saved successfully", savedConfig));
            
        } catch (Exception e) {
            log.error("Error saving system configuration", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to save system configuration"));
        }
    }
}