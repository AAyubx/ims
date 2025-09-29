package com.inventory.dto;

import com.inventory.entity.BarcodeStatus;
import com.inventory.entity.BarcodeType;
import com.inventory.entity.PackLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BarcodeResponseDto {

    private Long id;
    
    private Long tenantId;
    
    private Long variantId;
    
    private String variantSku;
    
    private String itemName;
    
    private String barcode;
    
    private BarcodeType barcodeType;
    
    private String barcodeTypeDisplay;
    
    private Long uomId;
    
    private String uomName;
    
    private PackLevel packLevel;
    
    private String packLevelDisplay;
    
    private Boolean isPrimary;
    
    private BarcodeStatus status;
    
    private String statusDisplay;
    
    private Map<String, Object> aiPayload;
    
    private Long labelTemplateId;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private String displayFormat;
    
    private Boolean isValidFormat;
    
    private Boolean canBePrimary;
    
    private Boolean requiresUomConversion;
}