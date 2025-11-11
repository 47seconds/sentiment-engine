package com.moveinsync.sentiment.dto.auth;

import com.moveinsync.sentiment.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Registration request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private String phoneNumber;
    
    @Builder.Default
    private User.UserRole role = User.UserRole.EMPLOYEE;  // Default to EMPLOYEE
    
    private Long driverId;  // Optional - for drivers only
    
    private String licenseNumber;  // Optional - for drivers only
    
    private String vehicleNumber;  // Optional - for drivers only
}
