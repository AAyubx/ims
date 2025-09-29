package com.inventory.exception;

/**
 * Exception thrown when barcode validation fails
 */
public class BarcodeValidationException extends RuntimeException {

    private String errorCode;
    private Object details;

    public BarcodeValidationException(String message) {
        super(message);
    }

    public BarcodeValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public BarcodeValidationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BarcodeValidationException(String message, String errorCode, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object getDetails() {
        return details;
    }
}