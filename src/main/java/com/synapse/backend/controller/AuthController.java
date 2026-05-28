package com.synapse.backend.controller;

import com.synapse.backend.dto.ApiResponse;
import com.synapse.backend.dto.request.LoginRequest;
import com.synapse.backend.dto.request.SignupRequest;
import com.synapse.backend.dto.request.TokenRefreshRequest;
import com.synapse.backend.dto.response.AuthResponse;
import com.synapse.backend.dto.response.UserResponse;
import com.synapse.backend.security.UserPrincipal;
import com.synapse.backend.service.AuthService;
import com.synapse.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication and authorization")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/signup")
    @Operation(summary = "Register a new user", description = "Registers a new user with email, username, and password.")
    public ResponseEntity<ApiResponse<UserResponse>> signup(@Valid @RequestBody SignupRequest signupRequest) {
        UserResponse userResponse = authService.signup(signupRequest);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", userResponse));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Logs in a user with email and password, returning JWT access token and refresh token.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Uses a valid refresh token to rotate both the access token and the refresh token.")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody TokenRefreshRequest refreshRequest) {
        AuthResponse authResponse = authService.refreshToken(refreshRequest);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", authResponse));
    }

    @PostMapping("/logout")
    @Operation(summary = "Log out user", description = "Blacklists the current active access token and deletes the associated refresh token.")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, @RequestBody(required = false) TokenRefreshRequest refreshRequest) {
        String bearerToken = request.getHeader("Authorization");
        String jwt = null;
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            jwt = bearerToken.substring(7);
        }

        String refreshTokenString = refreshRequest != null ? refreshRequest.getRefreshToken() : null;
        authService.logout(jwt, refreshTokenString);

        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user's profile", description = "Retrieves profile details of the currently logged-in user.")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        UserResponse userResponse = userService.getUserProfile(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", userResponse));
    }
}
