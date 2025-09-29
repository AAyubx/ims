package com.inventory.controller;

import com.inventory.dto.*;
import com.inventory.entity.*;
import com.inventory.service.BarcodeGeneratorService;
import com.inventory.service.ItemBarcodeService;
import com.inventory.service.UnitOfMeasureService;
import com.inventory.util.BarcodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/catalog/barcodes")
public class BarcodeController {

    @Autowired
    private ItemBarcodeService itemBarcodeService;

    @Autowired
    private BarcodeGeneratorService barcodeGeneratorService;

    @Autowired
    private UnitOfMeasureService unitOfMeasureService;

    @Autowired
    private BarcodeMapper barcodeMapper;

    /**
     * Search and list barcodes with filtering
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BarcodeResponseDto>>> searchBarcodes(
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) Long variantId,
            @RequestParam(required = false) BarcodeType type,
            @RequestParam(required = false) BarcodeStatus status,
            @RequestParam(required = false) PackLevel packLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request) {

        try {
            Long tenantId = getCurrentTenantId(request);
            
            Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<ItemBarcode> barcodePage;

            // Handle different search scenarios
            if (barcode != null && !barcode.trim().isEmpty()) {
                // Search by barcode
                List<ItemBarcode> found = itemBarcodeService.searchActiveBarcodes(tenantId, barcode.trim());
                barcodePage = convertToPage(found, pageable);
            } else if (variantId != null) {
                // Get barcodes for specific variant
                List<ItemBarcode> found = itemBarcodeService.getVariantBarcodes(tenantId, variantId);
                barcodePage = convertToPage(found, pageable);
            } else if (status != null) {
                // Get barcodes by status
                barcodePage = itemBarcodeService.getBarcodesByStatus(tenantId, status, pageable);
            } else {
                // Get all scannable barcodes (default)
                List<ItemBarcode> found = itemBarcodeService.getScannableBarcodes(tenantId);
                barcodePage = convertToPage(found, pageable);
            }

            Page<BarcodeResponseDto> responsePage = barcodePage.map(barcodeMapper::toResponseDto);

            return ResponseEntity.ok(ApiResponse.success(responsePage));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to search barcodes", e.getMessage()));
        }
    }

    /**
     * Get barcode by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BarcodeResponseDto>> getBarcodeById(
            @PathVariable Long id,
            HttpServletRequest request) {

        try {
            Long tenantId = getCurrentTenantId(request);
            ItemBarcode barcode = itemBarcodeService.getBarcode(tenantId, id);
            BarcodeResponseDto response = barcodeMapper.toResponseDto(barcode);
            
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get barcode", e.getMessage()));
        }
    }

    /**
     * Create barcode for variant
     */
    @PostMapping("/variants/{variantId}")
    public ResponseEntity<ApiResponse<BarcodeResponseDto>> createBarcode(
            @PathVariable Long variantId,
            @Valid @RequestBody CreateBarcodeRequest request,
            HttpServletRequest httpRequest) {

        try {
            Long tenantId = getCurrentTenantId(httpRequest);
            Long userId = getCurrentUserId(httpRequest);

            UnitOfMeasure uom = null;
            if (request.getUomId() != null) {
                uom = unitOfMeasureService.getUnitOfMeasure(tenantId, request.getUomId());
            }

            ItemBarcode barcode = itemBarcodeService.createBarcode(
                    tenantId, 
                    variantId, 
                    request.getBarcode(), 
                    request.getBarcodeType(),
                    request.getPackLevel(), 
                    uom, 
                    userId
            );

            barcode.setLabelTemplateId(request.getLabelTemplateId());

            if (request.getIsPrimary()) {
                barcode = itemBarcodeService.setPrimary(tenantId, barcode.getId(), userId);
            }

            // Activate the barcode
            barcode = itemBarcodeService.activateBarcode(tenantId, barcode.getId(), userId);

            BarcodeResponseDto response = barcodeMapper.toResponseDto(barcode);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Barcode created successfully", response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create barcode", e.getMessage()));
        }
    }

    /**
     * Update barcode
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<BarcodeResponseDto>> updateBarcode(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBarcodeRequest request,
            HttpServletRequest httpRequest) {

        try {
            Long tenantId = getCurrentTenantId(httpRequest);
            Long userId = getCurrentUserId(httpRequest);

            UnitOfMeasure uom = null;
            if (request.getUomId() != null) {
                uom = unitOfMeasureService.getUnitOfMeasure(tenantId, request.getUomId());
            }

            ItemBarcode barcode = itemBarcodeService.updateBarcode(
                    tenantId, 
                    id, 
                    request.getBarcode(), 
                    request.getBarcodeType(),
                    request.getPackLevel(), 
                    uom, 
                    userId
            );

            if (request.getLabelTemplateId() != null) {
                barcode.setLabelTemplateId(request.getLabelTemplateId());
            }

            if (request.getIsPrimary() != null && request.getIsPrimary()) {
                barcode = itemBarcodeService.setPrimary(tenantId, barcode.getId(), userId);
            }

            if (request.getStatus() != null) {
                switch (request.getStatus()) {
                    case ACTIVE:
                        barcode = itemBarcodeService.activateBarcode(tenantId, id, userId);
                        break;
                    case DEPRECATED:
                        barcode = itemBarcodeService.deprecateBarcode(tenantId, id, userId);
                        break;
                    case BLOCKED:
                        barcode = itemBarcodeService.blockBarcode(tenantId, id, userId);
                        break;
                    case RESERVED:
                        // No action needed for RESERVED status
                        break;
                }
            }

            BarcodeResponseDto response = barcodeMapper.toResponseDto(barcode);
            
            return ResponseEntity.ok(ApiResponse.success("Barcode updated successfully", response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update barcode", e.getMessage()));
        }
    }

    /**
     * Generate barcode for variant
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<GeneratedBarcodeResponse>> generateBarcode(
            @Valid @RequestBody GenerateBarcodeRequest request,
            HttpServletRequest httpRequest) {

        try {
            Long tenantId = getCurrentTenantId(httpRequest);
            Long userId = getCurrentUserId(httpRequest);

            List<BarcodeResponseDto> generatedBarcodes = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            int successCount = 0;

            UnitOfMeasure uom = null;
            if (request.getUomId() != null) {
                uom = unitOfMeasureService.getUnitOfMeasure(tenantId, request.getUomId());
            }

            for (int i = 0; i < request.getCount(); i++) {
                try {
                    ItemBarcode barcode = itemBarcodeService.generateAndCreateBarcode(
                            tenantId,
                            request.getVariantId(),
                            request.getBarcodeType(),
                            request.getPackLevel(),
                            uom,
                            userId
                    );

                    if (request.getLabelTemplateId() != null) {
                        barcode.setLabelTemplateId(request.getLabelTemplateId());
                    }

                    if (request.getSetPrimary() && i == 0) {
                        barcode = itemBarcodeService.setPrimary(tenantId, barcode.getId(), userId);
                    }

                    barcode = itemBarcodeService.activateBarcode(tenantId, barcode.getId(), userId);
                    generatedBarcodes.add(barcodeMapper.toResponseDto(barcode));
                    successCount++;

                } catch (Exception e) {
                    errors.add("Failed to generate barcode " + (i + 1) + ": " + e.getMessage());
                }
            }

            long remainingCapacity = barcodeGeneratorService.getRemainingGTINCapacity(tenantId);

            GeneratedBarcodeResponse response = GeneratedBarcodeResponse.builder()
                    .success(successCount > 0)
                    .message(successCount + " barcode(s) generated successfully")
                    .barcodes(generatedBarcodes)
                    .successCount(successCount)
                    .failureCount(errors.size())
                    .errors(errors)
                    .requestedType(request.getBarcodeType())
                    .variantId(request.getVariantId())
                    .remainingCapacity(remainingCapacity)
                    .build();

            HttpStatus status = successCount > 0 ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
            
            return ResponseEntity.status(status)
                    .body(ApiResponse.success(response.getMessage(), response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to generate barcodes", e.getMessage()));
        }
    }

    /**
     * Delete barcode
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBarcode(
            @PathVariable Long id,
            HttpServletRequest request) {

        try {
            Long tenantId = getCurrentTenantId(request);
            itemBarcodeService.deleteBarcode(tenantId, id);
            
            return ResponseEntity.ok(ApiResponse.success("Barcode deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete barcode", e.getMessage()));
        }
    }

    /**
     * Get barcode statistics for tenant
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ItemBarcodeService.BarcodeStatistics>> getBarcodeStats(
            HttpServletRequest request) {

        try {
            Long tenantId = getCurrentTenantId(request);
            ItemBarcodeService.BarcodeStatistics stats = itemBarcodeService.getBarcodeStatistics(tenantId);
            
            return ResponseEntity.ok(ApiResponse.success(stats));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get barcode statistics", e.getMessage()));
        }
    }

    // Helper methods
    private Long getCurrentTenantId(HttpServletRequest request) {
        // This would typically come from authentication context
        // For now, return a default value - you can implement this based on your auth system
        return 1L; // Placeholder implementation
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        // This would typically come from authentication context  
        // For now, return a default value - you can implement this based on your auth system
        return 1L; // Placeholder implementation
    }

    private <T> Page<T> convertToPage(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        List<T> subList = list.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(subList, pageable, list.size());
    }
}