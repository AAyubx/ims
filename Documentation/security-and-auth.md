````markdown
# Security and Authentication
_Last updated: 2025-09-03_

This document consolidates authentication, admin user management, session handling, and security controls for the Inventory Management System.

## Overview
This covers user login, password policies, session management, admin user lifecycle, RBAC, and auditing.

## Core Authentication Features
- Email-based authentication (case-insensitive)
- JWT token-based authentication (access + refresh tokens)
- Login attempts tracking and account lockout
- Password policies: min length, complexity, expiry, history
- Password reset via secure tokens
- Session management and concurrent session limits

## Admin User Management
- Create/update/deactivate admin accounts
- Role assignment and hierarchical permissions
- Force password resets, unlock, and session termination
- Audit trail for all admin actions

## APIs (Representative)
- POST /api/auth/login
- POST /api/auth/forgot-password
- POST /api/auth/reset-password
- GET /api/auth/sessions
- POST /api/admin/users
- PUT /api/admin/users/{id}
- DELETE /api/admin/users/{id}

## Password Policy
- Minimum 8 characters, must contain uppercase, lowercase, digit, special
- Cannot reuse the last 3 passwords
- Password expiry: 60 days (configurable)

## Session & Token Management
- JWT access tokens: default 8 hours
- Refresh tokens: 7 days
- Token validation: signature, expiry, session presence (DB/cache)

## Security Implementation Notes
- Passwords hashed with BCrypt (strength 12)
- Authentication uses DaoAuthenticationProvider + BCryptPasswordEncoder
- Audit logs retained for admin actions and security events

## Operational Updates (2025-09-03)
- ENUM→VARCHAR forward Flyway migrations (V4..V8) applied; ensure migrations are run before service startup to avoid Hibernate validation errors.
- Developer runtime: prefer JDK 17 and ensure `JAVA_HOME` is set consistently.

## Testing & Monitoring
- Unit tests for password validation and auth flows
- Integration tests with Testcontainers for DB/Redis
- Monitor failed login attempts and account lockouts; alert on anomalies

````
