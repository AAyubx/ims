# Admin User Management Module Design

_Last updated: 2025-09-03_

## Overview

This module handles all administrative user management functions including user creation, role assignment, password management, and account lifecycle operations.

## Core Features

### 1. User Account Management

- **Create User Account**

  - Employee code (unique within tenant)
  - Email address (unique within tenant)
  - Display name
  - Role assignment (dropdown selection)
  - Initial password generation
  - Account status (ACTIVE/INACTIVE)

- **Update User Account**

  - Modify display name
  - Change employee code
  - Update role assignments
  - Change account status
  - Force password reset

- **Deactivate User Account**
  - Soft delete (status = INACTIVE)
  - Maintain audit trail
  - Revoke all active sessions
  - Prevent future logins

### 2. Role Management

- **Role Assignment**

  - Multiple roles per user supported
  - Role-based access control (RBAC)
  - Hierarchical permissions

- **Available Roles**
  - `ADMIN` - Full system access
  - `MANAGER` - Store/Warehouse management
  - `CLERK` - Operations access
  - `VIEWER` - Read-only access

### 3. Password Management

- **Password Policy Enforcement**

  - Minimum 8 characters
  - Must contain: uppercase, lowercase, digit, special character
  - Cannot reuse last 3 passwords
  - 60-day expiry (configurable)

- **Password Reset Options**
  - Admin-initiated password reset
  - Force password change on next login
  - Generate secure temporary passwords
  - Password expiry notifications

### 4. Account Security Features

- **Login Attempt Monitoring**

  - Track failed login attempts
  - Automatic account lockout (5 attempts)
  - 30-minute lockout duration (configurable)
  - Manual unlock by admin

- **Session Management**
  - View active user sessions
  - Force session termination
  - Session timeout configuration
  - Concurrent session limits

## API Endpoints

### User Management

```http
GET    /api/admin/users                    # List all users
POST   /api/admin/users                    # Create new user
GET    /api/admin/users/{id}               # Get user details
PUT    /api/admin/users/{id}               # Update user
DELETE /api/admin/users/{id}               # Deactivate user
POST   /api/admin/users/{id}/reset-password # Reset user password
POST   /api/admin/users/{id}/unlock        # Unlock user account
GET    /api/admin/users/{id}/sessions      # Get user sessions
DELETE /api/admin/users/{id}/sessions/{sessionId} # Terminate session
```

### Role Management

```http
GET    /api/admin/roles                    # List all roles
POST   /api/admin/users/{id}/roles         # Assign role to user
DELETE /api/admin/users/{id}/roles/{roleId} # Remove role from user
```

### Configuration Management

```http
GET    /api/admin/config                   # Get system configuration
PUT    /api/admin/config                   # Update configuration
```

## Data Transfer Objects (DTOs)

### CreateUserRequest

```java
public class CreateUserRequest {
    @NotBlank private String employeeCode;
    @Email private String email;
    @NotBlank private String displayName;
    @NotEmpty private Set<Long> roleIds;
    private String initialPassword; // Optional, will generate if null
}
```

### UserResponse

```java
public class UserResponse {
    private Long id;
    private String employeeCode;
    private String email;
    private String displayName;
    private UserStatus status;
    private Set<RoleDto> roles;
    private int failedLoginAttempts;
    private LocalDateTime accountLockedUntil;
    private LocalDateTime lastLoginAt;
    private LocalDateTime passwordExpiresAt;
    private boolean mustChangePassword;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### UpdateUserRequest

```java
public class UpdateUserRequest {
    private String displayName;
    private String employeeCode;
    private Set<Long> roleIds;
    private UserStatus status;
    private Boolean mustChangePassword;
}
```

## Service Layer Architecture

### AdminUserService

```java
@Service
public class AdminUserService {

    // User CRUD operations
    Page<UserResponse> getAllUsers(Pageable pageable);
    UserResponse getUserById(Long id);
    UserResponse createUser(CreateUserRequest request);
    UserResponse updateUser(Long id, UpdateUserRequest request);
    void deactivateUser(Long id);

    // Password management
    String resetUserPassword(Long id);
    void forcePasswordChange(Long id);
    void unlockUserAccount(Long id);

    // Session management
    List<UserSessionDto> getUserSessions(Long id);
    void terminateUserSession(Long userId, String sessionId);
    void terminateAllUserSessions(Long userId);

