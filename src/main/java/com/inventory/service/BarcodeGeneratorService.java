package com.inventory.service;

import com.inventory.entity.BarcodeType;
import com.inventory.entity.GS1Configuration;
import com.inventory.entity.PackLevel;
import com.inventory.repository.GS1ConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.security.SecureRandom;
import java.util.Optional;

/**
 * Service for generating barcodes including GTIN codes using GS1 standards.
 * Handles GTIN-13, GTIN-14, and other barcode generation with proper check digits.
 */
@Service
@Transactional
public class BarcodeGeneratorService {

    @Autowired
    private GS1ConfigurationRepository gs1ConfigurationRepository;

    @Autowired
    private BarcodeValidationService barcodeValidationService;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate GTIN-13 barcode using tenant's GS1 configuration
     */
    public String generateGTIN13(Long tenantId) {
        Optional<GS1Configuration> configOpt = gs1ConfigurationRepository.findBestConfigurationForGeneration(tenantId);
        
        if (!configOpt.isPresent()) {
            throw new IllegalStateException("No active GS1 configuration available for tenant: " + tenantId);
        }

        GS1Configuration config = configOpt.get();
        return config.generateGTIN13();
    }

    /**
     * Generate GTIN-14 (ITF-14) barcode using tenant's GS1 configuration
     */
    public String generateGTIN14(Long tenantId, int packagingIndicator) {
        if (packagingIndicator < 1 || packagingIndicator > 9) {
            throw new IllegalArgumentException("Packaging indicator must be 1-9");
        }

        Optional<GS1Configuration> configOpt = gs1ConfigurationRepository.findBestConfigurationForGeneration(tenantId);
        
        if (!configOpt.isPresent()) {
            throw new IllegalStateException("No active GS1 configuration available for tenant: " + tenantId);
        }

        GS1Configuration config = configOpt.get();
        return config.generateGTIN14(packagingIndicator);
    }

    /**
     * Generate barcode based on type and pack level
     */
    public GenerationResult generateBarcode(Long tenantId, BarcodeType type, PackLevel packLevel) {
        GenerationResult result = new GenerationResult();

        try {
            String barcode;
            
            switch (type) {
                case UPC_A:
                    barcode = generateUPCA(tenantId);
                    break;
                case EAN_13:
                    barcode = generateGTIN13(tenantId);
                    break;
                case ITF_14:
                    int packagingIndicator = getPackagingIndicatorForLevel(packLevel);
                    barcode = generateGTIN14(tenantId, packagingIndicator);
                    break;
                case UPC_E:
                    barcode = generateUPCE(tenantId);
                    break;
                case EAN_8:
                    barcode = generateEAN8(tenantId);
                    break;
                case CODE_128:
                    barcode = generateCode128(tenantId);
                    break;
                case GS1_128:
                    barcode = generateGS1128(tenantId, packLevel);
                    break;
                case DATAMATRIX_GS1:
                    barcode = generateDataMatrixGS1(tenantId, packLevel);
                    break;
                case QR_GS1_LINK:
                    barcode = generateQRGS1Link(tenantId, packLevel);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported barcode type: " + type);
            }

            result.setBarcode(barcode);
            result.setSuccess(true);
            result.setType(type);
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }

        return result;
    }

    /**
     * Generate UPC-A (12-digit GTIN)
     */
    private String generateUPCA(Long tenantId) {
        String gtin13 = generateGTIN13(tenantId);
        // Convert GTIN-13 to UPC-A by removing leading zero (if present)
        if (gtin13.startsWith("0")) {
            return gtin13.substring(1);
        }
        throw new IllegalStateException("Cannot generate UPC-A from this GS1 configuration");
    }

    /**
     * Generate UPC-E (8-digit compressed UPC)
     */
    private String generateUPCE(Long tenantId) {
        // For UPC-E, we need specific number system and manufacturer codes
        // This is a simplified implementation
        String manufacturerCode = String.format("%05d", secureRandom.nextInt(100000));
        String itemCode = String.format("%03d", secureRandom.nextInt(1000));
        String upce = "0" + manufacturerCode.substring(0, 2) + itemCode + manufacturerCode.substring(2, 5);
        
        int checkDigit = barcodeValidationService.calculateGTINCheckDigit(upce.substring(0, 7));
        return upce + checkDigit;
    }

