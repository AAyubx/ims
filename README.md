# Inventory Management System

A modern, multi-tenant inventory management system built with Spring Boot, featuring comprehensive user management, authentication, and audit capabilities.

## 🚀 Features

### Core Functionality
- **Multi-tenant Architecture**: Complete tenant isolation and data security
- **User Management**: Admin user creation, role assignment, and lifecycle management
- **Advanced Authentication**: JWT-based auth with password policies and session management
- **Security Controls**: Failed login tracking, account lockout, and password history
- **Audit Logging**: Comprehensive tracking of all user actions and system changes

### Security Features
- Password policy enforcement (complexity, expiry, history)
- Account lockout after failed login attempts
- Session management and timeout controls
- Role-based access control (RBAC)
- Comprehensive audit trail

### Technical Stack
- **Backend**: Spring Boot 3.2, Spring Security 6, Spring Data JPA
- **Database**: MySQL 8.0 with Flyway migrations
- **Caching**: Redis for session and configuration caching
- **Authentication**: JWT tokens with refresh mechanism
- **Documentation**: OpenAPI 3 (Swagger)
- **Testing**: JUnit 5, Mockito, Testcontainers

## 📋 Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Git

## 🛠️ Quick Start

1. **Clone and Navigate**
   ```bash
   git clone <repository-url>
   cd inventory-management-system
   ```

2. **Start Services**
   ```bash
   docker-compose up -d mysql redis
   ```

3. **Build and Run**
   ```bash
   mvn clean compile
   mvn flyway:migrate
   mvn spring-boot:run
   ```

4. **Verify Setup**
   - API: http://localhost:8080/api/actuator/health
   - Docs: http://localhost:8080/api/swagger-ui.html
   - phpMyAdmin: http://localhost:8080

For detailed setup instructions, see [SETUP_INSTRUCTIONS.md](SETUP_INSTRUCTIONS.md).

## 📚 Documentation

- [Setup Instructions](SETUP_INSTRUCTIONS.md) - Complete setup guide
- [Tech Stack Requirements](TECH_STACK_REQUIREMENTS.md) - Technical requirements and architecture
- [Admin User Management Design](ADMIN_USER_MANAGEMENT_DESIGN.md) - Admin module specifications
- [User Authentication Design](USER_AUTHENTICATION_DESIGN.md) - Authentication system design
- [Microservices Integration](microservices_integration_diagram.md) - System architecture

## 🏗️ Project Structure

```
src/
├── main/java/com/inventory/
│   ├── entity/           # JPA entities
│   ├── repository/       # Spring Data repositories
│   ├── service/          # Business logic services
│   ├── controller/       # REST controllers
│   ├── dto/             # Data transfer objects
│   ├── config/          # Configuration classes
│   └── security/        # Security components
├── main/resources/
│   ├── db/migration/    # Flyway migration scripts
│   └── application.yml  # Configuration
└── test/                # Tests
```

## 🔐 Default Users

| Email | Role | Employee Code | Status |
|-------|------|---------------|--------|
| admin@demo.example | ADMIN | EMP001 | Password required |
| manager@demo.example | MANAGER | EMP002 | Password required |

Use the password reset functionality to set initial passwords.

## 🔧 Configuration

### Key Settings
- **Password Expiry**: 60 days (configurable)
- **Login Attempts**: 5 max before lockout
- **Session Timeout**: 8 hours
- **Password History**: Last 3 passwords remembered

### Environment Variables
```bash
JWT_SECRET=your-256-bit-secret
DB_PASSWORD=secure-password
MAIL_USERNAME=smtp-user
MAIL_PASSWORD=smtp-pass
```

## 🧪 Testing

```bash
# Run all tests
mvn test

# Integration tests only
mvn test -Dtest=**/*IntegrationTest

# Generate coverage report
mvn clean test jacoco:report
```

## 📊 API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout
- `POST /api/auth/change-password` - Change password
- `POST /api/auth/forgot-password` - Password reset request

### Admin User Management
- `GET /api/admin/users` - List users
- `POST /api/admin/users` - Create user
- `PUT /api/admin/users/{id}` - Update user
- `DELETE /api/admin/users/{id}` - Deactivate user

Full API documentation available at `/swagger-ui.html`.

## 🐳 Docker Services

The `docker-compose.yml` includes:
- **MySQL 8.0**: Primary database
- **Redis 7**: Caching and sessions
- **phpMyAdmin**: Database management UI
- **Redis Commander**: Redis management UI

## 🔍 Monitoring & Health

- **Health Check**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Application Logs**: `logs/inventory-management.log`

## 🚧 Development

### Database Changes
1. Create migration in `src/main/resources/db/migration/`
2. Run `mvn flyway:migrate`

### Adding Features
1. Create/update entities
2. Add repository methods
3. Implement service layer
4. Create controllers
5. Add tests

## 🔒 Security Best Practices

- JWT tokens with configurable expiration
- Password complexity enforcement
- Account lockout protection
- Session management
- Comprehensive audit logging
- SQL injection protection
- XSS protection via Spring Security

## 📈 Roadmap

- [ ] Multi-factor authentication (MFA)
- [ ] OAuth 2.0 / OpenID Connect integration
- [ ] Advanced inventory features
- [ ] Real-time notifications
- [ ] Mobile app support
- [ ] Advanced reporting and analytics

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Ensure all tests pass
6. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Support

For setup issues or questions:
1. Check [SETUP_INSTRUCTIONS.md](SETUP_INSTRUCTIONS.md)
2. Review application logs in `logs/`
3. Verify Docker services are running
4. Check database connectivity

---

**Built with ❤️ using Spring Boot and modern Java practices**