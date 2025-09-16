package com.inventory.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "location_currency", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"location_id", "currency_code"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
public class LocationCurrency {

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

    @Column(name = "currency_code", nullable = false, length = 3, columnDefinition = "CHAR(3)")
    private String currencyCode;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    @Column(name = "exchange_rate", nullable = false, precision = 12, scale = 6)
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Validate entity data before persist/update
     */
    @PrePersist
    @PreUpdate
    private void validateEntity() {
        // Validate currency code format (3 uppercase letters)
        if (currencyCode != null) {
            currencyCode = currencyCode.toUpperCase();
            if (!currencyCode.matches("^[A-Z]{3}$")) {
                throw new IllegalArgumentException("Currency code must be 3 uppercase letters");
            }
        }
        
        // Validate exchange rate is positive
        if (exchangeRate != null && exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Exchange rate must be positive");
        }
    }
}