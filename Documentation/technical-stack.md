# Technical Stack
<!-- Consolidated technical stack and architecture. This file is the canonical tech-stack doc. -->

# Technical Stack & Architecture

_Last updated: 2025-09-04_

This document is the canonical reference for the project's technology stack, architecture notes, and developer tooling.

## Backend

- Java 17+ (LTS)
- Spring Boot 3.x, Spring Data JPA, Spring Web
- Spring Security 6.x with JWT for stateless auth
- BCrypt for password hashing

## Database

- MySQL 8 (InnoDB), Flyway for migrations
- Redis for caching and session data

## Build & Test Tooling

- Maven 3.8+
- JUnit 5, Mockito, Testcontainers for integration tests
- CI: GitHub Actions (recommended)

## Infrastructure & Observability

- Docker & Docker Compose for local infra
- Kubernetes + Helm for staging/production
- Observability: Prometheus, Grafana, Loki, Jaeger/Tempo

## Frontend

- Recommended: React + TypeScript (or equivalent modern SPA)
- Component library, Storybook for UI development

## Notes

- Use JDK 17 for local development. Pin `spring-boot-maven-plugin` in `pom.xml` for consistent builds.
- Database migrations (including enum→varchar conversions) live in `src/main/resources/db/migration/`.

For fuller architecture details and future microservices plans, see `Documentation/archived/tech-stack-and-architecture.md` which has been consolidated into this single canonical file.
