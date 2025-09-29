package com.inventory.util;

import com.inventory.dto.BarcodeResponseDto;
import com.inventory.entity.ItemBarcode;
import org.springframework.stereotype.Component;

@Component
public class BarcodeMapper {

    public BarcodeResponseDto toResponseDto(ItemBarcode barcode) {
        if (barcode == null) {
            return null;
        }

        return BarcodeResponseDto.builder()
                .id(barcode.getId())
                .tenantId(barcode.getTenantId())
                .variantId(barcode.getVariant() != null ? barcode.getVariant().getId() : null)
                .variantSku(barcode.getVariant() != null ? barcode.getVariant().getVariantSku() : null)
                .itemName(barcode.getVariant() != null && barcode.getVariant().getItem() != null 
                    ? barcode.getVariant().getItem().getName() : null)
                .barcode(barcode.getBarcode())
                .barcodeType(barcode.getBarcodeType())
                .barcodeTypeDisplay(barcode.getBarcodeType() != null ? barcode.getBarcodeType().getDisplayName() : null)
                .uomId(barcode.getUnitOfMeasure() != null ? barcode.getUnitOfMeasure().getId() : null)
                .uomName(barcode.getUnitOfMeasure() != null ? barcode.getUnitOfMeasure().getName() : null)
                .packLevel(barcode.getPackLevel())
                .packLevelDisplay(barcode.getPackLevel() != null ? barcode.getPackLevel().getDisplayName() : null)
                .isPrimary(barcode.getIsPrimary())
                .status(barcode.getStatus())
                .statusDisplay(barcode.getStatus() != null ? barcode.getStatus().getDisplayName() : null)
                .aiPayload(barcode.getAiPayload())
                .labelTemplateId(barcode.getLabelTemplateId())
                .createdAt(barcode.getCreatedAt())
                .updatedAt(barcode.getUpdatedAt())
                .displayFormat(barcode.getDisplayFormat())
                .isValidFormat(barcode.isValidFormat())
                .canBePrimary(barcode.canBePrimary())
                .requiresUomConversion(barcode.requiresUomConversion())
                .build();
    }
}