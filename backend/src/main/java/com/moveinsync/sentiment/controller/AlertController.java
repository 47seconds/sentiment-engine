package com.moveinsync.sentiment.controller;

import com.moveinsync.sentiment.dto.*;
import com.moveinsync.sentiment.model.Alert;
import com.moveinsync.sentiment.service.AlertService;
import com.moveinsync.sentiment.util.EntityMapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Alert operations
 * 
 * Endpoints:
 * - POST   /api/alerts                         - Create alert
 * - GET    /api/alerts/{id}                    - Get alert by ID
 * - GET    /api/alerts/driver/{driverId}       - Get alerts for driver
 * - GET    /api/alerts/active                  - Get active alerts
 * - GET    /api/alerts/pending                 - Get pending alerts
 * - GET    /api/alerts/by-severity/{severity}  - Get alerts by severity
 * - POST   /api/alerts/{id}/acknowledge        - Acknowledge alert
 * - POST   /api/alerts/{id}/resolve            - Resolve alert
 * - POST   /api/alerts/{id}/assign             - Assign alert
 * - POST   /api/alerts/{id}/escalate           - Escalate alert
 */
@Slf4j
@RestController
@RequestMapping("/alerts")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AlertController {

    private final AlertService alertService;
    private final EntityMapper entityMapper;

    public AlertController(AlertService alertService, EntityMapper entityMapper) {
        this.alertService = alertService;
        this.entityMapper = entityMapper;
    }

    /**
     * Create alert (Note: Alerts are typically created automatically by the system,
     * but this endpoint is provided for manual alert creation if needed)
     * 
     * POST /api/alerts - Not implemented, alerts are created automatically
     */

    /**
     * Get alert by ID
     * 
     * GET /api/alerts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlertResponse>> getAlert(@PathVariable Long id) {
        log.debug("Getting alert: {}", id);
        
        Alert alert = alertService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + id));
        
        AlertResponse response = entityMapper.toAlertResponse(alert);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get alerts for driver
     * 
     * GET /api/alerts/driver/{driverId}
     */
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getAlertsForDriver(@PathVariable Long driverId) {
        log.debug("Getting alerts for driver: {}", driverId);
        
        List<Alert> alerts = alertService.getAlertsByDriver(driverId);
        List<AlertResponse> responseList = alerts.stream()
                .map(entityMapper::toAlertResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * Get alerts for currently authenticated driver
     * 
     * GET /api/alerts/my
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getMyAlerts(
            @RequestHeader("Authorization") String authHeader) {
        log.debug("Getting alerts for authenticated driver");
        
        // For now, return active alerts (can be enhanced to filter by driver ID from JWT)
        List<Alert> alerts = alertService.getActiveAlerts();
        List<AlertResponse> responseList = alerts.stream()
                .map(entityMapper::toAlertResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * Get active alerts for driver
     * 
     * GET /api/alerts/driver/{driverId}/active
     */
    @GetMapping("/driver/{driverId}/active")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getActiveAlertsForDriver(@PathVariable Long driverId) {
        log.debug("Getting active alerts for driver: {}", driverId);
        
        List<Alert> alerts = alertService.getActiveAlertsByDriver(driverId);
        List<AlertResponse> responseList = alerts.stream()
                .map(entityMapper::toAlertResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * Get all active alerts
     * 
     * GET /api/alerts/active
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getActiveAlerts() {
        log.debug("Getting all active alerts");
        
        List<Alert> alerts = alertService.getActiveAlerts();
        List<AlertResponse> responseList = alerts.stream()
                .map(entityMapper::toAlertResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * Get unacknowledged alerts (pending action)
     * 
     * GET /api/alerts/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getPendingAlerts() {
        log.debug("Getting unacknowledged alerts");
        
        List<Alert> alerts = alertService.getUnacknowledgedAlerts();
        List<AlertResponse> responseList = alerts.stream()
                .map(entityMapper::toAlertResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * Get unassigned alerts
     * 
     * GET /api/alerts/unassigned
     */
    @GetMapping("/unassigned")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getUnassignedAlerts() {
        log.debug("Getting unassigned alerts");
        
        List<Alert> alerts = alertService.getUnassignedAlerts();
        List<AlertResponse> responseList = alerts.stream()
                .map(entityMapper::toAlertResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * Get overdue alerts
     * 
     * GET /api/alerts/overdue
     */
    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getOverdueAlerts() {
        log.debug("Getting overdue alerts");
        
        List<Alert> alerts = alertService.getOverdueAlerts();
        List<AlertResponse> responseList = alerts.stream()
                .map(entityMapper::toAlertResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * Get high priority alerts
     * 
     * GET /api/alerts/high-priority
     */
    @GetMapping("/high-priority")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getHighPriorityAlerts() {
        log.debug("Getting high priority alerts");
        
        List<Alert> alerts = alertService.getHighPriorityAlerts();
        List<AlertResponse> responseList = alerts.stream()
                .map(entityMapper::toAlertResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * Get critical alerts
     * 
     * GET /api/alerts/critical
     */
    @GetMapping("/critical")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getCriticalAlerts() {
        log.debug("Getting critical alerts");
        
        List<Alert> alerts = alertService.getCriticalAlerts();
        List<AlertResponse> responseList = alerts.stream()
                .map(entityMapper::toAlertResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * Get alerts by manager
     * 
     * GET /api/alerts/manager/{managerId}
     */
    @GetMapping("/manager/{managerId}")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getAlertsByManager(@PathVariable Long managerId) {
        log.debug("Getting alerts for manager: {}", managerId);
        
        List<Alert> alerts = alertService.getAlertsByManager(managerId);
        List<AlertResponse> responseList = alerts.stream()
                .map(entityMapper::toAlertResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * Get alert statistics
     * 
     * GET /api/alerts/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<AlertService.AlertStatistics>> getAlertStatistics() {
        log.debug("Getting alert statistics");
        
        AlertService.AlertStatistics stats = alertService.getAlertStatistics();
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Acknowledge alert
     * 
     * POST /api/alerts/{id}/acknowledge
     */
    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<ApiResponse<AlertResponse>> acknowledgeAlert(
            @PathVariable Long id,
            @Valid @RequestBody AlertAcknowledgeRequest request) {
        log.info("Acknowledging alert: {}, by user: {}", id, request.getAcknowledgedBy());
        
        Alert alert = alertService.acknowledgeAlert(id, request.getAcknowledgedBy(), request.getNotes());
        AlertResponse response = entityMapper.toAlertResponse(alert);
        
        return ResponseEntity.ok(ApiResponse.success("Alert acknowledged successfully", response));
    }

    /**
     * Resolve alert
     * 
     * POST /api/alerts/{id}/resolve
     */
    @PostMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<AlertResponse>> resolveAlert(
            @PathVariable Long id,
            @Valid @RequestBody AlertResolveRequest request) {
        log.info("Resolving alert: {}, by user: {}", id, request.getResolvedBy());
        
        Alert alert = alertService.resolveAlert(id, request.getResolvedBy(), request.getResolutionNotes());
        AlertResponse response = entityMapper.toAlertResponse(alert);
        
        return ResponseEntity.ok(ApiResponse.success("Alert resolved successfully", response));
    }

    /**
     * Assign alert to user
     * 
     * POST /api/alerts/{id}/assign
     */
    @PostMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<AlertResponse>> assignAlert(
            @PathVariable Long id,
            @RequestParam Long userId) {
        log.info("Assigning alert: {} to user: {}", id, userId);
        
        Alert alert = alertService.assignAlert(id, userId);
        AlertResponse response = entityMapper.toAlertResponse(alert);
        
        return ResponseEntity.ok(ApiResponse.success("Alert assigned successfully", response));
    }

    /**
     * Escalate alert
     * 
     * POST /api/alerts/{id}/escalate
     */
    @PostMapping("/{id}/escalate")
    public ResponseEntity<ApiResponse<AlertResponse>> escalateAlert(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        log.info("Escalating alert: {}", id);
        
        Alert alert = alertService.escalateAlert(id, reason);
        AlertResponse response = entityMapper.toAlertResponse(alert);
        
        return ResponseEntity.ok(ApiResponse.success("Alert escalated successfully", response));
    }
}
