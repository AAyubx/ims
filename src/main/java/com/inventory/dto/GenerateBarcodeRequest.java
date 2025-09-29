package com.inventory.dto;

import com.inventory.entity.BarcodeType;
import com.inventory.entity.PackLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateBarcodeRequest {

    @NotNull(message = "Variant ID is required")
    private Long variantId;

    @NotNull(message = "Barcode type is required")
    private BarcodeType barcodeType;

    @Builder.Default
    private PackLevel packLevel = PackLevel.EACH;

    private Long uomId;

    @Min(value = 1, message = "Count must be at least 1")
    @Max(value = 100, message = "Count cannot exceed 100")
    @Builder.Default
    private Integer count = 1;

    @Builder.Default
    private Boolean setPrimary = true;

    private Long labelTemplateId;
}