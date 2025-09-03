# Admin User Management

_Last updated: 2025-09-03_

This document consolidates the admin user management design and operational considerations for the Inventory Management System. It covers user lifecycle, role management, password and session policies, APIs, and audit requirements.

## Core Capabilities

- Create, update, deactivate/reactivate user accounts
- Role assignment and RBAC
- Password policy, password history, and reset flows
- Failed login tracking and account lockout
- Session management and session termination
- Audit logging for all admin actions

## User Account Model (high level)

- employeeCode: unique within tenant
- email: unique within tenant, case-insensitive
- displayName
- roles: set of role ids/names
- status: ACTIVE / INACTIVE
- passwordHash: BCrypt stored (VARBINARY preserving ASCII hash)

## Password & Security Policies

- Minimum 8 characters, require uppercase/lowercase/digit/special
- Cannot reuse last 3 passwords
- Password expiry: default 60 days (configurable)
- Account lockout after 5 failed attempts; configurable lockout duration (default 30 minutes)
- Force change on next login support

## Session & Token Management

- JWT access tokens with refresh token flow
- Session records persisted for revocation and monitoring
- Concurrent session limits and manual session termination APIs

## API (admin)

```
GET    /api/admin/users
POST   /api/admin/users
GET    /api/admin/users/{id}
PUT    /api/admin/users/{id}
DELETE /api/admin/users/{id}
POST   /api/admin/users/{id}/reset-password
POST   /api/admin/users/{id}/unlock
GET    /api/admin/users/{id}/sessions
DELETE /api/admin/users/{id}/sessions/{sessionId}
```

## Auditing & Compliance

- Audit log for user creation, role changes, password resets, unlocks, and session terminations
- Immutable audit records, retention policy configurable
- GDPR/PII considerations: export and deletion endpoints

## Operational Notes

- When applying schema migrations that change enum types to varchar, ensure Flyway forward migrations are used and tested in staging first.
- For DB updates to password_hash, write as binary literal to preserve leading `$` for bcrypt when using SQL: `UPDATE user_account SET password_hash = _binary'$2b$...'`.

## Configuration Keys

```
password.expiry.days=60
password.min.length=8
password.history.count=3
login.max.attempts=5
login.lockout.minutes=30
session.timeout.minutes=480
```

## Testing

- Unit tests for policy validators and service logic
- Integration tests for login, reset flows, and session revocation

## References

- See `security-and-auth.md` for detailed authentication and JWT configuration.
