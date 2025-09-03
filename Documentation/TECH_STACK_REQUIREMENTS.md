# Tech Stack Requirements - Inventory Management System

_Last updated: 2025-09-03_

## Overview

This document outlines the technical stack required to set up and run the Inventory Management System locally for development and testing.

## Backend Stack

### Core Framework

- **Java 17+** - Primary programming language
- **Spring Boot 3.x** - Main application framework
- **Spring Security 6.x** - Authentication and authorization
- **Spring Data JPA** - Data access layer
- **Spring Web** - REST API development

### Database

- **MySQL 8.0+** - Primary database
- **Flyway** - Database migration tool
- **Redis 6.x** - Caching and session management

### Security & Authentication

- **Spring Security** - Core security framework
- **JWT (JSON Web Tokens)** - Stateless authentication
- **BCrypt** - Password hashing
- **OWASP Java Encoder** - Input sanitization

### API & Documentation

- **OpenAPI 3** (Swagger) - API documentation
- **Spring Boot Actuator** - Health checks and monitoring

### Testing

- **JUnit 5** - Unit testing framework
- **Mockito** - Mocking framework
- **Testcontainers** - Integration testing with database
- **Spring Boot Test** - Integration testing

### Build & Development Tools

- **Maven 3.8+** - Build and dependency management
- **Docker & Docker Compose** - Containerization
- **Git** - Version control

## Local Development Setup Requirements

### Prerequisites

1. **Java Development Kit (JDK) 17+**

   - Oracle JDK or OpenJDK
   - Verify: `java -version`

2. **Maven 3.8+**

   - For dependency management and build
   - Verify: `mvn -version`

3. **Docker Desktop**

   - For running MySQL and Redis locally
   - Verify: `docker --version`

4. **Git**

   - Version control
   - Verify: `git --version`

5. **IDE (Recommended)**
   - IntelliJ IDEA Community/Ultimate
   - VS Code with Java Extension Pack
   - Eclipse IDE for Java Developers

### Optional Tools

- **Postman** or **Insomnia** - API testing
- **MySQL Workbench** - Database GUI
- **Redis Desktop Manager** - Redis GUI

## Local Environment Setup

### 1. Database Setup (Docker)

```bash
# Create docker-compose.yml for local development
docker-compose up -d mysql redis
```

### 2. Application Properties

```properties
# application-local.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/inventory_saas
    username: inventory_user
    password: inventory_pass
  redis:
    host: localhost
    port: 6379
```

### 3. Build and Run

```bash
# Build the application
mvn clean compile

# Run database migrations
mvn flyway:migrate

# Start the application
mvn spring-boot:run
```

## Security Configuration Notes

### Password Policy

- Minimum 8 characters
- Must contain: uppercase, lowercase, digit, special character
- Cannot reuse last 3 passwords
- Password expiry: 60 days (configurable)

### Authentication Features

- JWT token-based authentication
- Login attempt limiting (5 attempts)
- Account lockout mechanism
- Password reset functionality

### Audit Trail

- All user actions logged
- Database change tracking
- Failed authentication logging
- Admin action auditing

## Development Guidelines

### Code Quality

- **SonarQube** - Code quality analysis
- **SpotBugs** - Bug detection
- **Checkstyle** - Code style enforcement

### Documentation

- JavaDoc for all public methods
- API documentation with OpenAPI
- Database schema documentation

## Production Considerations

### Future Scalability

- Kubernetes deployment ready
- Microservices architecture prepared
- Message queue integration (Kafka/RabbitMQ)
- Distributed caching (Redis Cluster)

### Monitoring & Observability

- Prometheus metrics
- Grafana dashboards
- Centralized logging (ELK stack)
- Distributed tracing (Jaeger)

## Getting Started Checklist

- [ ] Install Java 17+
- [ ] Install Maven 3.8+
- [ ] Install Docker Desktop
- [ ] Clone repository
- [ ] Run `docker-compose up -d`
- [ ] Run `mvn clean compile`
- [ ] Run `mvn flyway:migrate`
- [ ] Run `mvn spring-boot:run`
- [ ] Access application at `http://localhost:8080`
- [ ] Access Swagger UI at `http://localhost:8080/swagger-ui.html`

## GitHub Integration

### Repository Setup

- Repository: `inventory-management-system`
- Branch protection rules for `main`
- Pull request reviews required
- CI/CD pipeline with GitHub Actions

### Commit Convention

- Conventional Commits format
- Signed commits preferred
- Meaningful commit messages

## Updates (2025-09-03)

These are important recent changes to know about when developing and running the project:

- JDK/runtime: The project is run and validated with JDK 17 in developer environments. Make sure `JAVA_HOME` points to a JDK 17 installation before running Maven or Spring Boot. VS Code is configured to use `${env:JAVA_HOME}` for consistency.
- Database migrations: Several forward Flyway migrations (V4 through V8) were added to convert MySQL `ENUM` columns to `VARCHAR` so they match JPA fields annotated with `@Enumerated(EnumType.STRING)` and to satisfy Hibernate schema validation. Migrations are under `src/main/resources/db/migration/`.
- Migration strategy: For live databases, avoid editing already-applied migrations. Instead add new forward migrations (e.g., `V9__...sql`) that alter column types or apply fixes.
- Build plugin pin: `spring-boot-maven-plugin` was explicitly pinned in `pom.xml` to ensure consistent plugin behavior with `mvn spring-boot:run`.
- Code changes: Some JPQL queries and tests were updated to use Spring Data `Pageable` rather than `LIMIT` so they work with JPA and across dialects.
