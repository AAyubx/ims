package com.inventory.dto;

import com.inventory.entity.BarcodeType;
import com.inventory.entity.PackLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBarcodeRequest {

    @NotBlank(message = "Barcode cannot be blank")
    @Size(min = 4, max = 64, message = "Barcode must be between 4 and 64 characters")
    private String barcode;

    @NotNull(message = "Barcode type is required")
    private BarcodeType barcodeType;

    private Long uomId;

    @Builder.Default
    private PackLevel packLevel = PackLevel.EACH;

    @Builder.Default
    private Boolean isPrimary = false;

    private Long labelTemplateId;
}