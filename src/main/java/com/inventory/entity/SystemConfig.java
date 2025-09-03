package com.inventory.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "config_key", nullable = false, unique = true, length = 64)
    private String configKey;

    @Column(name = "config_value", nullable = false, length = 512)
    private String configValue;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private UserAccount updatedBy;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum ConfigKey {
        PASSWORD_EXPIRY_DAYS("password.expiry.days"),
        PASSWORD_MIN_LENGTH("password.min.length"),
        PASSWORD_REQUIRE_UPPERCASE("password.require.uppercase"),
        PASSWORD_REQUIRE_LOWERCASE("password.require.lowercase"),
        PASSWORD_REQUIRE_DIGIT("password.require.digit"),
        PASSWORD_REQUIRE_SPECIAL("password.require.special"),
        PASSWORD_HISTORY_COUNT("password.history.count"),
        LOGIN_MAX_ATTEMPTS("login.max.attempts"),
        LOGIN_LOCKOUT_MINUTES("login.lockout.minutes"),
        SESSION_TIMEOUT_MINUTES("session.timeout.minutes"),
        PASSWORD_RESET_TOKEN_EXPIRY_HOURS("password.reset.token.expiry.hours");

        private final String key;

        ConfigKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }
}