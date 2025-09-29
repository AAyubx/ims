package com.inventory.entity;

/**
 * Enumeration of barcode lifecycle statuses.
 * Represents the current state of a barcode in the system.
 */
public enum BarcodeStatus {
    
    RESERVED("Reserved", "Barcode allocated but not yet active", false),
    ACTIVE("Active", "Barcode is active and in use", true),
    DEPRECATED("Deprecated", "Barcode replaced/retired but kept for historical reference", false),
    BLOCKED("Blocked", "Barcode blocked due to counterfeit/error - cannot be used", false);
    
    private final String displayName;
    private final String description;
    private final boolean isUsable;
    
    BarcodeStatus(String displayName, String description, boolean isUsable) {
        this.displayName = displayName;
        this.description = description;
        this.isUsable = isUsable;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if barcode can be used for operations (scanning, lookup, etc.)
     */
    public boolean isUsable() {
        return isUsable;
    }
    
    /**
     * Check if barcode can be scanned for inventory operations.
     */
    public boolean isScannable() {
        return this == ACTIVE;
    }
    
    /**
     * Check if barcode can be modified.
     */
    public boolean isModifiable() {
        return this == RESERVED || this == ACTIVE;
    }
    
    /**
     * Check if barcode can be deleted.
     */
    public boolean isDeletable() {
        return this == RESERVED || this == DEPRECATED;
    }
    
    /**
     * Get valid transition statuses from current status.
     */
    public BarcodeStatus[] getValidTransitions() {
        switch (this) {
            case RESERVED:
                return new BarcodeStatus[]{ACTIVE, BLOCKED};
            case ACTIVE:
                return new BarcodeStatus[]{DEPRECATED, BLOCKED};
            case DEPRECATED:
                return new BarcodeStatus[]{ACTIVE}; // Can be reactivated
            case BLOCKED:
                return new BarcodeStatus[]{}; // Terminal state
            default:
                return new BarcodeStatus[]{};
        }
    }
    
    /**
     * Check if transition to another status is valid.
     */
    public boolean canTransitionTo(BarcodeStatus newStatus) {
        for (BarcodeStatus validStatus : getValidTransitions()) {
            if (validStatus == newStatus) {
                return true;
            }
        }
        return false;
    }
}