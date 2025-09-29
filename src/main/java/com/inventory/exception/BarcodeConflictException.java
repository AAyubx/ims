package com.inventory.exception;

/**
 * Exception thrown when barcode conflicts occur (duplicates, etc.)
 */
public class BarcodeConflictException extends RuntimeException {

    private String conflictingBarcode;
    private Long existingVariantId;

    public BarcodeConflictException(String message) {
        super(message);
    }

    public BarcodeConflictException(String message, String conflictingBarcode) {
        super(message);
        this.conflictingBarcode = conflictingBarcode;
    }

    public BarcodeConflictException(String message, String conflictingBarcode, Long existingVariantId) {
        super(message);
        this.conflictingBarcode = conflictingBarcode;
        this.existingVariantId = existingVariantId;
    }

    public String getConflictingBarcode() {
        return conflictingBarcode;
    }

    public Long getExistingVariantId() {
        return existingVariantId;
    }
}