````markdown
# Technical Stack
_Last updated: 2025-09-03_

## Backend
- Java 17+
- Spring Boot 3.x
- Spring Security 6.x
- Spring Data JPA

## Database & Migrations
- MySQL 8.0+
- Flyway for schema migrations (migrations in `src/main/resources/db/migration`)
- Redis for cache/session

## Build & Dev Tools
- Maven 3.8+
- Docker & Docker Compose
- Testcontainers for integration tests

## Security
- JWT for stateless auth
- BCrypt for password hashing

## Frontend & UI
- Frontend frameworks documented separately; UI tech stack includes React/Vite or Angular where applicable

## Notes
- Ensure `JAVA_HOME` points to JDK 17 in dev environments
- Several Flyway forward migrations (V4..V8) were added to convert ENUM→VARCHAR to match JPA enum mappings

````
