package com.inventory.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tax_jurisdiction", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "code"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
public class TaxJurisdiction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "code", nullable = false, length = 32)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "country_code", nullable = false, length = 2, columnDefinition = "CHAR(2)")
    private String countryCode;

    @Column(name = "state_province", length = 100)
    private String stateProvince;

    @Column(name = "tax_rate", nullable = false, precision = 8, scale = 4)
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_type", nullable = false, length = 20)
    private TaxType taxType;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum TaxType {
        VAT, GST, SALES_TAX, INCOME_TAX, NONE
    }

    /**
     * Check if this tax jurisdiction is currently effective
     */
    public boolean isEffective() {
        LocalDate now = LocalDate.now();
        boolean afterEffective = effectiveDate == null || !now.isBefore(effectiveDate);
        boolean beforeExpiry = expiryDate == null || now.isBefore(expiryDate);
        return afterEffective && beforeExpiry;
    }

    /**
     * Check if this tax jurisdiction is effective for a specific date
     */
    public boolean isEffectiveOn(LocalDate date) {
        boolean afterEffective = effectiveDate == null || !date.isBefore(effectiveDate);
        boolean beforeExpiry = expiryDate == null || date.isBefore(expiryDate);
        return afterEffective && beforeExpiry;
    }
}