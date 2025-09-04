# Service Testing Documentation

This document provides testing instructions for all services in the `com.inventory.service` package.

## Testing Framework Setup

The project uses the following testing technologies:
- **JUnit 5** (Jupiter) - Unit testing framework
- **Spring Boot Test** - Integration testing support
- **Spring Security Test** - Security context testing
- **Testcontainers** - Database integration testing
- **H2 Database** - In-memory database for unit tests
- **Mockito** - Mocking framework
- **JaCoCo** - Code coverage analysis

### Test Execution Commands

```bash
# Run all tests
mvn test

# Run tests with coverage report
mvn test jacoco:report

# Run only unit tests (exclude integration tests)
mvn test -Dtest="**/*Test"

# Run only integration tests
mvn test -Dtest="**/*IT"
```

---

## AuthenticationService Tests

**Summary:** Tests authentication flows, JWT token operations, login security, and password management.

### Key Test Scenarios:
• Successful user login with valid credentials
• Failed login attempts and account lockout mechanisms
• JWT token generation and validation
• Refresh token functionality
• Password change with validation
• Session management and limits enforcement
• Multi-factor authentication flows (if implemented)

### Test Setup Instructions:

1. **Create test file:** `src/test/java/com/inventory/service/AuthenticationServiceTest.java`

2. **Required test annotations:**
   ```java
   @ExtendWith(MockitoExtension.class)
   @MockBean({AuthenticationManager.class, JwtTokenProvider.class, 
             SessionService.class, LoginAttemptService.class})
   ```

3. **Test data preparation:**
   ```java
   @Mock private UserAccountRepository userAccountRepository;
   private LoginRequest validLoginRequest = new LoginRequest("test@example.com", "password123");
   private UserPrincipal mockUserPrincipal = createMockUserPrincipal();
   ```

4. **Critical test methods to implement:**
   - `testSuccessfulLogin()` - Verify complete login flow
   - `testFailedLoginWithInvalidCredentials()` - Test authentication failure
   - `testAccountLockoutAfterMaxAttempts()` - Verify lockout mechanism
   - `testRefreshTokenGeneration()` - Test token refresh flow
   - `testPasswordChangeWithValidation()` - Test password update
   - `testSessionLimitEnforcement()` - Verify session limits
   - `testLogoutSessionInvalidation()` - Test session cleanup

5. **Security context setup:**
   ```java
   @WithMockUser(roles = "USER")
   SecurityContext mockSecurityContext = mock(SecurityContext.class);
   ```

---

## AdminUserService Tests

**Summary:** Tests administrative user management operations with proper authorization and tenant isolation.

### Key Test Scenarios:
• User creation with role assignments
• User profile updates and status changes
• Account deactivation and reactivation
• Password reset functionality
• Role-based access control validation
• Tenant isolation enforcement
• Admin permission validations

### Test Setup Instructions:

1. **Create test file:** `src/test/java/com/inventory/service/AdminUserServiceTest.java`

2. **Required test annotations:**
   ```java
   @ExtendWith(MockitoExtension.class)
   @WithMockUser(roles = "ADMIN")
   ```

3. **Mock dependencies:**
   ```java
   @Mock private UserAccountRepository userAccountRepository;
   @Mock private RoleRepository roleRepository;
   @Mock private TenantRepository tenantRepository;
   @Mock private PasswordService passwordService;
   ```

4. **Test data preparation:**
   ```java
   private CreateUserRequest createUserRequest = new CreateUserRequest();
   private UpdateUserRequest updateUserRequest = new UpdateUserRequest();
   private UserAccount mockUser = createMockUserAccount();
   ```

5. **Essential test methods:**
   - `testCreateUserWithValidData()` - Verify user creation
   - `testCreateUserDuplicateEmail()` - Test unique constraint
   - `testUpdateUserDisplayName()` - Test profile updates
   - `testDeactivateUser()` - Test account deactivation
   - `testResetUserPassword()` - Test password reset
   - `testUnlockUserAccount()` - Test account unlock
   - `testTenantIsolation()` - Verify tenant boundaries
   - `testInsufficientPermissions()` - Test authorization

6. **Security context configuration:**
   ```java
   UserPrincipal adminPrincipal = createMockAdminPrincipal();
   SecurityContextHolder.setContext(mockSecurityContext);
   ```

---

## PasswordService Tests

**Summary:** Tests password security operations including hashing, validation, policy enforcement, and history management.

