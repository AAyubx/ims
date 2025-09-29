package com.inventory.service;

import com.inventory.entity.BarcodeType;
import com.inventory.entity.PackLevel;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Service for validating barcode formats and implementing check digit algorithms.
 * Supports GS1 standards including GTIN validation with Mod-10 algorithm.
 */
@Service
public class BarcodeValidationService {

    // Regex patterns for different barcode formats
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^\\d+$");
    private static final Pattern UPC_A_PATTERN = Pattern.compile("^\\d{12}$");
    private static final Pattern UPC_E_PATTERN = Pattern.compile("^\\d{8}$");
    private static final Pattern EAN_13_PATTERN = Pattern.compile("^\\d{13}$");
    private static final Pattern EAN_8_PATTERN = Pattern.compile("^\\d{8}$");
    private static final Pattern ITF_14_PATTERN = Pattern.compile("^\\d{14}$");
    private static final Pattern CODE_128_PATTERN = Pattern.compile("^[\\x20-\\x7F]+$");
    private static final Pattern CODE_39_PATTERN = Pattern.compile("^[A-Z0-9\\-. $/+%]+$");

    /**
     * Validate barcode format for its type
     */
    public boolean isValidFormat(String barcode, BarcodeType type) {
        if (barcode == null || barcode.trim().isEmpty()) {
            return false;
        }

        switch (type) {
            case UPC_A:
                return UPC_A_PATTERN.matcher(barcode).matches();
            case UPC_E:
                return UPC_E_PATTERN.matcher(barcode).matches();
            case EAN_13:
                return EAN_13_PATTERN.matcher(barcode).matches();
            case EAN_8:
                return EAN_8_PATTERN.matcher(barcode).matches();
            case ITF_14:
                return ITF_14_PATTERN.matcher(barcode).matches();
            case CODE_128:
            case GS1_128:
                return CODE_128_PATTERN.matcher(barcode).matches() && barcode.length() >= 4 && barcode.length() <= 64;
            case CODE_39:
                return CODE_39_PATTERN.matcher(barcode).matches() && barcode.length() >= 4 && barcode.length() <= 64;
            case DATAMATRIX_GS1:
            case QR_GS1_LINK:
                return barcode.length() >= 4 && barcode.length() <= 64;
            default:
                return false;
        }
    }

    /**
     * Validate GTIN check digit using Mod-10 algorithm
     */
    public boolean isValidGTIN(String gtin) {
        if (gtin == null || !NUMERIC_PATTERN.matcher(gtin).matches()) {
            return false;
        }

        // Support GTIN-12 (UPC-A), GTIN-13 (EAN-13), GTIN-14 (ITF-14)
        if (gtin.length() != 12 && gtin.length() != 13 && gtin.length() != 14) {
            return false;
        }

        return isValidCheckDigit(gtin);
    }

    /**
     * Calculate GTIN check digit using Mod-10 algorithm
     */
    public int calculateGTINCheckDigit(String digits) {
        if (digits == null || !NUMERIC_PATTERN.matcher(digits).matches()) {
            throw new IllegalArgumentException("Input must contain only digits");
        }

        int sum = 0;
        boolean odd = true;

        // Process digits from right to left
        for (int i = digits.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(digits.charAt(i));
            sum += odd ? digit * 3 : digit;
            odd = !odd;
        }

        int checkDigit = (10 - (sum % 10)) % 10;
        return checkDigit;
    }

