package com.inventory.service;

import com.inventory.dto.LoginRequest;
import com.inventory.dto.LoginResponse;
import com.inventory.entity.UserAccount;
import com.inventory.entity.Tenant;
import com.inventory.entity.UserSession;
import com.inventory.security.UserPrincipal;
import com.inventory.repository.UserAccountRepository;
import com.inventory.security.JwtTokenProvider;
import com.inventory.service.AuthenticationService;
import com.inventory.service.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.Collections;
import java.time.LocalDateTime;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import com.inventory.dto.ChangePasswordRequest;
import com.inventory.service.PasswordService.PasswordValidationResult;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * AuthenticationServiceTest
 *
 * This file is a scaffold that follows the test plan in
 * Documentation/service-testing.md (AuthenticationService Tests).
 *
 * It intentionally starts disabled so it can be wired to the real
 * AuthenticationService implementation in a controlled change.
 *
 * Suggested next steps to enable tests:
 *  - Wire an AuthenticationService instance in {@link #setUp()} (constructor or Spring test context)
 *  - Replace the placeholder assertions with real assertions against the service return values
 *  - Remove the {@code @Disabled} annotation once the tests are runnable
 */
@ExtendWith(MockitoExtension.class)
// @Disabled("Scaffolded tests - enable after wiring AuthenticationService and its dependencies")
public class AuthenticationServiceTest {

    // mocks for AuthenticationService dependencies
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private SessionService sessionService;
    @Mock private LoginAttemptService loginAttemptService;
    @Mock private UserAccountRepository userAccountRepository;
    @Mock private PasswordService passwordService;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
    // instantiate the real service with mocked dependencies (constructor injection)
    authenticationService = new AuthenticationService(
        authenticationManager,
        jwtTokenProvider,
        sessionService,
        loginAttemptService,
        passwordService,
        userAccountRepository
    );
    }

    @Test
    void testRefreshToken_shouldReturnNewAccessToken() {
        // Arrange
        String refreshToken = "refresh-token-sample";
        Long userId = 2L;

        when(jwtTokenProvider.isTokenValid(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(refreshToken)).thenReturn(userId);

        UserAccount user = new UserAccount();
        user.setId(userId);
        user.setEmail("user@example.com");
        Tenant tenant = new Tenant(); tenant.setId(200L); user.setTenant(tenant);
        user.setRoles(new HashSet<>());

        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(user));

        UserPrincipal principal = UserPrincipal.create(user);
        String newAccess = "new-access-token";
        when(jwtTokenProvider.generateAccessToken(eq(principal))).thenReturn(newAccess);

        // Act
        LoginResponse resp = authenticationService.refreshToken(refreshToken);

        // Assert
        assertNotNull(resp);
        assertEquals(newAccess, resp.getAccessToken());
        assertEquals(refreshToken, resp.getRefreshToken());
        assertEquals("Bearer", resp.getTokenType());
    }

    @Test
    void testChangePassword_shouldUpdatePasswordAndTerminateSessions() {
        // Arrange
        Long userId = 3L;
        UserAccount user = new UserAccount();
        user.setId(userId);
        user.setEmail("pwuser@example.com");
        Tenant tenant = new Tenant(); tenant.setId(300L); user.setTenant(tenant);
        user.setPasswordHash("oldHash".getBytes());
        user.setRoles(new HashSet<>());

        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(user));

        ChangePasswordRequest req = new ChangePasswordRequest("currentPass", "NewP@ssword1", "NewP@ssword1");

        when(passwordService.validatePassword(eq("currentPass"), anyString())).thenReturn(true);
        PasswordValidationResult validResult = new PasswordValidationResult();
        when(passwordService.validatePasswordPolicy(req.getNewPassword())).thenReturn(validResult);
        when(passwordService.canReusePassword(userId, req.getNewPassword())).thenReturn(true);
        when(passwordService.hashPassword(req.getNewPassword())).thenReturn("hashedNew");

        // Act
        authenticationService.changePassword(userId, req);

        // Assert
        verify(userAccountRepository).save(any(UserAccount.class));
        verify(passwordService).savePasswordHistory(eq(userId), eq("hashedNew"));
        verify(sessionService).terminateUserSessions(eq(userId));
    }

    @Test
    void testLogin_enforcesSessionLimit() {
        // Arrange
        LoginRequest req = new LoginRequest("limittest@example.com", "pw", false);

        UserAccount user = new UserAccount();
        user.setId(4L);
        user.setEmail("limittest@example.com");
        Tenant tenant = new Tenant(); tenant.setId(400L); user.setTenant(tenant);
        user.setRoles(new HashSet<>());

        UserPrincipal principal = UserPrincipal.create(user);

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(principal);

        when(jwtTokenProvider.generateAccessToken(eq(principal))).thenReturn("atoken");
        when(jwtTokenProvider.generateRefreshToken(eq(principal))).thenReturn("rtoken");

        when(sessionService.isUserSessionLimitReached(user.getId())).thenReturn(true);
        when(sessionService.createSession(eq(user.getId()), eq(user.getTenant().getId()), anyString(), anyString()))
                .thenReturn(new UserSession() {{ setId("s-lim-1"); }});

        when(userAccountRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // Act
        LoginResponse resp = authenticationService.login(new LoginRequest("limittest@example.com", "pw", false), "127.0.0.1", "JUnit");

        // Assert
        assertNotNull(resp);
        verify(sessionService).enforceSessionLimit(eq(user.getId()));
        verify(sessionService).createSession(eq(user.getId()), eq(user.getTenant().getId()), anyString(), anyString());
    }

    @Test
    void testSuccessfulLogin_shouldReturnJwtTokenAndCreateSession() {
    // Arrange
    LoginRequest req = new LoginRequest("admin@demo.example", "password123", false);

    // create a user and corresponding UserPrincipal
    UserAccount user = new UserAccount();
    user.setId(1L);
    user.setEmail("admin@demo.example");
    Tenant tenant = new Tenant();
    tenant.setId(100L);
    user.setTenant(tenant);
    // ensure roles is non-null for UserPrincipal.create
    user.setRoles(Collections.emptySet());

    UserPrincipal userPrincipal = UserPrincipal.create(user);

    Authentication auth = mock(Authentication.class);
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(auth);
    when(auth.getPrincipal()).thenReturn(userPrincipal);

    // stub token generation
    String sampleAccessToken = "eyJhbGci...sampleAccess";
    String sampleRefreshToken = "sampleRefreshToken";
    when(jwtTokenProvider.generateAccessToken(eq(userPrincipal))).thenReturn(sampleAccessToken);
    when(jwtTokenProvider.generateRefreshToken(eq(userPrincipal))).thenReturn(sampleRefreshToken);

    // stub session creation to return a session with id
    UserSession session = new UserSession();
    session.setId("session-123");
    when(sessionService.createSession(eq(user.getId()), eq(user.getTenant().getId()), anyString(), anyString()))
        .thenReturn(session);

    // stub repository lookups used in updateUserLoginInfo
    when(userAccountRepository.findById(user.getId())).thenReturn(Optional.of(user));

    // Act
    LoginResponse resp = authenticationService.login(req, "127.0.0.1", "JUnit-test-agent");

    // Assert
    assertNotNull(resp);
    assertEquals(sampleAccessToken, resp.getAccessToken());
    assertEquals(sampleRefreshToken, resp.getRefreshToken());
    assertEquals("session-123", resp.getSessionId());

    // verify session created and login attempt recorded as successful
    verify(sessionService).createSession(eq(user.getId()), eq(user.getTenant().getId()), anyString(), anyString());
    verify(loginAttemptService).recordLoginAttempt(eq("admin@demo.example"), anyString(), anyString(), eq(true), isNull());
    }

    @Test
    void testFailedLoginWithInvalidCredentials_shouldRecordFailedAttemptAndNotReturnToken() {
    // Arrange
    String email = "admin@demo.example";
    LoginRequest req = new LoginRequest(email, "wrong-password", false);

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    // Act & Assert
    BadCredentialsException ex = assertThrows(BadCredentialsException.class,
        () -> authenticationService.login(req, "127.0.0.1", "JUnit-agent"));

    // verify failed attempt recorded and no session created
    verify(loginAttemptService).recordLoginAttempt(eq(email.toLowerCase()), anyString(), anyString(), eq(false), eq("Invalid credentials"));
    verify(sessionService, never()).createSession(anyLong(), anyLong(), anyString(), anyString());
    }

    @Test
    void testAccountLockoutAfterMaxAttempts_shouldPreventLogin() {
    // Arrange
    String email = "admin@demo.example";
    LoginRequest req = new LoginRequest(email, "password123", false);

    when(loginAttemptService.isAccountLocked(email.toLowerCase())).thenReturn(true);
    LocalDateTime expiry = LocalDateTime.now().plusMinutes(30);
    when(loginAttemptService.getLockoutExpiry(email.toLowerCase())).thenReturn(expiry);

    // Act & Assert
    LockedException ex = assertThrows(LockedException.class,
        () -> authenticationService.login(req, "127.0.0.1", "JUnit-agent"));

    // verify that a failed login attempt was recorded with reason "Account locked"
    // AuthenticationService records this twice: once at lock detection and once in the catch block
    verify(loginAttemptService, times(2)).recordLoginAttempt(eq(email.toLowerCase()), anyString(), anyString(), eq(false), eq("Account locked"));
    verify(sessionService, never()).createSession(anyLong(), anyLong(), anyString(), anyString());
    }

    // Add more tests based on the test plan:
    // - testRefreshTokenGeneration
    // - testPasswordChangeWithValidation
    // - testSessionLimitEnforcement
    // - testLogoutSessionInvalidation

}
