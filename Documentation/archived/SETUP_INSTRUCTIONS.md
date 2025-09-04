# Setup Instructions - Inventory Management System

_Last updated: 2025-09-03_

## Overview

This document provides step-by-step instructions to set up and run the Inventory Management System locally on your development machine.

## Prerequisites

Before you begin, ensure you have the following installed:

### Required Software

1. **Java 17+** (Oracle JDK or OpenJDK)

   ```bash
   # Check Java version
   java -version
   ```

2. **Maven 3.8+**

   ```bash
   # Check Maven version
   mvn -version
   ```

3. **Docker Desktop**

   ```bash
   # Check Docker version
   docker --version
   docker-compose --version
   ```

4. **Git**
   ```bash
   # Check Git version
   git --version
   ```

## Step-by-Step Setup

### Step 1: Clone the Repository

```bash
# Clone the repository
git clone <your-repo-url> inventory-management-system
cd inventory-management-system

# Or if you're starting from existing files
cd "/Users/ayubahmed/Documents/Programming/Java/Inventory Management System"
```

### Step 2: Start Database Services

```bash
# Start MySQL and Redis using Docker Compose
docker-compose up -d mysql redis

# Wait for services to be ready (check logs)
docker-compose logs -f mysql

# You should see "MySQL init process done. Ready for start up."
```

### Step 3: Verify Database Connection

```bash
# Option 1: Connect via MySQL command line
mysql -h 127.0.0.1 -P 3306 -u inventory_user -p
# Password: inventory_pass

# Option 2: Use phpMyAdmin (optional)
# Navigate to http://localhost:8080 in your browser
# Username: inventory_user
# Password: inventory_pass
```

### Step 4: Build the Application

```bash
# Clean and compile the project
mvn clean compile

# Run tests (optional)
mvn test

# Package the application
mvn package -DskipTests
```

### Step 5: Run Database Migrations

```bash
# Run Flyway migrations to set up the database schema
mvn flyway:migrate

# Verify migrations were applied successfully
mvn flyway:info
```

### Step 6: Start the Application

```bash
# Start the Spring Boot application
mvn spring-boot:run

# Or run the JAR file directly
java -jar target/inventory-management-system-1.0.0-SNAPSHOT.jar
```

### Step 7: Verify Application is Running

1. **Health Check**

   ```bash
   curl http://localhost:8080/api/actuator/health
   ```

2. **API Documentation**

   - Navigate to: http://localhost:8080/api/swagger-ui.html
   - API Docs: http://localhost:8080/api/api-docs

3. **Database Management Tools**
   - phpMyAdmin: http://localhost:8080
   - Redis Commander: http://localhost:8081

## Default User Accounts

After running the migrations, you'll have these default users:

| Email                | Password                 | Role    | Employee Code |
| -------------------- | ------------------------ | ------- | ------------- |
| admin@demo.example   | (Set via password reset) | ADMIN   | EMP001        |
| manager@demo.example | (Set via password reset) | MANAGER | EMP002        |

⚠️ **Important**: Default users don't have passwords set. Use the password reset functionality to set initial passwords.

## Environment Configuration

### Development Environment

The application runs with the `dev` profile by default. Configuration is in `application.yml`:

```yaml
# Database connection
spring.datasource.url: jdbc:mysql://localhost:3306/inventory_saas
spring.datasource.username: inventory_user
spring.datasource.password: inventory_pass

# Redis connection
spring.data.redis.host: localhost
spring.data.redis.port: 6379
```

### Environment Variables (Optional)

You can override configuration using environment variables:

```bash
# JWT Secret (recommended for production)
export JWT_SECRET=your-256-bit-secret-key

# Database credentials
export DB_PASSWORD=your-secure-password

# Mail configuration (for password reset emails)
export MAIL_USERNAME=your-smtp-username
export MAIL_PASSWORD=your-smtp-password
```

## Testing the API

### 1. Get API Token (Login)

First, you need to set a password for a user. Since we don't have authentication working yet, you can directly update the database:

```sql
-- Connect to MySQL and run this to set a temporary password
USE inventory_saas;
UPDATE user_account
SET password_hash = '$2a$12$LQv3c1yqBw3hjKQV3d5gOuP.1Dg/QqvGwm1QhOj8Kj2.mZuOj8Kj2'
WHERE email = 'admin@demo.example';
-- This sets password to: TempPassword123!
```

### 2. Test Authentication Endpoints

```bash
# Login request
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@demo.example",
    "password": "TempPassword123!"
  }'

# Expected response includes JWT token
```

### 3. Test Protected Endpoints

```bash
# Use the JWT token from login response
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

## Troubleshooting

### Common Issues

#### 1. MySQL Connection Failed

```bash
# Check if MySQL is running
docker-compose ps mysql

