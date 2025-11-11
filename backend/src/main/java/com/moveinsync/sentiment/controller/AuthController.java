package com.moveinsync.sentiment.controller;

import com.moveinsync.sentiment.dto.ApiResponse;
import com.moveinsync.sentiment.dto.auth.AuthResponse;
import com.moveinsync.sentiment.dto.auth.LoginRequest;
import com.moveinsync.sentiment.dto.auth.RegisterRequest;
import com.moveinsync.sentiment.model.User;
import com.moveinsync.sentiment.repository.UserRepository;
import com.moveinsync.sentiment.security.JwtTokenProvider;
import com.moveinsync.sentiment.security.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * Handles user registration, login, and profile retrieval
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    /**
     * User login
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            String token = tokenProvider.generateToken(authentication);

            // Get user info
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update last login
            user.updateLastLogin();
            userRepository.save(user);

            // Build response
            AuthResponse authResponse = AuthResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .expiresIn(tokenProvider.getExpirationTime())
                    .user(AuthResponse.UserInfo.builder()
                            .id(user.getId())
                            .name(user.getName())
                            .email(user.getEmail())
                            .role(user.getRole())
                            .driverId(user.getDriverId())
                            .isActive(user.getIsActive())
                            .build())
                    .build();

            log.info("User logged in successfully: {}", user.getEmail());
            return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));

        } catch (Exception e) {
            log.error("Login failed for email: {}", request.getEmail(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid email or password"));
        }
    }

    /**
     * User registration
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse.UserInfo>> register(
            @Valid @RequestBody RegisterRequest request) {
        
        log.info("Registration attempt for email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration failed: Email already exists: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Email already registered"));
        }

        // Create new user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole() != null ? request.getRole() : User.UserRole.EMPLOYEE)
                .driverId(request.getDriverId())
                .licenseNumber(request.getLicenseNumber())
                .vehicleNumber(request.getVehicleNumber())
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        // Build response
        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .driverId(savedUser.getDriverId())
                .isActive(savedUser.getIsActive())
                .build();

        log.info("User registered successfully: {} with role: {}", savedUser.getEmail(), savedUser.getRole());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", userInfo));
    }

    /**
     * Get current user profile
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse.UserInfo>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated"));
        }

        UserDetailsServiceImpl.UserPrincipal principal = (UserDetailsServiceImpl.UserPrincipal) userDetails;
        
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .driverId(user.getDriverId())
                .isActive(user.getIsActive())
                .build();

        return ResponseEntity.ok(ApiResponse.success("User profile retrieved", userInfo));
    }

    /**
     * Logout (client-side only - invalidate token)
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ApiResponse.success("Logout successful", "OK"));
    }
}
