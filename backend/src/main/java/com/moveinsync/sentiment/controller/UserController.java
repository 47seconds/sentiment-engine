package com.moveinsync.sentiment.controller;

import com.moveinsync.sentiment.dto.ApiResponse;
import com.moveinsync.sentiment.model.User;
import com.moveinsync.sentiment.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for User operations
 * 
 * Endpoints:
 * - POST   /api/users                    - Create user
 * - POST   /api/auth/login               - Authenticate user
 * - GET    /api/users/{id}               - Get user by ID
 * - GET    /api/users/email/{email}      - Get user by email
 * - GET    /api/users/all                - Get all users
 * - GET    /api/users/drivers            - Get all drivers
 * - GET    /api/users/managers           - Get all managers
 * - PUT    /api/users/{id}               - Update user
 * - DELETE /api/users/{id}               - Delete user
 * - PUT    /api/users/{id}/password      - Change password
 * - PUT    /api/users/{id}/deactivate    - Deactivate user
 * - PUT    /api/users/{id}/activate      - Activate user
 */
@Slf4j
@RestController
@RequestMapping("")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Create new driver (ADMIN ONLY)
     * 
     * POST /api/users/drivers
     */
    @PostMapping("/users/drivers")
    public ResponseEntity<ApiResponse<UserResponse>> createDriver(@Valid @RequestBody DriverCreateRequest request) {
        log.info("Creating driver: {}", request.email());
        
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        
        User currentUser = userService.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
        
        // Check if user is admin
        if (!currentUser.isAdmin()) {
            log.error("User {} attempted to create driver without admin privileges", currentUser.getId());
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only admins can create drivers"));
        }
        
        User driver = new User();
        driver.setEmail(request.email());
        driver.setPassword(request.password());
        driver.setName(request.name());
        driver.setRole(User.UserRole.EMPLOYEE);
        driver.setPhoneNumber(request.phoneNumber());
        driver.setDriverId(request.driverId());
        driver.setLicenseNumber(request.licenseNumber());
        driver.setVehicleNumber(request.vehicleNumber());
        driver.setIsActive(true);
        
        User savedDriver = userService.createUser(driver);
        UserResponse response = toUserResponse(savedDriver);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Driver created successfully", response));
    }

    /**
     * Create new user (ADMIN ONLY)
     * 
     * POST /api/users
     */
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("Creating user: {}", request.email());
        
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        
        User currentUser = userService.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
        
        // Check if user is admin
        if (!currentUser.isAdmin()) {
            log.error("User {} attempted to create user without admin privileges", currentUser.getId());
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only admins can create users"));
        }
        
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setName(request.name());
        user.setRole(request.role());
        user.setPhoneNumber(request.phoneNumber());
        user.setDriverId(request.driverId());
        user.setLicenseNumber(request.licenseNumber());
        user.setVehicleNumber(request.vehicleNumber());
        user.setIsActive(true);
        
        User savedUser = userService.createUser(user);
        UserResponse response = toUserResponse(savedUser);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", response));
    }

    /**
     * Get user by ID
     * 
     * GET /api/users/{id}
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        log.debug("Getting user: {}", id);
        
        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        
        UserResponse response = toUserResponse(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get user by email
     * 
     * GET /api/users/email/{email}
     */
    @GetMapping("/users/email/{email}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email) {
        log.debug("Getting user by email: {}", email);
        
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
        
        UserResponse response = toUserResponse(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all users
     * 
     * GET /api/users/all
     */
    @GetMapping("/users/all")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        log.debug("Getting all users");
        
        List<User> users = userService.getAllUsers();
        List<UserResponse> responseList = users.stream()
                .map(this::toUserResponse)
                .toList();
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * Get all active users
     * 
     * GET /api/users/active
     */
    @GetMapping("/users/active")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getActiveUsers() {
        log.debug("Getting active users");
        
        List<User> users = userService.getActiveUsers();
        List<UserResponse> responseList = users.stream()
                .map(this::toUserResponse)
                .toList();
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * Get all actual drivers (users with driver-specific data)
     * 
     * GET /api/users/actual-drivers
     */
    @GetMapping("/users/actual-drivers")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getActualDrivers() {
        log.debug("Getting actual drivers only");
        
        List<User> drivers = userService.getUsersByRole(User.UserRole.EMPLOYEE)
                .stream()
                .filter(user -> user.getDriverId() != null && 
                               (user.getLicenseNumber() != null || user.getVehicleNumber() != null))
                .toList();
        
        List<UserResponse> responseList = drivers.stream()
                .map(this::toUserResponse)
                .toList();
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * Get all drivers
     * 
     * GET /api/users/drivers
     */
    @GetMapping("/users/drivers")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getDrivers() {
        log.debug("Getting all drivers");
        
        List<User> drivers = userService.getUsersByRole(User.UserRole.EMPLOYEE);
        List<UserResponse> responseList = drivers.stream()
                .map(this::toUserResponse)
                .toList();
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * Validate driver ID exists
     * 
     * GET /api/users/drivers/{id}/validate
     */
    @GetMapping("/users/drivers/{id}/validate")
    public ResponseEntity<ApiResponse<DriverValidationResponse>> validateDriverId(@PathVariable Long id) {
        log.debug("Validating driver ID: {}", id);
        
        try {
            Optional<User> driverOpt = userService.findByDriverId(id);
            
            if (driverOpt.isPresent()) {
                User driver = driverOpt.get();
                
                // Check if user is active
                if (!driver.getIsActive()) {
                    return ResponseEntity.ok(
                        ApiResponse.success(new DriverValidationResponse(false, "Driver is inactive"))
                    );
                }
                
                // Check if user has driver role
                if (driver.getRole() != User.UserRole.EMPLOYEE) {
                    return ResponseEntity.ok(
                        ApiResponse.success(new DriverValidationResponse(false, "User is not a driver"))
                    );
                }
                
                return ResponseEntity.ok(
                    ApiResponse.success(new DriverValidationResponse(true, "Driver ID is valid"))
                );
            } else {
                return ResponseEntity.ok(
                    ApiResponse.success(new DriverValidationResponse(false, "Driver ID not found"))
                );
            }
        } catch (Exception e) {
            log.error("Error validating driver ID {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error validating driver ID"));
        }
    }

    /**
     * Get all managers
     * 
     * GET /api/users/managers
     */
    @GetMapping("/users/managers")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getManagers() {
        log.debug("Getting all managers");
        
        List<User> managers = userService.getUsersByRole(User.UserRole.MANAGER);
        List<UserResponse> responseList = managers.stream()
                .map(this::toUserResponse)
                .toList();
        
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * Update user
     * 
     * PUT /api/users/{id}
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("Updating user: {}", id);
        
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        
        User currentUser = userService.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
        
        User targetUser = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        
        // Check if user is updating their own profile or is an admin
        boolean isAdmin = currentUser.getRole() == User.UserRole.ADMIN || 
                         currentUser.getRole() == User.UserRole.MANAGER;
        boolean isSelfUpdate = currentUser.getId().equals(id);
        
        if (!isSelfUpdate && !isAdmin) {
            log.error("User {} attempted to update user {}", currentUser.getId(), id);
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("You can only update your own profile"));
        }
        
        // Users can update their own name, email, and phone
        if (request.name() != null) {
            targetUser.setName(request.name());
        }
        if (request.email() != null) {
            targetUser.setEmail(request.email());
        }
        if (request.phoneNumber() != null) {
            targetUser.setPhoneNumber(request.phoneNumber());
        }
        if (request.driverId() != null) {
            targetUser.setDriverId(request.driverId());
        }
        if (request.licenseNumber() != null) {
            targetUser.setLicenseNumber(request.licenseNumber());
        }
        if (request.vehicleNumber() != null) {
            targetUser.setVehicleNumber(request.vehicleNumber());
        }
        
        // Only admins can change roles
        if (request.role() != null) {
            if (!isAdmin) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Only admins can change user roles"));
            }
            targetUser.setRole(request.role());
        }
        
        User updatedUser = userService.updateUser(id, targetUser);
        UserResponse response = toUserResponse(updatedUser);
        
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", response));
    }

    /**
     * Change password
     * 
     * PUT /api/users/{id}/password
     */
    @PutMapping("/users/{id}/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody PasswordChangeRequest request) {
        log.info("Changing password for user: {}", id);
        
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        
        User currentUser = userService.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
        
        // Check if user is changing their own password
        if (!currentUser.getId().equals(id)) {
            log.error("User {} attempted to change password for user {}", currentUser.getId(), id);
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("You can only change your own password"));
        }
        
        try {
            userService.changePassword(id, request.currentPassword(), request.newPassword());
            return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
        } catch (IllegalArgumentException e) {
            log.error("Password change failed: {}", e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Deactivate user
     * 
     * PUT /api/users/{id}/deactivate
     */
    @PutMapping("/users/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
        log.info("Deactivating user: {}", id);
        
        userService.deactivateUser(id);
        
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", null));
    }

    /**
     * Activate user
     * 
     * PUT /api/users/{id}/activate
     */
    @PutMapping("/users/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable Long id) {
        log.info("Activating user: {}", id);
        
        userService.activateUser(id);
        
        return ResponseEntity.ok(ApiResponse.success("User activated successfully", null));
    }

    /**
     * Delete user
     * 
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("Deleting user: {}", id);
        
        userService.deleteUser(id);
        
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    // Helper method to convert User to UserResponse (excluding password)
    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getDriverId(),
                user.getEmail(),
                user.getName(),
                user.getPhoneNumber(),
                user.getLicenseNumber(),
                user.getVehicleNumber(),
                user.getRole(),
                user.getIsActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    // DTOs
    public record DriverCreateRequest(
            @jakarta.validation.constraints.NotBlank String email,
            @jakarta.validation.constraints.NotBlank 
            @jakarta.validation.constraints.Size(min = 8, message = "Password must be at least 8 characters") 
            String password,
            @jakarta.validation.constraints.NotBlank String name,
            String phoneNumber,
            Long driverId,
            @jakarta.validation.constraints.NotBlank String licenseNumber,
            @jakarta.validation.constraints.NotBlank String vehicleNumber
    ) {}

    public record UserCreateRequest(
            @jakarta.validation.constraints.NotBlank String email,
            @jakarta.validation.constraints.NotBlank 
            @jakarta.validation.constraints.Size(min = 8, message = "Password must be at least 8 characters") 
            String password,
            @jakarta.validation.constraints.NotBlank String name,
            String phoneNumber,
            Long driverId,
            String licenseNumber,
            String vehicleNumber,
            @jakarta.validation.constraints.NotNull User.UserRole role
    ) {}

    public record UserUpdateRequest(
            String email,
            String name,
            String phoneNumber,
            Long driverId,
            String licenseNumber,
            String vehicleNumber,
            User.UserRole role
    ) {}

    public record LoginRequest(
            @jakarta.validation.constraints.NotBlank String email,
            @jakarta.validation.constraints.NotBlank String password
    ) {}

    public record PasswordChangeRequest(
            @jakarta.validation.constraints.NotBlank String currentPassword,
            @jakarta.validation.constraints.NotBlank String newPassword
    ) {}

    public record UserResponse(
            Long id,
            Long driverId,
            String email,
            String name,
            String phoneNumber,
            String licenseNumber,
            String vehicleNumber,
            User.UserRole role,
            boolean active,
            java.time.LocalDateTime createdAt,
            java.time.LocalDateTime updatedAt
    ) {}

    public record DriverValidationResponse(
            boolean isValid,
            String message
    ) {}
}
