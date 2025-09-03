# Technical Stack

_Last updated: 2025-09-03_

This document summarizes the primary technologies and local development tooling for the project.

## Backend
- Java 17+, Spring Boot 3.x, Spring Data JPA, Spring Security
- MySQL 8, Flyway migrations, Redis for caching/session
- JWT for auth, BCrypt for passwords

## Build & Tooling
- Maven 3.8+, JUnit 5, Mockito, Testcontainers
- CI: GitHub Actions (or GitLab CI)

## Dev & Infra
- Docker/Docker Compose for local infra
- Kubernetes + Helm for staging/prod
- Observability: Prometheus, Grafana, Loki, Jaeger

## Frontend (UI)
- React / TypeScript or similar modern SPA stack
- Component library + Storybook, accessibility testing

## Notes
- Prefer JDK 17 for local development; `spring-boot-maven-plugin` pinned in `pom.xml` for consistent behavior.
- Database enum→varchar migrations live in `src/main/resources/db/migration/`.
