# Inventory Management System

A modern, multi-tenant inventory management system built with Spring Boot, featuring comprehensive user management, authentication, and audit capabilities.

## 🚧 Development Progress: 90% Complete

✅ **Phase 1 Complete**: Authentication, User Management, Store Setup  
✅ **Phase 2 Complete**: Item Master & Catalog Management System Operational  
🔄 **Current Phase**: Final Testing & Production Readiness  
📅 **Major Milestone Achieved**: September 23, 2025 - Add functionality completed

| Component | Status | Progress |
|-----------|--------|----------|
| **Multi-Tenant Auth** | ✅ Complete | 100% |
| **User Management** | ✅ Complete | 100% |
| **Store Management** | ✅ Complete | 95% |
| **Email Service** | ✅ Complete | 100% |
| **API Documentation** | ✅ Complete | 100% |
| **Item Master & Catalog** | ✅ Complete | 90% |
| **Database Schema** | ✅ Complete | 100% |
| **Frontend UI** | ✅ Complete | 95% |

[📊 View Detailed Progress Tracking →](Documentation/modern_inventory_system_critical_features.md)

## 🚀 Features

### Core Functionality

- **Multi-tenant Architecture**: Complete tenant isolation and data security
- **Store Management**: Complete store creation with multi-step wizard, location setup, and tax configuration
- **Comprehensive User Management**: Full CRUD operations, advanced filtering, bulk actions
- **Role-Based Access Control**: Admin user creation, role assignment, and lifecycle management
- **Session Management**: Active session tracking, individual/bulk session termination
- **Advanced Authentication**: JWT-based auth with password policies and session management
- **Email Service**: Professional welcome emails, password reset notifications, and account updates
- **Security Controls**: Failed login tracking, account lockout, password reset, and password history
- **Audit Logging**: Comprehensive tracking of all user actions and system changes

### Security Features

- Password policy enforcement (complexity, expiry, history)
- **Mandatory password change on first login** with interactive modal and validation
- Account lockout after failed login attempts with manual unlock capability
- Advanced session management and timeout controls
- Individual and bulk session termination
- Role-based access control (RBAC) with dynamic role assignment
- Comprehensive audit trail with detailed user action tracking
- Secure user activation/deactivation workflow

### Technical Stack

**Backend Framework**
- **Spring Boot 3.2** - Main application framework with auto-configuration
- **Spring Security 6** - Authentication, authorization, and session management
- **Spring Data JPA** - Database abstraction with Hibernate ORM
- **Spring Mail** - Email service with Thymeleaf templating

**Frontend Framework**
- **Next.js 14** - React framework with App Router and SSR
- **React 18** - Component-based UI with TypeScript
- **Tailwind CSS** - Utility-first CSS framework
- **Zustand** - Lightweight state management
- **React Hook Form + Zod** - Form handling with validation

**Database & Caching**
- **MySQL 8.0** - Primary relational database with multi-tenant support
- **Flyway** - Database migration and version control
- **Redis 7** - Session storage and application caching

**Development & Deployment**
- **Docker & Docker Compose** - Containerized development environment
- **Maven** - Build automation and dependency management
- **MailHog** - Email testing in development
- **OpenAPI 3 (Swagger)** - API documentation and testing

**Security & Authentication**
- **JWT Tokens** - Stateless authentication with refresh mechanism
- **BCrypt** - Password hashing with configurable strength
- **RBAC** - Role-based access control with granular permissions

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

3. **Build and Run Backend**

   ```bash
   mvn clean compile
   mvn flyway:migrate
   mvn spring-boot:run
   ```

4. **Start Frontend (separate terminal)**

   ```bash
   cd inventory-ui
   npm install
   npm run dev
   ```

5. **Verify Setup**
   - Backend API: http://localhost:8080/api/actuator/health
   - Frontend App: http://localhost:3000
   - API Docs: http://localhost:8080/api/swagger-ui.html
   - phpMyAdmin: http://localhost:8080

For detailed setup instructions, see [SETUP_INSTRUCTIONS.md](SETUP_INSTRUCTIONS.md).

## 📚 Documentation

