package com.moveinsync.sentiment.service;

import com.moveinsync.sentiment.model.User;
import com.moveinsync.sentiment.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * User Service
 * 
 * Handles user management operations including:
 * - User creation and updates
 * - Authentication and authorization
 * - Driver profile management
 * - User search and filtering
 * 
 * Uses Redis caching for frequently accessed user data.
 */
@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create a new user
     * 
     * @param user User to create
     * @return Created user
     * @throws IllegalArgumentException if email already exists
     */
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public User createUser(User user) {
        log.info("Creating new user: email={}, role={}", user.getEmail(), user.getRole());
        
        // Validate email uniqueness
        if (userRepository.existsByEmail(user.getEmail())) {
            log.error("User creation failed: email already exists: {}", user.getEmail());
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        
        // Validate driver ID uniqueness if provided
        if (user.getDriverId() != null && userRepository.existsByDriverId(user.getDriverId())) {
            log.error("User creation failed: driver ID already exists: {}", user.getDriverId());
            throw new IllegalArgumentException("Driver ID already exists: " + user.getDriverId());
        }
        
        // Hash password
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        // Set default values
        if (user.getPermissions() == null) {
            user.setPermissions(new HashSet<>());
        }
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }
        
        User savedUser = userRepository.save(user);
        log.info("User created successfully: id={}, email={}, role={}", 
                savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
        
        return savedUser;
    }

    /**
     * Update an existing user
     * 
     * @param id User ID
     * @param updatedUser Updated user data
     * @return Updated user
     * @throws IllegalArgumentException if user not found
     */
    @Transactional
    @CacheEvict(value = {"users", "userById", "userByEmail", "userByDriverId"}, allEntries = true)
    public User updateUser(Long id, User updatedUser) {
        log.info("Updating user: id={}", id);
        
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        
        // Update allowed fields
        if (updatedUser.getName() != null) {
            existingUser.setName(updatedUser.getName());
        }
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(updatedUser.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + updatedUser.getEmail());
            }
            existingUser.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        }
        if (updatedUser.getRole() != null) {
            existingUser.setRole(updatedUser.getRole());
        }
        if (updatedUser.getDriverId() != null && !updatedUser.getDriverId().equals(existingUser.getDriverId())) {
            if (userRepository.existsByDriverId(updatedUser.getDriverId())) {
                throw new IllegalArgumentException("Driver ID already exists: " + updatedUser.getDriverId());
            }
            existingUser.setDriverId(updatedUser.getDriverId());
        }
        if (updatedUser.getPermissions() != null) {
            existingUser.setPermissions(updatedUser.getPermissions());
        }
        if (updatedUser.getIsActive() != null) {
            existingUser.setIsActive(updatedUser.getIsActive());
        }
        
        // Hash new password if provided
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        
        User savedUser = userRepository.save(existingUser);
        log.info("User updated successfully: id={}, email={}", savedUser.getId(), savedUser.getEmail());
        
        return savedUser;
    }

    /**
     * Find user by ID (cached)
     * 
     * @param id User ID
     * @return User if found
     */
    @Cacheable(value = "userById", key = "#id")
    public Optional<User> findById(Long id) {
        log.debug("Finding user by id: {}", id);
        return userRepository.findById(id);
    }

    /**
     * Find user by email (cached)
     * 
     * @param email User email
     * @return User if found
     */
    @Cacheable(value = "userByEmail", key = "#email")
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    /**
     * Find user by driver ID (cached)
     * 
     * @param driverId Driver ID
     * @return User if found
     */
    @Cacheable(value = "userByDriverId", key = "#driverId")
    public Optional<User> findByDriverId(Long driverId) {
        log.debug("Finding user by driver ID: {}", driverId);
        return userRepository.findByDriverId(driverId);
    }

    /**
     * Get all users
     * 
     * @return List of all users
     */
    public List<User> getAllUsers() {
        log.debug("Getting all users");
        return userRepository.findAll();
    }

    /**
     * Get all active users
     * 
     * @return List of active users
     */
    public List<User> getActiveUsers() {
        log.debug("Getting all active users");
        return userRepository.findByIsActiveTrue();
    }

    /**
     * Get users by role
     * 
     * @param role User role
     * @return List of users with the specified role
     */
    public List<User> getUsersByRole(User.UserRole role) {
        log.debug("Getting users by role: {}", role);
        return userRepository.findByRole(role);
    }

    /**
     * Get all drivers
     * 
     * @return List of all driver users
     */
    public List<User> getAllDrivers() {
        log.debug("Getting all drivers");
        return userRepository.findAllDrivers();
    }

    /**
     * Get active drivers
     * 
     * @return List of active driver users
     */
    public List<User> getActiveDrivers() {
        log.debug("Getting active drivers");
        return userRepository.findActiveDrivers();
    }

    /**
     * Authenticate user
     * 
     * @param email User email
     * @param password Plain text password
     * @return User if authentication successful
     */
    @Transactional
    public Optional<User> authenticate(String email, String password) {
        log.info("Authenticating user: email={}", email);
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            log.warn("Authentication failed: user not found: {}", email);
            return Optional.empty();
        }
        
        User user = userOpt.get();
        
        if (!user.getIsActive()) {
            log.warn("Authentication failed: user inactive: {}", email);
            return Optional.empty();
        }
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Authentication failed: invalid password: {}", email);
            return Optional.empty();
        }
        
        // Update last login time
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("User authenticated successfully: id={}, email={}", user.getId(), user.getEmail());
        return Optional.of(user);
    }

    /**
     * Check if user has permission
     * 
     * @param userId User ID
     * @param permission Permission to check
     * @return true if user has permission
     */
    public boolean hasPermission(Long userId, String permission) {
        log.debug("Checking permission: userId={}, permission={}", userId, permission);
        
        Optional<User> userOpt = findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        return user.getPermissions() != null && user.getPermissions().contains(permission);
    }

    /**
     * Add permission to user
     * 
     * @param userId User ID
     * @param permission Permission to add
     */
    @Transactional
    @CacheEvict(value = {"users", "userById"}, allEntries = true)
    public void addPermission(Long userId, String permission) {
        log.info("Adding permission to user: userId={}, permission={}", userId, permission);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        if (user.getPermissions() == null) {
            user.setPermissions(new HashSet<>());
        }
        
        user.getPermissions().add(permission);
        userRepository.save(user);
        
        log.info("Permission added successfully: userId={}, permission={}", userId, permission);
    }

    /**
     * Remove permission from user
     * 
     * @param userId User ID
     * @param permission Permission to remove
     */
    @Transactional
    @CacheEvict(value = {"users", "userById"}, allEntries = true)
    public void removePermission(Long userId, String permission) {
        log.info("Removing permission from user: userId={}, permission={}", userId, permission);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        if (user.getPermissions() != null) {
            user.getPermissions().remove(permission);
            userRepository.save(user);
            log.info("Permission removed successfully: userId={}, permission={}", userId, permission);
        }
    }

    /**
     * Deactivate user
     * 
     * @param userId User ID
     */
    @Transactional
    @CacheEvict(value = {"users", "userById", "userByEmail", "userByDriverId"}, allEntries = true)
    public void deactivateUser(Long userId) {
        log.info("Deactivating user: userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        user.setIsActive(false);
        userRepository.save(user);
        
        log.info("User deactivated successfully: userId={}", userId);
    }

    /**
     * Activate user
     * 
     * @param userId User ID
     */
    @Transactional
    @CacheEvict(value = {"users", "userById", "userByEmail", "userByDriverId"}, allEntries = true)
    public void activateUser(Long userId) {
        log.info("Activating user: userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        user.setIsActive(true);
        userRepository.save(user);
        
        log.info("User activated successfully: userId={}", userId);
    }

    /**
     * Delete user
     * 
     * @param userId User ID
     */
    @Transactional
    @CacheEvict(value = {"users", "userById", "userByEmail", "userByDriverId"}, allEntries = true)
    public void deleteUser(Long userId) {
        log.info("Deleting user: userId={}", userId);
        
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        
        userRepository.deleteById(userId);
        log.info("User deleted successfully: userId={}", userId);
    }

    /**
     * Change user password with current password validation
     * 
     * @param userId User ID
     * @param currentPassword Current password
     * @param newPassword New password
     * @throws IllegalArgumentException if current password is incorrect
     */
    @Transactional
    @CacheEvict(value = {"users", "userById", "userByEmail", "userByDriverId"}, allEntries = true)
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        log.info("Changing password for user: userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        // Validate current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.error("Password change failed: incorrect current password for user: {}", userId);
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        // Update password
        user.updatePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("Password changed successfully for user: userId={}", userId);
    }

    /**
     * Search users by name
     * 
     * @param name Name to search for
     * @return List of matching users
     */
    public List<User> searchByName(String name) {
        log.debug("Searching users by name: {}", name);
        return userRepository.searchByName(name);
    }

    /**
     * Get inactive users (no login since date)
     * 
     * @param since Date to check inactivity from
     * @return List of inactive users
     */
    public List<User> getInactiveUsers(LocalDateTime since) {
        log.debug("Getting inactive users since: {}", since);
        return userRepository.findInactiveUsersSince(since);
    }

    /**
     * Get user statistics
     * 
     * @return User statistics map
     */
    public UserStatistics getUserStatistics() {
        log.debug("Getting user statistics");
        
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsActiveTrue();
        long driverCount = userRepository.countByRole(User.UserRole.EMPLOYEE);
        long managerCount = userRepository.countByRole(User.UserRole.MANAGER);
        long adminCount = userRepository.countByRole(User.UserRole.ADMIN);
        
        return new UserStatistics(totalUsers, activeUsers, driverCount, managerCount, adminCount);
    }

    /**
     * User statistics POJO
     */
    public record UserStatistics(
        long totalUsers,
        long activeUsers,
        long driverCount,
        long managerCount,
        long adminCount
    ) {}
}
