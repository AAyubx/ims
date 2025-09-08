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
}