    /**
     * Validate check digit for GTIN codes
     */
    public boolean isValidCheckDigit(String gtin) {
        if (gtin == null || gtin.length() < 8) {
            return false;
        }

        try {
            String digits = gtin.substring(0, gtin.length() - 1);
            int expectedCheckDigit = Character.getNumericValue(gtin.charAt(gtin.length() - 1));
            int calculatedCheckDigit = calculateGTINCheckDigit(digits);

            return expectedCheckDigit == calculatedCheckDigit;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Generate complete GTIN with check digit
     */
    public String generateCompleteGTIN(String partialGTIN) {
        if (partialGTIN == null || !NUMERIC_PATTERN.matcher(partialGTIN).matches()) {
            throw new IllegalArgumentException("Partial GTIN must contain only digits");
        }

        int checkDigit = calculateGTINCheckDigit(partialGTIN);
        return partialGTIN + checkDigit;
    }

    /**
     * Validate barcode for specific pack level
     */
    public boolean isValidForPackLevel(BarcodeType type, PackLevel packLevel) {
        if (type == null || packLevel == null) {
            return false;
        }

        return type.supportsPackLevel(packLevel);
    }

    /**
     * Validate UPC-E compression (if needed for UPC-E barcodes)
     */
    public boolean isValidUPCE(String upce) {
        if (!UPC_E_PATTERN.matcher(upce).matches()) {
            return false;
        }

        // Basic validation - UPC-E has specific compression rules
        // This is a simplified validation
        return isValidCheckDigit(upce);
    }

    /**
     * Validate EAN-8 format
     */
    public boolean isValidEAN8(String ean8) {
        if (!EAN_8_PATTERN.matcher(ean8).matches()) {
            return false;
        }

        return isValidCheckDigit(ean8);
    }

    /**
     * Validate ITF-14 format (includes packaging indicator)
     */
    public boolean isValidITF14(String itf14) {
        if (!ITF_14_PATTERN.matcher(itf14).matches()) {
            return false;
        }

        // First digit is packaging indicator (1-9)
        char firstDigit = itf14.charAt(0);
        if (firstDigit < '1' || firstDigit > '9') {
            return false;
        }

        return isValidCheckDigit(itf14);
    }

    /**
     * Extract packaging indicator from ITF-14
     */
    public int extractPackagingIndicator(String itf14) {
        if (itf14 == null || itf14.length() != 14) {
            throw new IllegalArgumentException("ITF-14 must be exactly 14 digits");
        }

        return Character.getNumericValue(itf14.charAt(0));
    }

    /**
     * Validate GS1-128 Application Identifier format (basic validation)
     */
    public boolean isValidGS1128(String gs1128) {
        if (gs1128 == null || gs1128.length() < 4) {
            return false;
        }

        // GS1-128 starts with Application Identifiers in parentheses or uses FNC1
        return gs1128.startsWith("(") || gs1128.contains("\\x1D"); // FNC1 character
    }

    /**
     * Comprehensive barcode validation combining format and business rules
     */
    public ValidationResult validateBarcode(String barcode, BarcodeType type, PackLevel packLevel) {
        ValidationResult result = new ValidationResult();

        // Basic format validation
        if (!isValidFormat(barcode, type)) {
            result.setValid(false);
            result.addError("Invalid format for barcode type " + type.getDisplayName());
            return result;
        }

        // Pack level compatibility
        if (!isValidForPackLevel(type, packLevel)) {
            result.setValid(false);
            result.addError("Barcode type " + type.getDisplayName() + " is not compatible with pack level " + packLevel.getDisplayName());
            return result;
        }

        // GTIN-specific validation
        if (type.requiresCheckDigit()) {
            if (!isValidGTIN(barcode)) {
                result.setValid(false);
                result.addError("Invalid GTIN check digit");
                return result;
            }
        }

        // Type-specific validation
        switch (type) {
            case ITF_14:
                if (!isValidITF14(barcode)) {
                    result.setValid(false);
                    result.addError("Invalid ITF-14 format or packaging indicator");
                    return result;
                }
                break;
            case UPC_E:
                if (!isValidUPCE(barcode)) {
                    result.setValid(false);
                    result.addError("Invalid UPC-E format");
                    return result;
                }
                break;
            case EAN_8:
                if (!isValidEAN8(barcode)) {
                    result.setValid(false);
                    result.addError("Invalid EAN-8 format");
                    return result;
                }
                break;
            case GS1_128:
                if (!isValidGS1128(barcode)) {
                    result.setValid(false);
                    result.addError("Invalid GS1-128 format");
                    return result;
                }
                break;
        }

        result.setValid(true);
        return result;
    }

    /**
     * Result class for comprehensive validation
     */
    public static class ValidationResult {
        private boolean valid;
        private java.util.List<String> errors = new java.util.ArrayList<>();

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public java.util.List<String> getErrors() {
            return errors;
        }

        public void addError(String error) {
            this.errors.add(error);
        }

        public String getErrorMessage() {
            return String.join("; ", errors);
        }
    }
}