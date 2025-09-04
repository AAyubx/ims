# Security and Authentication

_Last updated: 2025-09-03_

This consolidated document describes authentication, password policy, session management, JWT configuration, and security controls used by the Inventory Management System.

## Overview

- Email/password authentication (case-insensitive email)
- JWT-based stateless authentication with refresh tokens
- Password hashing with BCrypt (cost 12)
- Account lockout, rate limiting, and failed-attempt tracking
- Session persistence for revocation and auditing

## Authentication Flow

1. Client POSTs credentials to `/api/auth/login`.
2. Service validates user status and BCrypt password hash.
3. On success, a JWT access token and refresh token are issued and a session record is created.
4. Protected endpoints validate JWT signature + session state (cached in Redis when appropriate).

## Token Configuration (example)

```
app.jwt.secret=${JWT_SECRET}
app.jwt.access-expiry-seconds=28800 # 8 hours
app.jwt.refresh-expiry-seconds=604800 # 7 days
```

## Password Policy

- Minimum length: 8
- Require uppercase, lowercase, digits, special characters
- History: avoid reuse of last 3 passwords
- Expiry: 60 days by default
- Reset tokens expire in 24 hours

## APIs

```
POST   /api/auth/login
POST   /api/auth/logout
POST   /api/auth/refresh
GET    /api/auth/me
POST   /api/auth/forgot-password
POST   /api/auth/reset-password
GET    /api/auth/password-policy
```

## Security Configuration Notes

- Use `BCryptPasswordEncoder` with strength 12.
- Ensure `SecurityConfig` request matchers account for servlet context path (e.g., `/api`).
- Permit unauthenticated access to login and forgot-password endpoints only.

## Session & Revocation

- Store session records in `user_session` table with sessionId, userId, ttl and lastActivity.
- Cache permissions and session validity in Redis for fast checks; fallback to DB on cache miss.
- Administrative session termination APIs revoke both cache and DB records.

## Logging & Auditing

- Log all failed login attempts and successful logins (include ip, user-agent)
- Log password resets and admin-initiated password changes
- Maintain immutable audit log with timestamps and actor id

## Operational Tips

- When updating password_hash via SQL, use binary literal to preserve the bcrypt string: `UPDATE user_account SET password_hash = _binary'$2b$...'`.
- If Hibernate complains about enum vs varchar column types, ensure Flyway migrations converting enums to varchar are applied prior to startup.

### Dev mail and actuator notes

- For local development we use a test SMTP (MailHog) on `localhost:1025` and the MailHog UI on `http://localhost:8025` so password-reset emails and other notifications can be inspected.
- The `dev` profile disables SMTP auth/starttls by default and the mail health indicator can be disabled so the actuator health endpoint does not report DOWN simply because no SMTP credentials are set on a developer machine.
- After editing `application.yml` the Spring Boot application must be restarted for changes (mail settings or management.health.mail.enabled) to take effect.

## Testing

- Unit tests for token generation/validation, password policy enforcement, and login flows
- Integration tests using Testcontainers for MySQL and Redis
