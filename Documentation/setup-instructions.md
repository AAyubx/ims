````markdown
# Setup Instructions
_Last updated: 2025-09-03_

Quickstart to run the project locally for development.

## Prerequisites
- Java 17+
- Maven 3.8+
- Docker Desktop
- Git

## Steps
1. Clone repo and change directory
2. Start DB and Redis
   ```bash
   docker-compose up -d mysql redis
   ```
3. Build and run migrations
   ```bash
   mvn clean compile
   mvn flyway:migrate
   ```
4. Start the app
   ```bash
   mvn spring-boot:run
   ```

## Default accounts
- `admin@demo.example` — use password reset or update DB to set a temporary password

## Troubleshooting
- Check `docker-compose ps` and `docker-compose logs mysql`
- Use `mysql -h 127.0.0.1 -P 3307 -u inventory_user -pinventory_pass` if Docker port is 3307

````
