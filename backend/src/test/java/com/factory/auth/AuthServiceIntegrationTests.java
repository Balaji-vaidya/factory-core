package com.factory.auth;

import com.factory.auth.dto.LoginRequest;
import com.factory.auth.dto.TokenResponse;
import com.factory.auth.service.AuthService;
import com.factory.common.exception.BusinessException;
import com.factory.common.exception.ErrorCode;
import com.factory.common.security.CustomUserDetails;
import com.factory.common.security.SecurityEvaluator;
import com.factory.user.entity.User;
import com.factory.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthServiceIntegrationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityEvaluator securityEvaluator;

    @Test
    @DisplayName("Owner Login Succeeds With Valid Credentials")
    void test_loginSuccess_withValidCredentials() {
        LoginRequest request = new LoginRequest("owner@agrisep.com", "Owner@123");
        TokenResponse response = authService.login(request, "127.0.0.1", "JUnit-Test-Agent");

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("OWNER", response.getUser().getRole());
        assertEquals("owner@agrisep.com", response.getUser().getEmail());
    }

    @Test
    @DisplayName("Login Rejects Invalid Password with ErrorCode.INVALID_CREDENTIALS")
    void test_loginFails_withInvalidPassword() {
        LoginRequest request = new LoginRequest("owner@agrisep.com", "WrongPassword999");
        BusinessException ex = assertThrows(BusinessException.class, () ->
                authService.login(request, "127.0.0.1", "JUnit-Test-Agent"));

        assertEquals(ErrorCode.INVALID_CREDENTIALS, ex.getErrorCode());
    }

    @Test
    @DisplayName("Login Rejects Non-Existent User with ErrorCode.INVALID_CREDENTIALS")
    void test_loginFails_withNonExistentUser() {
        LoginRequest request = new LoginRequest("unknown.user@agrisep.com", "Password@123");
        BusinessException ex = assertThrows(BusinessException.class, () ->
                authService.login(request, "127.0.0.1", "JUnit-Test-Agent"));

        assertEquals(ErrorCode.INVALID_CREDENTIALS, ex.getErrorCode());
    }

    @Test
    @DisplayName("Token Refresh Succeeds and Rotates Tokens")
    void test_refreshToken_success() {
        LoginRequest request = new LoginRequest("godown@agrisep.com", "Godown@123");
        TokenResponse initialToken = authService.login(request, "127.0.0.1", "JUnit-Test-Agent");

        TokenResponse refreshed = authService.refreshToken(initialToken.getRefreshToken());
        assertNotNull(refreshed);
        assertNotNull(refreshed.getAccessToken());
        assertNotNull(refreshed.getRefreshToken());
        assertEquals("GODOWN_MANAGER", refreshed.getUser().getRole());
    }

    @Test
    @DisplayName("Security Evaluator Enforces Office Scoping Between Roles")
    void test_securityEvaluator_officeScoping() {
        User outerUser = userRepository.findByEmail("kiran.reddy@agrisep.com").orElseThrow();
        CustomUserDetails outerUserDetails = new CustomUserDetails(outerUser);
        Authentication outerAuth = new UsernamePasswordAuthenticationToken(outerUserDetails, null, outerUserDetails.getAuthorities());

        User ownerUser = userRepository.findByEmail("owner@agrisep.com").orElseThrow();
        CustomUserDetails ownerUserDetails = new CustomUserDetails(ownerUser);
        Authentication ownerAuth = new UsernamePasswordAuthenticationToken(ownerUserDetails, null, ownerUserDetails.getAuthorities());

        // Outer state user (AP) can access AP office, but CANNOT access Gujarat office
        assertTrue(securityEvaluator.canAccessOffice(outerAuth, "off-reg-ap"));
        assertFalse(securityEvaluator.canAccessOffice(outerAuth, "off-reg-gujarat"));

        // Owner can access all offices unrestricted
        assertTrue(securityEvaluator.canAccessOffice(ownerAuth, "off-reg-ap"));
        assertTrue(securityEvaluator.canAccessOffice(ownerAuth, "off-reg-gujarat"));
        assertTrue(securityEvaluator.canAccessOffice(ownerAuth, "off-hq-jalna"));
    }

    @Test
    @DisplayName("Deactivated User Cannot Log In")
    void test_deactivatedUser_cannotLogIn() {
        User user = userRepository.findByEmail("admin@agrisep.com").orElseThrow();
        user.setStatus(com.factory.user.entity.UserStatus.INACTIVE);
        userRepository.save(user);

        try {
            LoginRequest request = new LoginRequest("admin@agrisep.com", "Admin@123");
            BusinessException ex = assertThrows(BusinessException.class, () ->
                    authService.login(request, "127.0.0.1", "JUnit-Test-Agent"));
            assertEquals(ErrorCode.ACCESS_DENIED, ex.getErrorCode());
        } finally {
            // Restore active status
            user.setStatus(com.factory.user.entity.UserStatus.ACTIVE);
            userRepository.save(user);
        }
    }

    @Test
    @DisplayName("Consecutive Failed Attempts Trigger Temporary Lockout")
    void test_accountLockout_afterFailedAttempts() {
        User user = userRepository.findByEmail("mainoffice@agrisep.com").orElseThrow();
        user.resetFailedAttempts();
        userRepository.save(user);

        LoginRequest badRequest = new LoginRequest("mainoffice@agrisep.com", "WrongPassword");
        for (int i = 0; i < 5; i++) {
            assertThrows(BusinessException.class, () ->
                    authService.login(badRequest, "127.0.0.1", "JUnit-Test-Agent"));
        }

        User lockedUser = userRepository.findByEmail("mainoffice@agrisep.com").orElseThrow();
        assertFalse(lockedUser.isAccountNonLocked());
        assertTrue(lockedUser.getFailedAttempts() >= 5);

        // Clean up
        lockedUser.resetFailedAttempts();
        userRepository.save(lockedUser);
    }
}
