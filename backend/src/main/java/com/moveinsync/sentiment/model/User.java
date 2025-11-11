package com.moveinsync.sentiment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity representing system users (drivers, admins, support staff)
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_role", columnList = "role"),
    @Index(name = "idx_user_active", columnList = "is_active"),
    @Index(name = "idx_user_driver", columnList = "driver_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "driver_id", unique = true)
    private Long driverId;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8)
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.EMPLOYEE;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "license_number", length = 50)
    private String licenseNumber;

    @Column(name = "vehicle_number", length = 30)
    private String vehicleNumber;

    @ElementCollection
    @CollectionTable(name = "user_permissions", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "permission")
    @Builder.Default
    private Set<String> permissions = new HashSet<>();

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * User roles in the system
     */
    public enum UserRole {
        EMPLOYEE,         // Regular employee/driver
        ADMIN,            // System administrator
        SUPPORT,          // Customer support
        MANAGER,          // Fleet manager
        ANALYST           // Data analyst
    }

    /**
     * Check if user is a driver/employee
     */
    public boolean isDriver() {
        return this.role == UserRole.EMPLOYEE && this.driverId != null;
    }

    /**
     * Check if user has admin privileges
     */
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN || this.role == UserRole.MANAGER;
    }

    /**
     * Check if user has specific permission
     */
    public boolean hasPermission(String permission) {
        return this.permissions.contains(permission);
    }

    /**
     * Add permission to user
     */
    public void addPermission(String permission) {
        this.permissions.add(permission);
    }

    /**
     * Remove permission from user
     */
    public void removePermission(String permission) {
        this.permissions.remove(permission);
    }

    /**
     * Update last login timestamp
     */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * Update password and track when it was changed
     */
    public void updatePassword(String newPassword) {
        this.password = newPassword;
        this.passwordChangedAt = LocalDateTime.now();
    }
}
