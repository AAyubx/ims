````markdown
# Database Connection Fix
_Updated: 2025-09-03_

## Issue Resolved
The MySQL connection was failing because there was a local MySQL instance running on port 3306, conflicting with the Docker MySQL container.

## Solution Applied
1. **Changed Docker MySQL port** from `3306` to `3307` to avoid conflicts
2. **Updated application configuration** to use port 3307
3. **Fixed user authentication** to use `mysql_native_password`

## Correct Connection Details

### MySQL Connection from Command Line
```bash
# Use 127.0.0.1 instead of localhost, and port 3307
mysql -h 127.0.0.1 -P 3307 -u inventory_user -p
# Password: inventory_pass
```

### Application Configuration
The following files have been updated to use port 3307:
- `src/main/resources/application.yml`
- `pom.xml` (Flyway configuration)
- `docker-compose.yml`

### Services Access
- **MySQL**: `localhost:3307`
- **Redis**: `localhost:6379`  
- **phpMyAdmin**: `http://localhost:8080`
- **Redis Commander**: `http://localhost:8081`

### Verification Commands
```bash
# Test MySQL connection
mysql -h 127.0.0.1 -P 3307 -u inventory_user -pinventory_pass -e "SELECT 'Success!' as status;"

# Check Docker containers
docker-compose ps

# Test application startup
mvn spring-boot:run
```

## Why This Happened
- You have a local MySQL installation running on port 3306
- Docker was trying to use the same port, causing conflicts
- MySQL client defaults to connecting to `localhost` which resolves to the local MySQL instance
- Using `127.0.0.1` forces TCP connection to the Docker container on the specified port

## Next Steps
1. The database connection should now work correctly
2. You can proceed with running Flyway migrations: `mvn flyway:migrate`
3. Start the Spring Boot application: `mvn spring-boot:run`
````
