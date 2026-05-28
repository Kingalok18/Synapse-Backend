package com.synapse.backend.service;

import com.synapse.backend.dto.request.LoginRequest;
import com.synapse.backend.dto.request.SignupRequest;
import com.synapse.backend.dto.request.TokenRefreshRequest;
import com.synapse.backend.dto.response.AuthResponse;
import com.synapse.backend.dto.response.UserResponse;
import com.synapse.backend.exception.BusinessException;
import com.synapse.backend.exception.ResourceNotFoundException;
import com.synapse.backend.model.Provider;
import com.synapse.backend.model.RefreshToken;
import com.synapse.backend.model.User;
import com.synapse.backend.model.UserRole;
import com.synapse.backend.repository.RefreshTokenRepository;
import com.synapse.backend.repository.UserRepository;
import com.synapse.backend.security.UserPrincipal;
import com.synapse.backend.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenBlacklistService tokenBlacklistService;
    private final UserService userService;

    @Value("${app.security.jwt.refresh-expiration-ms:2592000000}")
    private long refreshExpirationInMs;

    @Transactional
    public UserResponse signup(SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new BusinessException("Email address already in use.", HttpStatus.BAD_REQUEST);
        }

        User user = User.builder()
                .email(signupRequest.getEmail())
                .username(signupRequest.getUsername())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .provider(Provider.LOCAL)
                .role(UserRole.ROLE_USER)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);
        return userService.convertToUserResponse(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String accessToken = jwtTokenProvider.generateToken(authentication);
        RefreshToken refreshToken = createRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .user(userService.convertToUserResponse(user))
                .build();
    }

    @Transactional
    public RefreshToken createRefreshToken(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Delete existing refresh tokens for clean state
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpirationInMs))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        RefreshToken token = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new BusinessException("Refresh token is not in database!", HttpStatus.UNAUTHORIZED));

        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new BusinessException("Refresh token was expired. Please make a new signin request", HttpStatus.UNAUTHORIZED);
        }

        User user = token.getUser();
        
        // Generate new access token
        String accessToken = jwtTokenProvider.generateTokenFromUserId(user.getId());
        
        // Rotate refresh token (delete old and create a new one)
        refreshTokenRepository.delete(token);
        RefreshToken newRefreshToken = createRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken.getToken())
                .user(userService.convertToUserResponse(user))
                .build();
    }

    @Transactional
    public void logout(String jwt, String refreshTokenString) {
        // Blacklist the access token
        if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
            try {
                Date expiration = jwtTokenProvider.getExpirationFromJWT(jwt);
                long expirationMs = expiration.getTime() - System.currentTimeMillis();
                tokenBlacklistService.blacklistToken(jwt, expirationMs);
            } catch (Exception e) {
                // If parsing/validation fails, we still proceed to delete the refresh token
            }
        }

        // Delete refresh token if provided
        if (refreshTokenString != null) {
            refreshTokenRepository.findByToken(refreshTokenString)
                    .ifPresent(refreshTokenRepository::delete);
        }
    }
}
