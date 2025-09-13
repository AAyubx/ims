# Email Service Documentation

_Last updated: 2025-09-13_

## Overview

The Email Service provides comprehensive email functionality for the Inventory Management System, including professional welcome emails for new user accounts, password management notifications, and system communications. Built with Spring Mail and Thymeleaf templating engine, it supports both HTML and plain text email formats with responsive design.

---

## Features

### 🎯 Core Capabilities
- **Account Creation Notifications**: Automatic welcome emails for new users
- **Password Management**: Reset, change, and admin-initiated password emails  
- **Professional Templates**: Beautiful, responsive HTML templates with company branding
- **Multi-format Support**: HTML with automatic plain text fallback
- **Development Testing**: MailHog integration for local email testing
- **Production Ready**: Configurable SMTP settings for production deployment

### 📧 Email Types

#### 1. Welcome Email (Account Creation)
**Trigger**: Automatically sent when admin creates new user account  
**Template**: `account-created.html`  
**Features**:
- Complete account details (email, employee code, roles, status)
- Conditional password display based on generation method
- Security best practices and password requirements
- Next steps checklist for new users
- Support contact information
- Professional company branding

#### 2. Password Reset Email  
**Trigger**: User requests password reset  
**Template**: `password-reset.html`  
**Features**:
- Secure reset link with expiring token
- Time limit warnings and security notes
- Clear instructions and fallback URL

#### 3. Password Changed Notification
**Trigger**: User successfully changes password  
**Template**: `password-changed.html`  
**Features**:
- Confirmation of password change
- Security details (IP, browser, timestamp)
- Contact information for unauthorized changes

#### 4. Admin Password Reset
**Trigger**: Admin resets user password  
**Template**: `admin-password-reset.html`  
**Features**:
- Admin-initiated reset notification
- Temporary password or setup instructions
- Security guidance and next steps

---

## Technical Implementation

### Architecture
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  AdminUserService  │──▶│   EmailService    │──▶│  JavaMailSender  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │ Thymeleaf Engine │
                       └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │ HTML Templates  │
                       └─────────────────┘
```

### Email Service Class
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    // Account creation email
    public void sendAccountCreationEmail(String toEmail, String displayName, 
                                       String employeeCode, List<String> roles, 
                                       String temporaryPassword);
    
    // Password management emails
    public void sendPasswordResetEmail(String toEmail, String displayName, 
                                     String resetToken, LocalDateTime expiresAt);
    
    public void sendPasswordChangedNotification(String toEmail, String displayName, 
                                              String ipAddress, String userAgent);
    
    public void sendAdminPasswordResetEmail(String toEmail, String displayName, 
                                          String resetToken, LocalDateTime expiresAt);
}
```

### Template Structure
```
src/main/resources/templates/email/
├── account-created.html      # Welcome email for new users
├── password-reset.html       # User-initiated password reset
├── password-changed.html     # Password change confirmation  
└── admin-password-reset.html # Admin-initiated password reset
```

### Template Variables
All templates support these common variables:
- `displayName`: User's display name
- `companyName`: Company/organization name  
- `supportEmail`: Support contact email
- `baseUrl`: Application base URL

**Account Creation Template Additional Variables**:
- `email`: User's email address
- `employeeCode`: User's employee code
- `roles`: Comma-separated list of assigned roles
- `hasTemporaryPassword`: Boolean flag for password display
- `temporaryPassword`: Generated password (if applicable)
- `loginUrl`: Login page URL
- `createdAt`: Account creation timestamp

---

## Configuration

### Application Properties
```yaml
spring:
  mail:
    host: ${MAIL_HOST:localhost}
    port: ${MAIL_PORT:1025}
    username: ${MAIL_USERNAME:}
    password: ${MAIL_PASSWORD:}
    properties:
      mail:
        smtp:
          auth: ${MAIL_AUTH:false}
          starttls:
            enable: ${MAIL_STARTTLS:false}
            
app:
  mail:
    from: ${APP_MAIL_FROM:noreply@inventory.com}
    from-name: ${APP_MAIL_FROM_NAME:Inventory Management System}
  base-url: ${APP_BASE_URL:http://localhost:3000}
  company:
    name: ${APP_COMPANY_NAME:Your Company}
```

### Environment Variables
| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `MAIL_HOST` | SMTP server hostname | localhost | No |
| `MAIL_PORT` | SMTP server port | 1025 (MailHog) | No |
| `MAIL_USERNAME` | SMTP authentication username | - | Production only |
| `MAIL_PASSWORD` | SMTP authentication password | - | Production only |
| `MAIL_AUTH` | Enable SMTP authentication | false | No |
| `MAIL_STARTTLS` | Enable STARTTLS encryption | false | No |
| `APP_MAIL_FROM` | From email address | noreply@inventory.com | No |
| `APP_MAIL_FROM_NAME` | From name | Inventory Management System | No |
| `APP_BASE_URL` | Application base URL | http://localhost:3000 | No |
| `APP_COMPANY_NAME` | Company name | Your Company | No |

---

