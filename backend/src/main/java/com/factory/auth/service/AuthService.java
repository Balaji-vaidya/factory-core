package com.factory.auth.service;

import com.factory.auth.dto.LoginRequest;
import com.factory.auth.dto.TokenResponse;
import com.factory.auth.dto.UserProfileResponse;
import com.factory.auth.entity.LoginHistory;
import com.factory.auth.repository.LoginHistoryRepository;
import com.factory.common.exception.BusinessException;
import com.factory.common.exception.ErrorCode;
import com.factory.common.security.JwtTokenProvider;
import com.factory.user.entity.User;
import com.factory.user.entity.UserStatus;
import com.factory.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Value("${jwt.access-expiration-ms:900000}")
    private long accessExpirationMs;

    public AuthService(UserRepository userRepository,
                       LoginHistoryRepository loginHistoryRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.loginHistoryRepository = loginHistoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public TokenResponse login(LoginRequest request, String ipAddress, String userAgent) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            recordLoginAttempt(null, email, "FAILED_USER_NOT_FOUND", ipAddress, userAgent);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Invalid email address or password.");
        }

        if (!user.isAccountNonLocked()) {
            recordLoginAttempt(user.getId(), email, "LOCKED", ipAddress, userAgent);
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "Account is temporarily locked due to excessive failed attempts. Please try again later.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            user.incrementFailedAttempts();
            userRepository.save(user);
            recordLoginAttempt(user.getId(), email, "FAILED_INVALID_PASSWORD", ipAddress, userAgent);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Invalid email address or password.");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            recordLoginAttempt(user.getId(), email, "FAILED_INACTIVE", ipAddress, userAgent);
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "Your account has been deactivated. Please contact Owner/Admin.");
        }

        // Login succeeded
        user.resetFailedAttempts();
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        recordLoginAttempt(user.getId(), email, "SUCCESS", ipAddress, userAgent);

        List<String> allowedOfficeIds = user.getAllowedOffices() != null
                ? user.getAllowedOffices().stream().map(o -> o.getId()).collect(Collectors.toList())
                : List.of();

        String officeId = user.getPrimaryOffice() != null ? user.getPrimaryOffice().getId() : "";
        String officeName = user.getPrimaryOffice() != null ? user.getPrimaryOffice().getName() : "";
        String state = user.getPrimaryOffice() != null ? user.getPrimaryOffice().getState() : "";

        String accessToken = tokenProvider.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                officeId,
                state,
                allowedOfficeIds
        );

        String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());

        UserProfileResponse userProfile = new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRole().name(),
                officeId,
                officeName,
                state,
                allowedOfficeIds,
                user.getStatus().name()
        );

        log.info("User logged in successfully: [{}] [{}] Role=[{}]", user.getId(), user.getEmail(), user.getRole());
        return new TokenResponse(accessToken, refreshToken, accessExpirationMs, userProfile);
    }

    @Transactional(readOnly = true)
    public TokenResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED, "Refresh token is invalid or expired.");
        }

        Map<String, Object> claims = tokenProvider.getClaims(refreshToken);
        String type = (String) claims.get("type");
        if (!"REFRESH".equals(type)) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED, "Invalid token type for refresh operation.");
        }

        String email = tokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "User no longer exists."));

        if (user.getStatus() != UserStatus.ACTIVE || !user.isAccountNonLocked()) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "User account is suspended or locked.");
        }

        List<String> allowedOfficeIds = user.getAllowedOffices() != null
                ? user.getAllowedOffices().stream().map(o -> o.getId()).collect(Collectors.toList())
                : List.of();

        String officeId = user.getPrimaryOffice() != null ? user.getPrimaryOffice().getId() : "";
        String officeName = user.getPrimaryOffice() != null ? user.getPrimaryOffice().getName() : "";
        String state = user.getPrimaryOffice() != null ? user.getPrimaryOffice().getState() : "";

        String newAccessToken = tokenProvider.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                officeId,
                state,
                allowedOfficeIds
        );

        String newRefreshToken = tokenProvider.generateRefreshToken(user.getEmail());

        UserProfileResponse userProfile = new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRole().name(),
                officeId,
                officeName,
                state,
                allowedOfficeIds,
                user.getStatus().name()
        );

        return new TokenResponse(newAccessToken, newRefreshToken, accessExpirationMs, userProfile);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "User profile not found."));

        List<String> allowedOfficeIds = user.getAllowedOffices() != null
                ? user.getAllowedOffices().stream().map(o -> o.getId()).collect(Collectors.toList())
                : List.of();

        String officeId = user.getPrimaryOffice() != null ? user.getPrimaryOffice().getId() : "";
        String officeName = user.getPrimaryOffice() != null ? user.getPrimaryOffice().getName() : "";
        String state = user.getPrimaryOffice() != null ? user.getPrimaryOffice().getState() : "";

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRole().name(),
                officeId,
                officeName,
                state,
                allowedOfficeIds,
                user.getStatus().name()
        );
    }

    private void recordLoginAttempt(String userId, String email, String status, String ip, String userAgent) {
        LoginHistory history = new LoginHistory(
                UUID.randomUUID().toString(),
                userId,
                email,
                status,
                ip,
                userAgent
        );
        loginHistoryRepository.save(history);
    }
}
