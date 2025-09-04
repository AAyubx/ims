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

### Local Mail & Actuator notes

- The development profile uses MailHog on `localhost:1025` for SMTP and `http://localhost:8025` for the MailHog UI. If MailHog is not running, the mail health check will report DOWN.
- To run MailHog quickly for local testing:

```bash
docker run --rm -p 8025:8025 -p 1025:1025 mailhog/mailhog
```

- Alternatively run a lightweight Python SMTP debug server (prints emails to stdout):

```bash
python3 -m smtpd -n -c DebuggingServer localhost:1025
```

- During development the app disables SMTP auth/starttls and the mail health indicator can be turned off in the `dev` profile to avoid failing actuator checks when no credentials are present. After changing `application.yml`, restart the app for changes to take effect.
