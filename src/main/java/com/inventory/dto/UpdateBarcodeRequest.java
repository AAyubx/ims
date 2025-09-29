package com.inventory.dto;

import com.inventory.entity.BarcodeStatus;
import com.inventory.entity.BarcodeType;
import com.inventory.entity.PackLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBarcodeRequest {

    @Size(min = 4, max = 64, message = "Barcode must be between 4 and 64 characters")
    private String barcode;

    private BarcodeType barcodeType;

    private Long uomId;

    private PackLevel packLevel;

    private Boolean isPrimary;

    private BarcodeStatus status;

    private Long labelTemplateId;
}