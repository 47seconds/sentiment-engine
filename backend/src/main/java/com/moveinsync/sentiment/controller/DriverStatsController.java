package com.moveinsync.sentiment.controller;

import com.moveinsync.sentiment.dto.ApiResponse;
import com.moveinsync.sentiment.dto.DriverStatsResponse;
import com.moveinsync.sentiment.model.DriverStats;
import com.moveinsync.sentiment.service.DriverStatsService;
import com.moveinsync.sentiment.util.EntityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Driver Statistics operations
 * 
 * Endpoints:
 * - GET    /api/stats/driver/{driverId}        - Get driver statistics
 * - POST   /api/stats/driver/{driverId}/recalculate - Recalculate driver statistics
 * - GET    /api/stats/all                      - Get all driver statistics
 * - GET    /api/stats/needing-attention        - Get drivers needing attention
 * - GET    /api/stats/critical                 - Get drivers with critical alerts
 * - GET    /api/stats/improving                - Get drivers with improving sentiment
 * - GET    /api/stats/declining                - Get drivers with declining sentiment
 * - GET    /api/stats/overview                 - Get system overview
 */
@Slf4j
@RestController
@RequestMapping("/stats")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DriverStatsController {

    private final DriverStatsService driverStatsService;
    private final EntityMapper entityMapper;

    public DriverStatsController(DriverStatsService driverStatsService, EntityMapper entityMapper) {
        this.driverStatsService = driverStatsService;
        this.entityMapper = entityMapper;
    }

    /**
     * Get driver statistics
     * 
     * GET /api/stats/driver/{driverId}
     */
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<ApiResponse<DriverStatsResponse>> getDriverStats(@PathVariable Long driverId) {
        log.debug("Getting statistics for driver: {}", driverId);
        
        DriverStats stats = driverStatsService.getOrCreateDriverStats(driverId);
        DriverStatsResponse response = entityMapper.toDriverStatsResponse(stats);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Recalculate driver statistics
     * 
     * POST /api/stats/driver/{driverId}/recalculate
     */
    @PostMapping("/driver/{driverId}/recalculate")
    public ResponseEntity<ApiResponse<DriverStatsResponse>> recalculateStats(@PathVariable Long driverId) {
        log.info("Recalculating statistics for driver: {}", driverId);
        
        DriverStats stats = driverStatsService.recalculateDriverStats(driverId);
        DriverStatsResponse response = entityMapper.toDriverStatsResponse(stats);
        
        return ResponseEntity.ok(ApiResponse.success("Statistics recalculated successfully", response));
    }

    /**
     * Get all driver statistics
     * 
     * GET /api/stats/all
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<DriverStatsResponse>>> getAllDriverStats() {
        log.debug("Getting all driver statistics");
        
        List<DriverStats> statsList = driverStatsService.getAllDriverStats();
        List<DriverStatsResponse> responseList = statsList.stream()
                .map(entityMapper::toDriverStatsResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * Get drivers needing attention (WARNING or CRITICAL status)
     * 
     * GET /api/stats/needing-attention
     */
    @GetMapping("/needing-attention")
    public ResponseEntity<ApiResponse<List<DriverStatsResponse>>> getDriversNeedingAttention() {
        log.debug("Getting drivers needing attention");
        
        List<DriverStats> statsList = driverStatsService.getDriversNeedingAttention();
        List<DriverStatsResponse> responseList = statsList.stream()
                .map(entityMapper::toDriverStatsResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * Get drivers with critical alerts
     * 
     * GET /api/stats/critical
     */
    @GetMapping("/critical")
    public ResponseEntity<ApiResponse<List<DriverStatsResponse>>> getCriticalDrivers() {
        log.debug("Getting drivers with critical alerts");
        
        List<DriverStats> statsList = driverStatsService.getDriversWithCriticalAlerts();
        List<DriverStatsResponse> responseList = statsList.stream()
                .map(entityMapper::toDriverStatsResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * Get drivers with improving sentiment
     * 
     * GET /api/stats/improving
     */
    @GetMapping("/improving")
    public ResponseEntity<ApiResponse<List<DriverStatsResponse>>> getImprovingDrivers() {
        log.debug("Getting drivers with improving sentiment");
        
        List<DriverStats> statsList = driverStatsService.getDriversWithImprovingSentiment();
        List<DriverStatsResponse> responseList = statsList.stream()
                .map(entityMapper::toDriverStatsResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * Get drivers with declining sentiment
     * 
     * GET /api/stats/declining
     */
    @GetMapping("/declining")
    public ResponseEntity<ApiResponse<List<DriverStatsResponse>>> getDecliningDrivers() {
        log.debug("Getting drivers with declining sentiment");
        
        List<DriverStats> statsList = driverStatsService.getDriversWithDecliningSentiment();
        List<DriverStatsResponse> responseList = statsList.stream()
                .map(entityMapper::toDriverStatsResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * Get system overview statistics
     * 
     * GET /api/stats/overview
     */
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<SystemOverview>> getSystemOverview() {
        log.debug("Getting system overview");
        
        List<DriverStats> allStats = driverStatsService.getAllDriverStats();
        long totalDrivers = allStats.size();
        long normalCount = allStats.stream().filter(s -> s.getAlertStatus() == DriverStats.AlertStatus.NORMAL).count();
        long warningCount = allStats.stream().filter(s -> s.getAlertStatus() == DriverStats.AlertStatus.WARNING).count();
        long criticalCount = allStats.stream().filter(s -> s.getAlertStatus() == DriverStats.AlertStatus.CRITICAL).count();
        
        SystemOverview overview = new SystemOverview(
                totalDrivers,
                normalCount,
                warningCount,
                criticalCount,
                driverStatsService.getDriversWithImprovingSentiment().size(),
                driverStatsService.getDriversWithDecliningSentiment().size()
        );
        
        return ResponseEntity.ok(ApiResponse.success(overview));
    }

    /**
     * System Overview DTO
     */
    public record SystemOverview(
            long totalDrivers,
            long normalCount,
            long warningCount,
            long criticalCount,
            long improvingCount,
            long decliningCount
    ) {}
}
