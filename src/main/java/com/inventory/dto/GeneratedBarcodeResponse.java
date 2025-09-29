package com.inventory.dto;

import com.inventory.entity.BarcodeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneratedBarcodeResponse {

    private boolean success;
    
    private String message;
    
    private List<BarcodeResponseDto> barcodes;
    
    private Integer successCount;
    
    private Integer failureCount;
    
    private List<String> errors;
    
    private BarcodeType requestedType;
    
    private Long variantId;
    
    private Long remainingCapacity;
}