### Core Documentation
- [Development Plan & Features](Documentation/modern_inventory_system_critical_features.md) - Comprehensive feature roadmap with progress tracking
- [Tech Stack & Architecture](Documentation/tech-stack-and-architecture.md) - Technical architecture overview
- [Setup & Deployment Guide](Documentation/setup-and-deployment.md) - Complete deployment instructions
- [Database Schema](Documentation/database-and-schema.md) - Database design and migration guides

### Feature-Specific Guides
- [Admin & Authentication](Documentation/admin-and-authentication.md) - User management and security features
- [Store Creation System](Documentation/store-creation.md) - Multi-step store setup wizard
- [Email Service](Documentation/email-service.md) - Email templates and SMTP configuration
- [UI Design & Implementation](Documentation/ui-design-and-tech-stack.md) - Frontend architecture and design system

### Additional Resources
- [Service Testing](Documentation/service-testing.md) - Testing strategies and frameworks
- [Security & Authentication](Documentation/security-and-auth.md) - Security implementation details
- [ERP UI Guide](Documentation/ERP_UI_Guide.md) - User interface guidelines

## 🏗️ Project Structure

```
.
├── src/main/java/com/inventory/          # Backend Spring Boot Application
│   ├── entity/                          # JPA entities (User, Store, Location, etc.)
│   ├── repository/                      # Spring Data JPA repositories
│   ├── service/                         # Business logic services
│   ├── controller/                      # REST API controllers
│   ├── dto/                             # Data transfer objects
│   ├── config/                          # Configuration classes
│   ├── security/                        # Security & JWT components
│   └── exception/                       # Custom exception handlers
├── src/main/resources/
│   ├── db/migration/                    # Flyway database migration scripts
│   ├── templates/                       # Thymeleaf email templates
│   └── application.yml                  # Application configuration
├── src/test/                            # Backend unit & integration tests
├── inventory-ui/                        # Frontend Next.js Application
│   ├── src/app/                         # Next.js 14 app router
│   ├── src/components/                  # Reusable React components
│   ├── src/stores/                      # Zustand state management
│   ├── src/types/                       # TypeScript type definitions
│   ├── src/lib/                         # Utility libraries
│   └── src/utils/                       # Helper utilities
├── Documentation/                       # Project documentation
│   ├── modern_inventory_system_critical_features.md
│   ├── admin-and-authentication.md
│   ├── store-creation.md
│   └── archived/                        # Legacy documentation
├── logs/                                # Application log files
├── docker-compose.yml                   # Multi-service development setup
└── pom.xml                              # Maven project configuration
```

## 🔐 Default Users

| Email                | Role    | Employee Code | Status            |
| -------------------- | ------- | ------------- | ----------------- |
| admin@demo.example   | ADMIN   | EMP001        | Password required |
| manager@demo.example | MANAGER | EMP002        | Password required |

Use the password reset functionality to set initial passwords.

## 🔧 Configuration

### Key Settings

- **Password Expiry**: 60 days (configurable)
- **Login Attempts**: 5 max before lockout
- **Session Timeout**: 8 hours
- **Password History**: Last 3 passwords remembered
- **API Base URL**: http://localhost:8080/api (frontend config)

### Environment Variables

```bash
# Backend
JWT_SECRET=your-256-bit-secret
DB_PASSWORD=secure-password
MAIL_USERNAME=smtp-user
MAIL_PASSWORD=smtp-pass

# Frontend (inventory-ui/.env.local)
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api
```

## 🧪 Testing

**Backend Tests:**

```bash
# Run all tests
mvn test

# Integration tests only
mvn test -Dtest=**/*IntegrationTest

# Generate coverage report
mvn clean test jacoco:report
```

**Frontend Tests:**

```bash
# Navigate to frontend directory
cd inventory-ui

# Run tests
npm test

# Run tests in watch mode
npm run test:watch
```

## 📊 API Endpoints

### Authentication

- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout
- `POST /api/auth/change-password` - Change password
- `POST /api/auth/forgot-password` - Password reset request

### Admin User Management