### Key Test Scenarios:
• Password hashing and validation
• Password policy compliance checking
• Password strength calculation
• Password reuse prevention
• Secure password generation
• Password expiry management
• Reset token generation

### Test Setup Instructions:

1. **Create test file:** `src/test/java/com/inventory/service/PasswordServiceTest.java`

2. **Required test annotations:**
   ```java
   @ExtendWith(MockitoExtension.class)
   @TestPropertySource(properties = {"spring.profiles.active=test"})
   ```

3. **Mock dependencies:**
   ```java
   @Mock private PasswordEncoder passwordEncoder;
   @Mock private UserPasswordHistoryRepository passwordHistoryRepository;
   @Mock private SystemConfigRepository systemConfigRepository;
   ```

4. **Test data preparation:**
   ```java
   private final String VALID_PASSWORD = "StrongPass123!";
   private final String WEAK_PASSWORD = "weak";
   private final Long TEST_USER_ID = 1L;
   ```

5. **Core test methods:**
   - `testPasswordHashing()` - Verify hashing functionality
   - `testPasswordValidation()` - Test password verification
   - `testPasswordPolicyValidation()` - Test policy enforcement
   - `testPasswordStrengthCalculation()` - Verify strength scoring
   - `testPasswordReuseCheck()` - Test history validation
   - `testSecurePasswordGeneration()` - Test password generation
   - `testPasswordExpiry()` - Test expiration logic
   - `testResetTokenGeneration()` - Test token creation

6. **Configuration mocking:**
   ```java
   when(systemConfigRepository.findByConfigKey(anyString()))
       .thenReturn(Optional.of(mockSystemConfig()));
   ```

---

## SessionService Tests

**Summary:** Tests user session lifecycle management, security, and cleanup operations.

### Key Test Scenarios:
• Session creation and validation
• Session timeout handling
• Session activity tracking
• Multi-session management
• Session cleanup and expiry
• Session limit enforcement
• Concurrent session handling

### Test Setup Instructions:

1. **Create test file:** `src/test/java/com/inventory/service/SessionServiceTest.java`

2. **Required test annotations:**
   ```java
   @ExtendWith(MockitoExtension.class)
   @DataJpaTest
   @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
   ```

3. **Mock dependencies:**
   ```java
   @Mock private UserSessionRepository sessionRepository;
   @Mock private SystemConfigRepository systemConfigRepository;
   ```

4. **Test data preparation:**
   ```java
   private final Long TEST_USER_ID = 1L;
   private final Long TEST_TENANT_ID = 1L;
   private final String TEST_IP = "192.168.1.1";
   private final String TEST_USER_AGENT = "Test-Agent/1.0";
   ```

5. **Important test methods:**
   - `testCreateSession()` - Verify session creation
   - `testUpdateSessionActivity()` - Test activity tracking
   - `testInvalidateSession()` - Test session termination
   - `testSessionValidation()` - Test validity checking
   - `testSessionTimeout()` - Test timeout handling
   - `testSessionLimitEnforcement()` - Test concurrent limits
   - `testCleanupExpiredSessions()` - Test cleanup process
   - `testGetActiveUserSessions()` - Test session retrieval

6. **Time-based testing:**
   ```java
   @MockBean private Clock clock;
   LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0);
   when(clock.now()).thenReturn(fixedTime);
   ```

---

## LoginAttemptService Tests

**Summary:** Tests login security monitoring, brute force protection, and suspicious activity detection.

### Key Test Scenarios:
• Login attempt recording and tracking
• Failed attempt counting and limits
• Account lockout mechanisms
• Suspicious activity detection
• Account unlock procedures
• Attempt history management
• IP-based monitoring

### Test Setup Instructions:

1. **Create test file:** `src/test/java/com/inventory/service/LoginAttemptServiceTest.java`

2. **Required test annotations:**
   ```java
   @ExtendWith(MockitoExtension.class)
   @TestMethodOrder(OrderAnnotation.class)
   ```

3. **Mock dependencies:**
   ```java
   @Mock private LoginAttemptRepository loginAttemptRepository;
   @Mock private UserAccountRepository userAccountRepository;
   @Mock private SystemConfigRepository systemConfigRepository;
   ```

4. **Test data preparation:**
   ```java
   private final String TEST_EMAIL = "test@example.com";
   private final String TEST_IP = "192.168.1.100";
   private final String TEST_USER_AGENT = "Test-Browser/1.0";
   ```