## Development & Testing

### MailHog Integration
MailHog is included in `docker-compose.yml` for development email testing:

```yaml
mailhog:
  image: mailhog/mailhog:latest
  container_name: inventory-mailhog
  ports:
    - "1025:1025"  # SMTP port
    - "8025:8025"  # Web UI port
```

### Testing Workflow
1. **Start Services**: `docker-compose up -d`
2. **Create User**: Use admin interface to create new user account
3. **View Email**: Open http://localhost:8025 to see sent emails
4. **Verify Content**: Check email rendering and content accuracy

### Template Development
1. **Edit Template**: Modify files in `src/main/resources/templates/email/`
2. **Test Variables**: Use Thymeleaf context variables for dynamic content
3. **Preview**: Send test emails via MailHog to verify rendering
4. **Mobile Testing**: Check responsive design on different screen sizes

---

## Production Deployment

### SMTP Configuration
For production deployment, configure real SMTP server:

```yaml
# Production SMTP example (Gmail)
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

### Security Considerations
- **Authentication**: Always use SMTP authentication in production
- **Encryption**: Enable STARTTLS for secure email transmission
- **Credentials**: Store SMTP credentials in secure environment variables
- **Rate Limiting**: Configure SMTP server rate limits to prevent abuse
- **Monitoring**: Monitor email delivery rates and failures

### Email Deliverability
- **SPF Records**: Configure SPF records for your domain
- **DKIM Signing**: Enable DKIM signing for email authentication  
- **DMARC Policy**: Implement DMARC policy for additional security
- **Reputation**: Monitor sender reputation and delivery rates

---

## Monitoring & Logging

### Logging Configuration
Email operations are logged at INFO level for successful operations and ERROR level for failures:

```java
log.info("Welcome email sent to new user: {}", savedUser.getEmail());
log.error("Failed to send welcome email to new user: {} - {}", savedUser.getEmail(), e.getMessage());
```

### Key Metrics to Monitor
- **Email Delivery Rate**: Percentage of successfully sent emails
- **Template Rendering Time**: Performance of template processing
- **SMTP Connection Health**: SMTP server connectivity status
- **Bounce Rate**: Rate of bounced/failed email deliveries
- **User Engagement**: Open rates and click-through rates (if tracking enabled)

### Error Handling
- Email failures are logged but don't prevent user creation
- Graceful fallback to plain text emails if HTML rendering fails
- Retry mechanism for transient SMTP failures
- Dead letter queue for failed email attempts

---

## API Integration

### AdminUserService Integration
The EmailService is automatically called by AdminUserService when creating new users:

```java
// In AdminUserService.createUser()
try {
    List<String> roleNames = savedUser.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toList());
    
    String temporaryPassword = null;
    if (request.getInitialPassword() == null || request.getInitialPassword().trim().isEmpty()) {
        temporaryPassword = password; // Include auto-generated password
    }
    
    emailService.sendAccountCreationEmail(
        savedUser.getEmail(),
        savedUser.getDisplayName(),
        savedUser.getEmployeeCode(),
        roleNames,
        temporaryPassword
    );
    
} catch (Exception e) {
    log.error("Failed to send welcome email: {}", e.getMessage());
    // Continue execution - email failure shouldn't prevent user creation
}
```

---

## Troubleshooting

### Common Issues

#### Email Not Sent
1. **Check SMTP Configuration**: Verify host, port, and authentication settings
2. **Review Logs**: Check application logs for error messages
3. **Test Connectivity**: Verify network connectivity to SMTP server
4. **Validate Credentials**: Ensure SMTP username/password are correct

#### Template Rendering Errors
1. **Verify Template Syntax**: Check Thymeleaf template syntax
2. **Check Variables**: Ensure all required variables are provided
3. **Review Classpath**: Verify templates are in correct location
4. **Test Locally**: Use MailHog to test template rendering

#### Production Email Issues
1. **SPF/DKIM Setup**: Verify DNS records are correctly configured
2. **Sender Reputation**: Check if sender IP/domain is blacklisted
3. **Rate Limiting**: Ensure not exceeding SMTP server limits
4. **Content Filtering**: Check if emails are being flagged as spam

### Debug Mode
Enable debug logging for detailed email operation information:

```yaml
logging:
  level:
    com.inventory.service.EmailService: DEBUG
    org.springframework.mail: DEBUG
```

---

## Future Enhancements

### Planned Features
- **Email Templates Management**: Admin UI for template customization
- **Delivery Tracking**: Enhanced tracking and analytics
- **Internationalization**: Multi-language email templates
- **Advanced Personalization**: Dynamic content based on user preferences
- **Bulk Email Operations**: Mass email capabilities for system announcements

### Integration Opportunities  
- **Notification Center**: Centralized notification management
- **SMS Integration**: Multi-channel communication support
- **Push Notifications**: Mobile app notification integration
- **Webhook Support**: External system notification capabilities

---

This documentation provides comprehensive coverage of the Email Service functionality, from development setup through production deployment. For additional support or feature requests, please contact the development team.