- `GET /api/admin/users` - List users with filtering and pagination
- `GET /api/admin/users/{id}` - Get user details by ID
- `POST /api/admin/users` - Create new user
- `PUT /api/admin/users/{id}` - Update user information
- `DELETE /api/admin/users/{id}` - Deactivate user (soft delete)
- `POST /api/admin/users/{id}/activate` - Activate user account
- `POST /api/admin/users/{id}/reset-password` - Reset user password
- `POST /api/admin/users/{id}/unlock` - Unlock locked user account
- `GET /api/admin/users/{id}/sessions` - Get active user sessions
- `DELETE /api/admin/users/{id}/sessions/{sessionId}` - Terminate specific session
- `DELETE /api/admin/users/{id}/sessions` - Terminate all user sessions
- `GET /api/admin/users/roles` - Get available roles
- `POST /api/admin/users/bulk-actions` - Perform bulk operations

### Store Management

- `GET /api/admin/locations` - List all stores/locations with filtering
- `GET /api/admin/locations/{id}` - Get store details by ID
- `POST /api/admin/locations` - Create new store with location setup
- `PUT /api/admin/locations/{id}` - Update store information
- `GET /api/admin/locations/tax-jurisdictions` - Get available tax jurisdictions
- `GET /api/admin/locations/currencies` - Get supported currencies

Full API documentation available at `/swagger-ui.html`.

### Admin User Management Features

The comprehensive admin user management system includes:

**User Lifecycle Management:**

- Create users with custom or auto-generated passwords
- Update user information and role assignments
- Activate/deactivate user accounts with proper audit trails
- Bulk operations for efficient multi-user management

**Advanced Filtering & Search:**

- Search by email, name, or employee code
- Filter by status (ACTIVE/INACTIVE), first name, email
- Paginated results with customizable sorting
- Real-time user status tracking

**Security & Session Control:**

- View and manage active user sessions
- Terminate individual or all sessions for any user
- Password reset functionality with temporary passwords
- Account unlock capabilities for locked accounts
- Role-based permissions with dynamic role assignment

**Bulk Operations:**

- Bulk activate/deactivate multiple users
- Bulk password reset with generated temporary passwords
- Efficient batch processing with detailed response feedback

**Database Schema Updates:**

- New `password_reset_token` table for secure password reset functionality
- Enhanced `location` table with comprehensive store management fields
- New `tax_jurisdiction` table for location-based tax configuration
- New `location_currency` table for multi-currency support per location
- Token expiry tracking with configurable timeout (24 hours default)
- Rate limiting support (3 requests per hour per email)
- IP address and user agent tracking for security audit
- Multi-tenant isolation with proper foreign key constraints

## 🏪 Store Management Features

The comprehensive store management system includes:

**Store Creation Wizard:**

- ✅ Multi-step guided store creation process (2-step wizard implemented)
- ✅ Basic information setup (name, code, type, description)
- ✅ Location configuration with full address details
- ❌ Tax jurisdiction assignment for compliance (backend ready, UI missing)
- ⏸️ Multi-currency support per store location (auto-selection implemented, manual selection missing)

**Store Management Capabilities:**

- ✅ Complete CRUD operations for store locations
- ✅ Store status management (ACTIVE/INACTIVE)
- ❌ Tax configuration with jurisdiction-based rules (backend ready, UI missing)
- ⏸️ Currency settings per location for international operations (basic implementation)
- ✅ Address validation and geographic data storage

**Frontend Components:**

- ✅ React-based store creation wizard with TypeScript
- ✅ Form validation using Zod schemas
- ✅ Step-by-step navigation with progress tracking
- ✅ Responsive design with Tailwind CSS
- ✅ Integration with backend APIs for real-time validation

**Current Implementation Status:**

- **Completed**: Basic 2-step store creation wizard with address and GPS coordinates
- **Missing**: Tax jurisdiction selection, advanced currency configuration, store hierarchy visualization
- **Backend Ready**: All database tables and APIs exist for missing UI components

## 🐳 Docker Services

The `docker-compose.yml` includes:

- **MySQL 8.0**: Primary database with multi-tenant support
- **Redis 7**: Caching and session storage
- **phpMyAdmin**: Database management UI (http://localhost:8080)
- **Redis Commander**: Redis management UI (http://localhost:8081)
- **MailHog**: Email testing service (UI: http://localhost:8025, SMTP: 1025)

## 📧 Email Service

### Features

- **Professional Templates**: Beautiful HTML email templates with company branding
- **Account Creation**: Automatic welcome emails for new users with account details
- **Password Management**: Password reset and change notifications
- **Multi-format**: Both HTML and plain text email support
- **Development Testing**: MailHog integration for local email testing

### Email Types

- **Welcome Email**: Sent automatically when admin creates new user account
- **Password Reset**: Secure password reset with expiring tokens
- **Password Changed**: Confirmation when password is successfully updated
- **Account Updates**: Notifications for account status changes

### Configuration

```yaml
spring:
  mail:
    host: ${MAIL_HOST:localhost} # MailHog for dev, SMTP server for prod
    port: ${MAIL_PORT:1025} # 1025 for MailHog, 587 for production
    username: ${MAIL_USERNAME:}
    password: ${MAIL_PASSWORD:}
```

### Testing Emails

1. **Start MailHog**: Already included in docker-compose.yml
2. **Create User**: Use the admin interface to create a new user
3. **View Email**: Open http://localhost:8025 to see the sent welcome email
4. **Production**: Configure SMTP settings via environment variables

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

### Project Configuration

- **Prompts folder**: Added to `.gitignore` to exclude development prompts and temporary files from version control
- **Development files**: Local configuration and prompt files are excluded from repository

## 🔒 Security Best Practices

- JWT tokens with configurable expiration
- Password complexity enforcement
- Account lockout protection
- Session management
- Comprehensive audit logging
- SQL injection protection
- XSS protection via Spring Security

## 📈 Development Roadmap

### 🚧 Current Phase (Authentication & Store Management - 65% Complete)
- ✅ Multi-tenant architecture with full data isolation
- ✅ JWT-based authentication with refresh tokens
- ✅ Comprehensive admin user management (CRUD, filtering, bulk operations)
- ✅ Professional email service with templated notifications
- ✅ Store creation wizard with location management
- 🔄 Tax jurisdiction configuration for stores
- 🔄 Enhanced multi-currency support

### ✅ Recently Completed: Item Master & Catalog Management (Sept 23, 2025)

**Major System Implementation Completed:**
- ✅ **Complete Database Schema**: All entities, relationships, and migrations operational
- ✅ **Item & Catalog Management**: Items, Categories, Brands, Departments, Attributes fully functional
- ✅ **Frontend UI Pages**: All catalog management pages built and operational
- ✅ **Data Integrity**: Schema validation, foreign keys, and business rules enforced
- ✅ **System Integration**: Application starting successfully, all components working

**Key Technical Achievements:**
- ✅ **7 Database Migrations (V21-V27)**: Complete schema alignment with JPA entities
- ✅ **Entity Relationship Resolution**: All Hibernate validation errors resolved
- ✅ **Frontend Dependencies**: All React components, icons, and packages properly configured
- ✅ **HTML Structure Fixes**: All hydration errors and invalid markup resolved
- ✅ **Repository Queries**: Non-existent relationship references properly handled

### 📦 Next Phase: Advanced Inventory Features
- [ ] **Stock Control & Movements**: Real-time inventory tracking, adjustments, transfers
- [ ] **Barcode Integration**: Label generation, mobile scanning capabilities  
- [ ] **Supplier Integration**: Enhanced vendor management with procurement workflows
- [ ] **Advanced Search**: Full-text search with facets and filtering
- [ ] **Bulk Operations**: Import/export and mass data management

### 🔮 Future Enhancements
- [ ] **Advanced Analytics**: Real-time dashboards, inventory insights, reporting
- [ ] **Mobile Applications**: Native iOS/Android apps for warehouse operations
- [ ] **Multi-factor Authentication**: Enhanced security with 2FA/MFA
- [ ] **OAuth Integration**: SSO with Google, Microsoft, and enterprise providers
- [ ] **Real-time Notifications**: WebSocket-based alerts and updates
- [ ] **RFID Support**: Advanced tracking with RFID technology
- [ ] **API Ecosystem**: Webhook system and third-party integrations

For detailed progress tracking, see [Development Plan](Documentation/modern_inventory_system_critical_features.md).


## 📄 License

This project is licensed under the LGPL-3.0 License - see the LICENSE file for details.

---

**Built with ❤️ using Spring Boot and modern Java practices**
