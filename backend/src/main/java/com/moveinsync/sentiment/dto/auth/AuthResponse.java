package com.moveinsync.sentiment.dto.auth;

import com.moveinsync.sentiment.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication response DTO
 * Contains JWT token and user information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    
    @Builder.Default
    private String tokenType = "Bearer";
    
    private Long expiresIn;  // Expiration time in milliseconds
    private UserInfo user;

    /**
     * User information included in auth response
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String name;
        private String email;
        private User.UserRole role;
        private Long driverId;
        private Boolean isActive;
    }
}