    /**
     * Generate EAN-8 (8-digit barcode)
     */
    private String generateEAN8(Long tenantId) {
        // Generate 7-digit code and calculate check digit
        String digits = String.format("%07d", secureRandom.nextInt(10000000));
        int checkDigit = barcodeValidationService.calculateGTINCheckDigit(digits);
        return digits + checkDigit;
    }

    /**
     * Generate Code 128 barcode
     */
    private String generateCode128(Long tenantId) {
        // Generate alphanumeric code for Code 128
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            if (secureRandom.nextBoolean()) {
                sb.append((char) ('A' + secureRandom.nextInt(26)));
            } else {
                sb.append(secureRandom.nextInt(10));
            }
        }
        return sb.toString();
    }

    /**
     * Generate GS1-128 barcode with Application Identifiers
     */
    private String generateGS1128(Long tenantId, PackLevel packLevel) {
        String gtin13 = generateGTIN13(tenantId);
        
        // Create GS1-128 with GTIN Application Identifier (01)
        StringBuilder gs1128 = new StringBuilder();
        gs1128.append("(01)").append(gtin13);
        
        // Add additional AIs based on pack level
        if (packLevel != PackLevel.EACH) {
            gs1128.append("(37)").append(String.format("%08d", secureRandom.nextInt(100000000))); // Count of trade items
        }
        
        return gs1128.toString();
    }

    /**
     * Generate DataMatrix GS1 barcode
     */
    private String generateDataMatrixGS1(Long tenantId, PackLevel packLevel) {
        String gtin13 = generateGTIN13(tenantId);
        
        // DataMatrix can contain more data than linear barcodes
        StringBuilder dataMatrix = new StringBuilder();
        dataMatrix.append("(01)").append(gtin13);
        dataMatrix.append("(17)").append("251231"); // Expiry date (YYMMDD format)
        dataMatrix.append("(10)").append("LOT").append(String.format("%06d", secureRandom.nextInt(1000000))); // Lot number
        
        return dataMatrix.toString();
    }

    /**
     * Generate QR Code with GS1 Digital Link
     */
    private String generateQRGS1Link(Long tenantId, PackLevel packLevel) {
        String gtin13 = generateGTIN13(tenantId);
        
        // GS1 Digital Link format
        return String.format("https://id.gs1.org/01/%s", gtin13);
    }

    /**
     * Get packaging indicator based on pack level
     */
    private int getPackagingIndicatorForLevel(PackLevel packLevel) {
        switch (packLevel) {
            case EACH:
                return 0; // Should not be used for ITF-14, but default
            case INNER:
                return 1;
            case CASE:
                return 2;
            case PALLET:
                return 3;
            default:
                return 1;
        }
    }

    /**
     * Generate custom barcode with specific prefix
     */
    public String generateCustomBarcode(String prefix, int length) {
        if (prefix == null || prefix.isEmpty()) {
            throw new IllegalArgumentException("Prefix cannot be empty");
        }
        
        if (length <= prefix.length()) {
            throw new IllegalArgumentException("Length must be greater than prefix length");
        }
        
        StringBuilder barcode = new StringBuilder(prefix);
        int remainingLength = length - prefix.length();
        
        for (int i = 0; i < remainingLength; i++) {
            barcode.append(secureRandom.nextInt(10));
        }
        
        return barcode.toString();
    }

    /**
     * Check if tenant can generate more barcodes
     */
    public boolean canGenerateBarcode(Long tenantId, BarcodeType type) {
        if (!type.isGTIN()) {
            return true; // Non-GTIN barcodes don't have capacity limits
        }
        
        return gs1ConfigurationRepository.findBestConfigurationForGeneration(tenantId).isPresent();
    }

    /**
     * Get remaining capacity for GTIN generation
     */
    public long getRemainingGTINCapacity(Long tenantId) {
        return gs1ConfigurationRepository.findAvailableConfigurations(tenantId)
                .stream()
                .mapToLong(GS1Configuration::getRemainingCapacity)
                .sum();
    }

    /**
     * Result class for barcode generation
     */
    public static class GenerationResult {
        private boolean success;
        private String barcode;
        private BarcodeType type;
        private String errorMessage;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getBarcode() {
            return barcode;
        }

        public void setBarcode(String barcode) {
            this.barcode = barcode;
        }

        public BarcodeType getType() {
            return type;
        }

        public void setType(BarcodeType type) {
            this.type = type;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}