5. **Essential test methods:**
   - `testRecordSuccessfulLoginAttempt()` - Test success logging
   - `testRecordFailedLoginAttempt()` - Test failure logging
   - `testFailedAttemptCounting()` - Test attempt counting
   - `testAccountLockoutAfterMaxAttempts()` - Test lockout logic
   - `testIsAccountLocked()` - Test lockout status check
   - `testUnlockAccount()` - Test unlock functionality
   - `testSuspiciousActivityDetection()` - Test threat detection
   - `testCleanupOldAttempts()` - Test data cleanup

6. **Configuration setup:**
   ```java
   SystemConfig maxAttemptsConfig = createConfigMock("LOGIN_MAX_ATTEMPTS", "5");
   SystemConfig lockoutConfig = createConfigMock("LOGIN_LOCKOUT_MINUTES", "30");
   ```

---

## AuditService Tests

**Summary:** Tests comprehensive audit logging, data tracking, and compliance reporting functionality.

### Key Test Scenarios:
• Action logging and audit trail creation
• User activity tracking
• Admin action monitoring
• Login attempt auditing
• Data change tracking
• Audit log cleanup
• Compliance reporting

### Test Setup Instructions:

1. **Create test file:** `src/test/java/com/inventory/service/AuditServiceTest.java`

2. **Required test annotations:**
   ```java
   @ExtendWith(MockitoExtension.class)
   @EnableAsync
   @TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
   ```

3. **Mock dependencies:**
   ```java
   @Mock private AuditLogRepository auditLogRepository;
   @Mock private ObjectMapper objectMapper;
   @MockBean private AsyncTaskExecutor taskExecutor;
   ```

4. **Test data preparation:**
   ```java
   private final Long TEST_USER_ID = 1L;
   private final String TEST_ENTITY_TYPE = "USER_ACCOUNT";
   private final String TEST_ENTITY_ID = "123";
   private final AuditLog.ActionType TEST_ACTION = AuditLog.ActionType.UPDATE;
   ```

5. **Key test methods:**
   - `testLogAction()` - Test basic action logging
   - `testLogUserAction()` - Test user-specific logging
   - `testLogLoginAttempt()` - Test login audit
   - `testLogPasswordChange()` - Test password change audit
   - `testLogAdminAction()` - Test admin action logging
   - `testAsyncAuditLogging()` - Test asynchronous processing
   - `testAuditLogCleanup()` - Test old log cleanup
   - `testRequestContextCapture()` - Test context extraction

6. **Async testing setup:**
   ```java
   @Test
   @Timeout(5)
   void testAsyncAuditLogging() throws InterruptedException {
       CountDownLatch latch = new CountDownLatch(1);
       // Test async execution
   }
   ```

7. **Security context mocking:**
   ```java
   @WithMockUser(username = "test@example.com")
   MockHttpServletRequest request = new MockHttpServletRequest();
   RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
   ```

---

## Integration Testing Guidelines

### Database Integration Tests

1. **Use Testcontainers for MySQL integration:**
   ```java
   @Testcontainers
   @SpringBootTest
   class ServiceIntegrationTest {
       @Container
       static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");
   }
   ```

2. **Test transaction rollback:**
   ```java
   @Transactional
   @Rollback
   @Test
   void testTransactionalBehavior() { }
   ```

### Security Integration Tests

1. **Test with Spring Security:**
   ```java
   @WithMockUser(roles = {"ADMIN", "USER"})
   @Test
   void testWithSecurityContext() { }
   ```

2. **Test authorization:**
   ```java
   @Test
   @WithAnonymousUser
   @ExpectedExeption(AccessDeniedException.class)
   void testUnauthorizedAccess() { }
   ```

### Performance Testing

1. **Load testing for session management:**
   ```java
   @RepeatedTest(100)
   @Execution(ExecutionMode.CONCURRENT)
   void testConcurrentSessionCreation() { }
   ```

2. **Memory testing for audit logs:**
   ```java
   @Test
   @EnabledOnOs(OS.LINUX)
   void testMemoryUsageUnderLoad() { }
   ```

## Test Coverage Goals

- **Minimum coverage:** 80% line coverage
- **Critical paths:** 95% coverage for security-related code
- **Integration tests:** Cover all service interactions
- **Edge cases:** Test boundary conditions and error scenarios

## Running Tests in CI/CD

```yaml
# Maven commands for CI pipeline
test:
  script:
    - mvn clean compile
    - mvn test -Dtest="**/*Test"
    - mvn test -Dtest="**/*IT" -Dspring.profiles.active=test
    - mvn jacoco:report
```