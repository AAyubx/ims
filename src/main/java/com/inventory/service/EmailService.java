package com.inventory.service;

import com.inventory.entity.UserAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from:noreply@inventory.com}")
    private String fromEmail;

    @Value("${app.mail.from-name:Inventory Management System}")
    private String fromName;

    @Value("${app.base-url:http://localhost:3000}")
    private String baseUrl;

    @Value("${app.company.name:Your Company}")
    private String companyName;

    public void sendPasswordResetEmail(String toEmail, String displayName, String resetToken, LocalDateTime expiresAt) {
        try {
            String resetUrl = baseUrl + "/reset-password?token=" + resetToken;
            
            Context context = new Context();
            context.setVariable("displayName", displayName != null ? displayName : "User");
            context.setVariable("resetUrl", resetUrl);
            context.setVariable("expiresAt", expiresAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
            context.setVariable("companyName", companyName);
            context.setVariable("supportEmail", fromEmail);
            
            String htmlBody = templateEngine.process("email/password-reset", context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Reset Your Password - " + companyName);
            helper.setText(htmlBody, true);
            
            mailSender.send(message);
            
            log.info("Password reset email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {} - {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    public void sendPasswordResetEmailPlainText(String toEmail, String displayName, String resetToken, LocalDateTime expiresAt) {
        try {
            String resetUrl = baseUrl + "/reset-password?token=" + resetToken;
            
            String subject = "Reset Your Password - " + companyName;
            String body = buildPlainTextResetEmail(displayName, resetUrl, expiresAt);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            
            log.info("Password reset email (plain text) sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send password reset email (plain text) to: {} - {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    public void sendPasswordChangedNotification(String toEmail, String displayName, String ipAddress, String userAgent) {
        try {
            Context context = new Context();
            context.setVariable("displayName", displayName != null ? displayName : "User");
            context.setVariable("ipAddress", ipAddress);
            context.setVariable("userAgent", userAgent);
            context.setVariable("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
            context.setVariable("companyName", companyName);
            context.setVariable("supportEmail", fromEmail);
            
            String htmlBody = templateEngine.process("email/password-changed", context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Password Changed - " + companyName);
            helper.setText(htmlBody, true);
            
            mailSender.send(message);
            
            log.info("Password changed notification sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send password changed notification to: {} - {}", toEmail, e.getMessage());
            // Don't throw exception for notification emails - they're not critical
        }
    }

    public void sendAdminPasswordResetEmail(String toEmail, String displayName, String resetToken, LocalDateTime expiresAt) {
        try {
            String resetUrl = baseUrl + "/reset-password?token=" + resetToken;
            
            Context context = new Context();
            context.setVariable("displayName", displayName != null ? displayName : "User");
            context.setVariable("resetUrl", resetUrl);
            context.setVariable("expiresAt", expiresAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
            context.setVariable("companyName", companyName);
            context.setVariable("supportEmail", fromEmail);
            context.setVariable("isAdminInitiated", true);
            
            String htmlBody = templateEngine.process("email/admin-password-reset", context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Required - " + companyName);
            helper.setText(htmlBody, true);
            
            mailSender.send(message);
            
            log.info("Admin-initiated password reset email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send admin password reset email to: {} - {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send admin password reset email", e);
        }
    }

    public void sendPasswordChangedNotificationPlainText(String toEmail, String displayName, String ipAddress, String userAgent) {
        try {
            String subject = "Password Changed - " + companyName;
            String body = buildPlainTextPasswordChangedEmail(displayName, ipAddress, userAgent);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            
            log.info("Password changed notification (plain text) sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send password changed notification (plain text) to: {} - {}", toEmail, e.getMessage());
            // Don't throw exception for notification emails - they're not critical
        }
    }

    private String buildPlainTextResetEmail(String displayName, String resetUrl, LocalDateTime expiresAt) {
        return String.format("""
            Hello %s,
            
            We received a request to reset your password for your %s account.
            
            To reset your password, please click on the following link:
            %s
            
            This link will expire on %s.
            
            If you did not request this password reset, please ignore this email or contact support if you have concerns.
            
            For security reasons, this link can only be used once.
            
            Best regards,
            The %s Team
            
            If you're having trouble clicking the password reset button, copy and paste the URL below into your web browser:
            %s
            """, 
            displayName != null ? displayName : "User",
            companyName,
            resetUrl,
            expiresAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
            companyName,
            resetUrl
        );
    }

    private String buildPlainTextPasswordChangedEmail(String displayName, String ipAddress, String userAgent) {
        return String.format("""
            Hello %s,
            
            Your password for your %s account was successfully changed.
            
            Details:
            - Date: %s
            - IP Address: %s
            - Browser: %s
            
            If you did not change your password, please contact support immediately.
            
            Best regards,
            The %s Team
            
            Support Email: %s
            """,
            displayName != null ? displayName : "User",
            companyName,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
            ipAddress != null ? ipAddress : "Unknown",
            userAgent != null ? userAgent : "Unknown",
            companyName,
            fromEmail
        );
    }
    
    public void sendAccountCreationEmail(String toEmail, String displayName, String employeeCode, 
                                       List<String> roles, String temporaryPassword) {
        try {
            String loginUrl = baseUrl + "/login";
            boolean hasTemporaryPassword = temporaryPassword != null && !temporaryPassword.trim().isEmpty();
            
            Context context = new Context();
            context.setVariable("displayName", displayName != null ? displayName : "User");
            context.setVariable("email", toEmail);
            context.setVariable("employeeCode", employeeCode != null ? employeeCode : "N/A");
            context.setVariable("roles", String.join(", ", roles != null ? roles : List.of("User")));
            context.setVariable("hasTemporaryPassword", hasTemporaryPassword);
            context.setVariable("temporaryPassword", hasTemporaryPassword ? temporaryPassword : "");
            context.setVariable("loginUrl", loginUrl);
            context.setVariable("createdAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
            context.setVariable("companyName", companyName);
            context.setVariable("supportEmail", fromEmail);
            
            String htmlBody = templateEngine.process("email/account-created", context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Welcome! Your Account Has Been Created - " + companyName);
            helper.setText(htmlBody, true);
            
            mailSender.send(message);
            
            log.info("Account creation email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send account creation email to: {} - {}", toEmail, e.getMessage());
            // Don't throw exception for notification emails - they're not critical to user creation
            // But we should log it for monitoring
        }
    }
    
    public void sendAccountCreationEmailPlainText(String toEmail, String displayName, String employeeCode, 
                                                List<String> roles, String temporaryPassword) {
        try {
            String subject = "Welcome! Your Account Has Been Created - " + companyName;
            String body = buildPlainTextAccountCreationEmail(toEmail, displayName, employeeCode, roles, temporaryPassword);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            
            log.info("Account creation email (plain text) sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send account creation email (plain text) to: {} - {}", toEmail, e.getMessage());
        }
    }
    
    private String buildPlainTextAccountCreationEmail(String email, String displayName, String employeeCode, 
                                                    List<String> roles, String temporaryPassword) {
        boolean hasTemporaryPassword = temporaryPassword != null && !temporaryPassword.trim().isEmpty();
        String rolesText = String.join(", ", roles != null ? roles : List.of("User"));
        
        StringBuilder body = new StringBuilder();
        body.append(String.format("Hello %s,\n\n", displayName != null ? displayName : "User"));
        body.append(String.format("Congratulations! Your account has been successfully created by an administrator for the %s Inventory Management System.\n\n", companyName));
        
        // Account Details
        body.append("ACCOUNT DETAILS:\n");
        body.append("================\n");
        body.append(String.format("Email Address: %s\n", email));
        body.append(String.format("Employee Code: %s\n", employeeCode != null ? employeeCode : "N/A"));
        body.append(String.format("Role(s): %s\n", rolesText));
        body.append("Account Status: Active\n\n");
        
        // Password Information
        if (hasTemporaryPassword) {
            body.append("TEMPORARY PASSWORD:\n");
            body.append("==================\n");
            body.append(String.format("Your temporary password is: %s\n", temporaryPassword));
            body.append("IMPORTANT: You will be required to change this password on your first login for security reasons.\n\n");
        } else {
            body.append("PASSWORD SETUP:\n");
            body.append("==============\n");
            body.append("A password reset link has been sent to help you set up your account password securely.\n\n");
        }
        
        // Next Steps
        body.append("NEXT STEPS:\n");
        body.append("===========\n");
        body.append("1. Log in to the system at: ").append(baseUrl).append("/login\n");
        if (hasTemporaryPassword) {
            body.append("2. Create a new secure password (required on first login)\n");
        } else {
            body.append("2. Check your email for password setup instructions\n");
        }
        body.append("3. Explore your dashboard and available features\n");
        body.append("4. Contact your administrator if you have any questions\n\n");
        
        // Security Tips
        body.append("SECURITY BEST PRACTICES:\n");
        body.append("========================\n");
        body.append("- Choose a strong password with at least 8 characters\n");
        body.append("- Include uppercase, lowercase, numbers, and special characters\n");
        body.append("- Never share your login credentials with others\n");
        body.append("- Log out when finished, especially on shared computers\n");
        body.append("- Report any suspicious activity to your administrator\n\n");
        
        body.append("NEED HELP?\n");
        body.append("==========\n");
        body.append("If you have any questions about your new account or need technical assistance, please contact us at: ");
        body.append(fromEmail).append("\n\n");
        
        body.append("Best regards,\n");
        body.append(String.format("The %s IT Team\n\n", companyName));
        
        body.append(String.format("This account was created on %s\n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))));
        body.append("If you believe this email was sent to you by mistake, please contact support immediately.\n");
        
        return body.toString();
    }
}