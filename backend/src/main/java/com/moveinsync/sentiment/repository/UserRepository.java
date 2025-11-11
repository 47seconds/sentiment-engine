package com.moveinsync.sentiment.repository;

import com.moveinsync.sentiment.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by driver ID
     */
    Optional<User> findByDriverId(Long driverId);

    /**
     * Find all users by role
     */
    List<User> findByRole(User.UserRole role);

    /**
     * Find all active users
     */
    List<User> findByIsActiveTrue();

    /**
     * Find all active users by role
     */
    List<User> findByRoleAndIsActiveTrue(User.UserRole role);

    /**
     * Find all drivers
     */
    @Query("SELECT u FROM User u WHERE u.role = 'EMPLOYEE' AND u.driverId IS NOT NULL")
    List<User> findAllDrivers();

    /**
     * Find active drivers
     */
    @Query("SELECT u FROM User u WHERE u.role = 'EMPLOYEE' AND u.driverId IS NOT NULL AND u.isActive = true")
    List<User> findActiveDrivers();

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if driver ID exists
     */
    boolean existsByDriverId(Long driverId);

    /**
     * Find users who haven't logged in since a specific date
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :date OR u.lastLoginAt IS NULL")
    List<User> findInactiveUsersSince(@Param("date") LocalDateTime date);

    /**
     * Find users with specific permission
     */
    @Query("SELECT u FROM User u JOIN u.permissions p WHERE p = :permission")
    List<User> findByPermission(@Param("permission") String permission);

    /**
     * Count users by role
     */
    long countByRole(User.UserRole role);

    /**
     * Count active users
     */
    long countByIsActiveTrue();

    /**
     * Find users created after a specific date
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Search users by name (case-insensitive)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> searchByName(@Param("name") String name);
}
