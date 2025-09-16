package com.inventory.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "store_configuration", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"location_id", "config_key"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
public class StoreConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "config_key", nullable = false, length = 100)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "config_type", nullable = false, length = 20)
    private ConfigType configType = ConfigType.STRING;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum ConfigType {
        STRING, JSON, BOOLEAN, NUMBER, DATE
    }

    /**
     * Get the configuration value as a Boolean
     * @return Boolean value if type is BOOLEAN, null otherwise
     */
    public Boolean getBooleanValue() {
        if (configType == ConfigType.BOOLEAN && configValue != null) {
            return Boolean.parseBoolean(configValue);
        }
        return null;
    }

    /**
     * Get the configuration value as a Number
     * @return Double value if type is NUMBER, null otherwise
     */
    public Double getNumberValue() {
        if (configType == ConfigType.NUMBER && configValue != null) {
            try {
                return Double.parseDouble(configValue);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Set a boolean configuration value
     */
    public void setBooleanValue(Boolean value) {
        this.configType = ConfigType.BOOLEAN;
        this.configValue = value != null ? value.toString() : null;
    }

    /**
     * Set a number configuration value
     */
    public void setNumberValue(Number value) {
        this.configType = ConfigType.NUMBER;
        this.configValue = value != null ? value.toString() : null;
    }
}