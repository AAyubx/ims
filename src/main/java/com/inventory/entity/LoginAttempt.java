package com.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "failure_reason", length = 100)
    private String failureReason;

    @CreatedDate
    @Column(name = "attempted_at", nullable = false, updatable = false)
    private LocalDateTime attemptedAt;

    public enum FailureReason {
        INVALID_CREDENTIALS("Invalid email or password"),
        ACCOUNT_LOCKED("Account is locked"),
        ACCOUNT_DISABLED("Account is disabled"),
        PASSWORD_EXPIRED("Password has expired"),
        TOO_MANY_ATTEMPTS("Too many failed attempts");

        private final String description;

        FailureReason(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}