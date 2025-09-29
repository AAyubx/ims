package com.inventory.entity;

/**
 * Enumeration of supported barcode symbologies.
 * Covers both 1D (linear) and 2D barcode types with GS1 standards support.
 */
public enum BarcodeType {
    
    // 1D Linear Barcodes - GTIN-based
    UPC_A("UPC-A", "Universal Product Code - 12 digits", 12, true),
    UPC_E("UPC-E", "Universal Product Code - Compressed 8 digits", 8, true),
    EAN_13("EAN-13", "European Article Number - 13 digits", 13, true),
    EAN_8("EAN-8", "European Article Number - 8 digits", 8, true),
    ITF_14("ITF-14", "Interleaved Two of Five - 14 digits (GTIN-14)", 14, true),
    
    // 1D Linear Barcodes - General Purpose
    CODE_128("Code 128", "High-density alphanumeric barcode", -1, false),
    CODE_39("Code 39", "Alphanumeric barcode with limited character set", -1, false),
    GS1_128("GS1-128", "Code 128 with GS1 Application Identifiers", -1, true),
    
    // 2D Barcodes
    DATAMATRIX_GS1("DataMatrix GS1", "2D matrix barcode with GS1 AIs", -1, true),
    QR_GS1_LINK("QR GS1 Digital Link", "QR Code with GS1 Digital Link format", -1, true);
    
    private final String displayName;
    private final String description;
    private final int fixedLength; // -1 for variable length
    private final boolean isGS1Standard;
    
    BarcodeType(String displayName, String description, int fixedLength, boolean isGS1Standard) {
        this.displayName = displayName;
        this.description = description;
        this.fixedLength = fixedLength;
        this.isGS1Standard = isGS1Standard;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getFixedLength() {
        return fixedLength;
    }
    
    public boolean hasFixedLength() {
        return fixedLength > 0;
    }
    
    public boolean isGS1Standard() {
        return isGS1Standard;
    }
    
    public boolean isGTIN() {
        return this == UPC_A || this == UPC_E || this == EAN_13 || this == EAN_8 || this == ITF_14;
    }
    
    public boolean requiresCheckDigit() {
        return isGTIN();
    }
    
    public boolean supportsPackLevel(PackLevel packLevel) {
        switch (this) {
            case ITF_14:
                return packLevel == PackLevel.CASE || packLevel == PackLevel.PALLET;
            case UPC_E:
            case EAN_8:
                return packLevel == PackLevel.EACH;
            default:
                return true; // Most barcode types support all pack levels
        }
    }
    
    /**
     * Get recommended barcode types for a specific pack level.
     */
    public static BarcodeType[] getRecommendedForPackLevel(PackLevel packLevel) {
        switch (packLevel) {
            case EACH:
                return new BarcodeType[]{UPC_A, EAN_13, UPC_E, EAN_8, CODE_128};
            case INNER:
                return new BarcodeType[]{EAN_13, CODE_128, GS1_128};
            case CASE:
                return new BarcodeType[]{ITF_14, GS1_128, CODE_128};
            case PALLET:
                return new BarcodeType[]{ITF_14, GS1_128, DATAMATRIX_GS1};
            default:
                return values();
        }
    }
}