    // Role management
    UserResponse assignRoleToUser(Long userId, Long roleId);
    UserResponse removeRoleFromUser(Long userId, Long roleId);
}
```

### PasswordService

```java
@Service
public class PasswordService {

    String generateSecurePassword();
    boolean isPasswordValid(String password);
    boolean canReusePassword(Long userId, String hashedPassword);
    void savePasswordHistory(Long userId, String hashedPassword);
    LocalDateTime calculatePasswordExpiry();

    // Password policy validation
    PasswordValidationResult validatePassword(String password);
    boolean isPasswordExpired(LocalDateTime passwordExpiresAt);
    boolean isPasswordExpiringSoon(LocalDateTime passwordExpiresAt, int warningDays);
}
```

## Security Considerations

### Authorization

- Only ADMIN role can access user management endpoints
- Audit all admin actions
- Multi-factor authentication for admin operations
- Rate limiting on sensitive operations

### Data Protection

- Hash passwords using BCrypt with salt
- Encrypt sensitive data in transit and at rest
- Implement data masking for logs
- GDPR compliance for user data

### Audit Trail

- Log all user management operations
- Track who performed each action
- Maintain immutable audit records
- Regular audit report generation

## Validation Rules

### Employee Code

- Alphanumeric characters only
- 3-32 characters length
- Unique within tenant
- Cannot be changed once set (business rule)

### Email Address

- Valid email format
- Maximum 320 characters
- Unique within tenant
- Case insensitive comparison

### Password Requirements

- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character (!@#$%^&\*)
- Cannot contain username or email

## Error Handling

### Common Error Responses

```http
400 Bad Request - Validation errors
401 Unauthorized - Authentication required
403 Forbidden - Insufficient permissions
404 Not Found - User not found
409 Conflict - Duplicate employee code/email
422 Unprocessable Entity - Business rule violations
429 Too Many Requests - Rate limit exceeded
500 Internal Server Error - System errors
```

### Business Rule Violations

- Cannot deactivate the last admin user
- Cannot remove admin role from yourself
- Cannot unlock account with expired password
- Cannot create user with duplicate employee code

## Configuration Parameters

### System Configuration Keys

```properties
password.expiry.days=60
password.min.length=8
password.require.uppercase=true
password.require.lowercase=true
password.require.digit=true
password.require.special=true
password.history.count=3
login.max.attempts=5
login.lockout.minutes=30
session.timeout.minutes=480
password.reset.token.expiry.hours=24
admin.mfa.required=true
```

## Testing Strategy

### Unit Tests

- Service layer business logic
- Password validation rules
- Role assignment logic
- Security validations

### Integration Tests

- Database operations
- API endpoint tests
- Authentication/authorization
- Audit logging verification

### Security Tests

- SQL injection prevention
- XSS protection
- Authentication bypass attempts
- Authorization boundary tests

## Performance Considerations

### Database Optimization

- Proper indexing on search fields
- Pagination for user lists
- Query optimization for role lookups
- Connection pooling configuration

### Caching Strategy

- Cache user roles and permissions
- Cache system configuration
- Session data caching
- Cache invalidation on updates

## Deployment Considerations

### Configuration Management

- Environment-specific configurations
- Secret management for sensitive data
- Database migration scripts
- Health check endpoints

### Monitoring and Alerting

- Failed login attempt monitoring
- Account lockout alerts
- Password expiry notifications
- System configuration changes

## Future Enhancements

### Advanced Security Features

- Multi-factor authentication (MFA)
- Single sign-on (SSO) integration
- OAuth 2.0 / OpenID Connect support
- Advanced threat detection

### User Experience Improvements

- Self-service password reset
- User profile management
- Email notifications for account changes
- Mobile-friendly admin interface

## Updates (2025-09-03)

Small but important operational updates related to recent schema and runtime changes:

- Several database columns used for account and tenant `status` were converted from MySQL `ENUM` to `VARCHAR` via forward Flyway migrations (V4..V8) so they match JPA `@Enumerated(EnumType.STRING)` mappings used by the application. This affects fields such as `user_account.status` and `tenant.status`. There is no API-level change — only the physical column type changed.
- If you encounter Hibernate validation errors about column types during startup, apply the missing migration(s) and re-run migrations before starting the service.
- Developer runtime: prefer JDK 17 for running the application locally. Ensure IDE and terminal use the same `JAVA_HOME`.

### Compliance Features

- GDPR data export/deletion
- HIPAA compliance auditing
- SOC 2 compliance reporting
- Advanced audit analytics