# Check MySQL logs
docker-compose logs mysql

# Restart MySQL
docker-compose restart mysql
```

#### 2. Port Already in Use

```bash
# Check what's using port 3306
lsof -i :3306

# Stop the conflicting service or change ports in docker-compose.yml
```

#### 3. Flyway Migration Errors

```bash
# Check migration status
mvn flyway:info

# Repair failed migrations
mvn flyway:repair

# Clean and re-run (⚠️ DESTRUCTIVE - only for development)
mvn flyway:clean flyway:migrate
```

#### 4. Application Won't Start

```bash
# Check application logs
tail -f logs/inventory-management.log

# Common issues:
# - Port 8080 already in use
# - Database connection failed
# - Missing environment variables
```

#### 5. Redis Connection Issues

```bash
# Check Redis status
docker-compose ps redis

# Test Redis connection
docker exec -it inventory-redis redis-cli ping
```

## Development Workflow

### 1. Making Database Changes

```bash
# Create new migration file
# src/main/resources/db/migration/V4__your_change_description.sql

# Apply migrations
mvn flyway:migrate
```

### 2. Running Tests

```bash
# Unit tests
mvn test

# Integration tests
mvn test -Dtest=**/*IntegrationTest

# With coverage report
mvn clean test jacoco:report
```

### 3. Code Quality Checks

```bash
# Compile and check for issues
mvn clean compile

# Run Checkstyle (if configured)
mvn checkstyle:check
```

## Production Deployment Notes

### Security Considerations

1. Change default JWT secret
2. Use strong database passwords
3. Enable SSL/TLS
4. Set up proper firewall rules
5. Configure log rotation
6. Set up monitoring and alerts

### Configuration Changes

```yaml
# application-prod.yml
spring:
  profiles:
    active: prod
  security:
    require-ssl: true
logging:
  level:
    com.inventory: INFO
```

## Next Steps

1. **Set Initial Passwords**: Use the password reset functionality to set passwords for default users
2. **Create Additional Users**: Use the admin interface to create more users
3. **Configure Email**: Set up SMTP settings for password reset emails
4. **Customize Configuration**: Adjust password policies and security settings
5. **Implement Frontend**: Connect your frontend application to the API

## Getting Help

### Useful Commands

```bash
# View running containers
docker-compose ps

# Stop all services
docker-compose down

# View application logs
tail -f logs/inventory-management.log

# Database console access
mysql -h localhost -P 3306 -u inventory_user -p

# Redis console access
docker exec -it inventory-redis redis-cli
```

### Log Files

- Application logs: `logs/inventory-management.log`
- Docker logs: `docker-compose logs [service]`

### Monitoring URLs

- Health check: http://localhost:8080/api/actuator/health
- Metrics: http://localhost:8080/api/actuator/metrics
- API docs: http://localhost:8080/api/swagger-ui.html

---

✅ **Verification Checklist**

After completing the setup, verify these items:

- [ ] Docker containers are running (MySQL, Redis)
- [ ] Database connection is working
- [ ] Flyway migrations completed successfully
- [ ] Spring Boot application starts without errors
- [ ] Health check endpoint returns UP status
- [ ] Swagger UI is accessible
- [ ] Can create and authenticate users
- [ ] API endpoints respond correctly

**Congratulations!** Your Inventory Management System is now ready for development and testing.

## Updates (2025-09-03)

Note: The project received several runtime and schema updates to align Flyway migrations, Hibernate/JPA mappings, and the developer runtime.

- Flyway forward migrations V4..V8 were added to convert several existing MySQL ENUM columns to VARCHAR (enum -> VARCHAR) so they match JPA fields annotated with `@Enumerated(EnumType.STRING`). These migrations live under `src/main/resources/db/migration/` and were applied to the development database. Do not edit already-applied migrations; add forward migrations for changes to live databases.
- Runtime Java: When running Maven or Spring Boot locally, set `JAVA_HOME` to a JDK 17 installation. Example:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

- VS Code: The workspace was updated to prefer `JAVA_HOME` for the IDE Java runtime (`.vscode/settings.json` is configured to use `${env:JAVA_HOME}`). Ensure your terminal and IDE use the same JDK to avoid build/runtime differences.
- Tests and JPQL queries: some JPQL usages that used `LIMIT` were refactored to use Spring Data `Pageable` to be compatible with JPA and tests.
- Build tooling: the `spring-boot-maven-plugin` was explicitly pinned in `pom.xml` to avoid version skew during `mvn spring-boot:run`.

If you see Hibernate schema-validation errors complaining about `enum` vs `varchar` column types, apply a forward Flyway migration that runs an `ALTER TABLE ... MODIFY COLUMN ... VARCHAR(...)` for the affected column and re-run `mvn flyway:migrate` before starting the application.
