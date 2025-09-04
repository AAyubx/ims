# Setup Instructions

_Last updated: 2025-09-03_

Step-by-step developer setup for local development.

## Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Git

## Quickstart
1. Clone the repo
2. Start infrastructure: `docker-compose up -d mysql redis`
3. Build app: `mvn clean compile`
4. Run migrations: `mvn flyway:migrate`
5. Start app: `mvn spring-boot:run`

## Useful Commands
```bash
# run tests
mvn test

# package
mvn package -DskipTests

# run flyway migrations
mvn flyway:migrate
```

## Default Accounts
- admin@demo.example — reset password via DB or password reset flow (see notes)

## Notes
- Ensure `JAVA_HOME` points to a JDK 17 installation.
- If you need to set a temporary admin password in DB: use a bcrypt hash written as binary literal to preserve the leading `$`.
