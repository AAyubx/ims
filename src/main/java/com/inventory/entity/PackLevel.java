package com.inventory.entity;

/**
 * Enumeration of packaging levels for barcode assignment.
 * Represents the hierarchy from individual items to shipping units.
 */
public enum PackLevel {
    
    EACH("Each", "Individual item/unit", 1),
    INNER("Inner Pack", "Inner packaging (e.g., 6-pack, dozen)", 2),
    CASE("Case", "Case or carton containing multiple inners or eaches", 3),
    PALLET("Pallet", "Pallet containing multiple cases", 4);
    
    private final String displayName;
    private final String description;
    private final int hierarchyLevel;
    
    PackLevel(String displayName, String description, int hierarchyLevel) {
        this.displayName = displayName;
        this.description = description;
        this.hierarchyLevel = hierarchyLevel;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getHierarchyLevel() {
        return hierarchyLevel;
    }
    
    /**
     * Check if this pack level is higher in hierarchy than another.
     */
    public boolean isHigherThan(PackLevel other) {
        return this.hierarchyLevel > other.hierarchyLevel;
    }
    
    /**
     * Check if this pack level is lower in hierarchy than another.
     */
    public boolean isLowerThan(PackLevel other) {
        return this.hierarchyLevel < other.hierarchyLevel;
    }
    
    /**
     * Get the next higher pack level in hierarchy.
     */
    public PackLevel getNextHigher() {
        switch (this) {
            case EACH:
                return INNER;
            case INNER:
                return CASE;
            case CASE:
                return PALLET;
            case PALLET:
                return null; // No higher level
            default:
                return null;
        }
    }
    
    /**
     * Get the next lower pack level in hierarchy.
     */
    public PackLevel getNextLower() {
        switch (this) {
            case INNER:
                return EACH;
            case CASE:
                return INNER;
            case PALLET:
                return CASE;
            case EACH:
                return null; // No lower level
            default:
                return null;
        }
    }
    
    /**
     * Check if UoM conversion is required for this pack level.
     */
    public boolean requiresUomConversion() {
        return this != EACH;
    }
}