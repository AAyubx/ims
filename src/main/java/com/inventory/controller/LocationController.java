package com.inventory.controller;

import com.inventory.entity.Location;
import com.inventory.entity.Location.LocationType;
import com.inventory.service.LocationService;
import com.inventory.service.LocationService.CreateStoreRequest;
import com.inventory.service.LocationService.UpdateStoreRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/v1/locations")
@RequiredArgsConstructor
@Slf4j
@Validated
public class LocationController {

    private final LocationService locationService;

    /**
     * Create a new store location
     */
    @PostMapping("/stores")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<LocationResponse> createStore(@Valid @RequestBody CreateStoreDto request) {
        log.info("Creating store: {} for tenant: {}", request.getCode(), request.getTenantId());

        try {
            // Convert DTO to service request
            CreateStoreRequest serviceRequest = convertToCreateRequest(request);
            
            // Create the store
            Location location = locationService.createStore(serviceRequest);
            
            // Convert to response DTO
            LocationResponse response = convertToResponse(location);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create store: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error creating store", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update an existing store location
     */
    @PutMapping("/stores/{locationId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<LocationResponse> updateStore(@PathVariable Long locationId, 
                                                       @Valid @RequestBody UpdateStoreDto request) {
        log.info("Updating store with ID: {}", locationId);

        try {
            // Convert DTO to service request
            UpdateStoreRequest serviceRequest = convertToUpdateRequest(request);
            
            // Update the store
            Location location = locationService.updateStore(locationId, serviceRequest);
            
            // Convert to response DTO
            LocationResponse response = convertToResponse(location);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update store: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error updating store", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get store by ID
     */
    @GetMapping("/stores/{locationId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLERK')")
    public ResponseEntity<LocationResponse> getStore(@PathVariable Long locationId) {
        return locationService.getStoreById(locationId)
                .map(location -> ResponseEntity.ok(convertToResponse(location)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all stores for a tenant
     */
    @GetMapping("/stores")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLERK')")
    public ResponseEntity<Page<LocationResponse>> getStores(@RequestParam Long tenantId, Pageable pageable) {
        Page<Location> locations = locationService.getStoresByTenant(tenantId, pageable);
        Page<LocationResponse> response = locations.map(this::convertToResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Get stores by country
     */
    @GetMapping("/stores/by-country/{countryCode}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLERK')")
    public ResponseEntity<List<LocationResponse>> getStoresByCountry(@PathVariable String countryCode, 
                                                                   @RequestParam Long tenantId) {
        List<Location> locations = locationService.getStoresByCountry(tenantId, countryCode);
        List<LocationResponse> response = locations.stream()
                .map(this::convertToResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Find stores within a geographical radius
     */
    @GetMapping("/stores/nearby")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLERK')")
    public ResponseEntity<List<LocationResponse>> getNearbyStores(
            @RequestParam Long tenantId,
            @RequestParam @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude,
            @RequestParam @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude,
            @RequestParam(defaultValue = "50") Double radiusKm) {
        
        List<Location> locations = locationService.findStoresWithinRadius(tenantId, latitude, longitude, radiusKm);
        List<LocationResponse> response = locations.stream()
                .map(this::convertToResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Get store hierarchy (children of a parent)
     */
    @GetMapping("/stores/{parentId}/children")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLERK')")
    public ResponseEntity<List<LocationResponse>> getStoreHierarchy(@PathVariable Long parentId) {
        List<Location> locations = locationService.getStoreHierarchy(parentId);
        List<LocationResponse> response = locations.stream()
                .map(this::convertToResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Convert CreateStoreDto to service request
     */
    private CreateStoreRequest convertToCreateRequest(CreateStoreDto dto) {
        CreateStoreRequest request = new CreateStoreRequest();
        request.setTenantId(dto.getTenantId());
        request.setCode(dto.getCode());
        request.setName(dto.getName());
        request.setType(dto.getType());
        request.setAddressLine1(dto.getAddressLine1());
        request.setAddressLine2(dto.getAddressLine2());
        request.setCity(dto.getCity());
        request.setStateProvince(dto.getStateProvince());
        request.setPostalCode(dto.getPostalCode());
        request.setCountryCode(dto.getCountryCode());
        request.setLatitude(dto.getLatitude());
        request.setLongitude(dto.getLongitude());
        request.setTimezone(dto.getTimezone());
        request.setParentLocationId(dto.getParentLocationId());
        request.setStoreManagerId(dto.getStoreManagerId());
        request.setTaxJurisdictionId(dto.getTaxJurisdictionId());
        request.setBusinessHoursJson(dto.getBusinessHoursJson());
        request.setCapabilitiesJson(dto.getCapabilitiesJson());
        request.setPrimaryCurrencyCode(dto.getPrimaryCurrencyCode());
        return request;
    }

    /**
     * Convert UpdateStoreDto to service request
     */
    private UpdateStoreRequest convertToUpdateRequest(UpdateStoreDto dto) {
        UpdateStoreRequest request = new UpdateStoreRequest();
        request.setName(dto.getName());
        request.setType(dto.getType());
        request.setStatus(dto.getStatus());
        request.setAddressLine1(dto.getAddressLine1());
        request.setAddressLine2(dto.getAddressLine2());
        request.setCity(dto.getCity());
        request.setStateProvince(dto.getStateProvince());
        request.setPostalCode(dto.getPostalCode());
        request.setCountryCode(dto.getCountryCode());
        request.setLatitude(dto.getLatitude());
        request.setLongitude(dto.getLongitude());
        request.setTimezone(dto.getTimezone());
        return request;
    }

    /**
     * Convert Location entity to response DTO
     */
    private LocationResponse convertToResponse(Location location) {
        LocationResponse response = new LocationResponse();
        response.setId(location.getId());
        response.setTenantId(location.getTenant().getId());
        response.setCode(location.getCode());
        response.setName(location.getName());
        response.setType(location.getType());
        response.setStatus(location.getStatus());
        response.setAddressLine1(location.getAddressLine1());
        response.setAddressLine2(location.getAddressLine2());
        response.setCity(location.getCity());
        response.setStateProvince(location.getStateProvince());
        response.setPostalCode(location.getPostalCode());
        response.setCountryCode(location.getCountryCode());
        response.setLatitude(location.getLatitude());
        response.setLongitude(location.getLongitude());
        response.setTimezone(location.getTimezone());
        response.setParentLocationId(location.getParentLocation() != null ? location.getParentLocation().getId() : null);
        response.setStoreManagerId(location.getStoreManager() != null ? location.getStoreManager().getId() : null);
        response.setTaxJurisdictionId(location.getTaxJurisdiction() != null ? location.getTaxJurisdiction().getId() : null);
        response.setCreatedAt(location.getCreatedAt());
        response.setUpdatedAt(location.getUpdatedAt());
        return response;
    }

    /**
     * DTO for creating a store
     */
    public static class CreateStoreDto {
        @NotNull(message = "Tenant ID is required")
        private Long tenantId;

        @NotBlank(message = "Store code is required")
        private String code;

        @NotBlank(message = "Store name is required")
        private String name;

        @NotNull(message = "Store type is required")
        private LocationType type;

        private String addressLine1;
        private String addressLine2;
        private String city;
        private String stateProvince;
        private String postalCode;
        private String countryCode;

        @DecimalMin(value = "-90", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90", message = "Latitude must be between -90 and 90")
        private BigDecimal latitude;

        @DecimalMin(value = "-180", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180", message = "Longitude must be between -180 and 180")
        private BigDecimal longitude;

        private String timezone;
        private Long parentLocationId;
        private Long storeManagerId;
        private Long taxJurisdictionId;
        private String businessHoursJson;
        private String capabilitiesJson;
        private String primaryCurrencyCode;

        // Getters and setters
        public Long getTenantId() { return tenantId; }
        public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public LocationType getType() { return type; }
        public void setType(LocationType type) { this.type = type; }

        public String getAddressLine1() { return addressLine1; }
        public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

        public String getAddressLine2() { return addressLine2; }
        public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getStateProvince() { return stateProvince; }
        public void setStateProvince(String stateProvince) { this.stateProvince = stateProvince; }

        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

        public BigDecimal getLatitude() { return latitude; }
        public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

        public BigDecimal getLongitude() { return longitude; }
        public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }

        public Long getParentLocationId() { return parentLocationId; }
        public void setParentLocationId(Long parentLocationId) { this.parentLocationId = parentLocationId; }

        public Long getStoreManagerId() { return storeManagerId; }
        public void setStoreManagerId(Long storeManagerId) { this.storeManagerId = storeManagerId; }

        public Long getTaxJurisdictionId() { return taxJurisdictionId; }
        public void setTaxJurisdictionId(Long taxJurisdictionId) { this.taxJurisdictionId = taxJurisdictionId; }

        public String getBusinessHoursJson() { return businessHoursJson; }
        public void setBusinessHoursJson(String businessHoursJson) { this.businessHoursJson = businessHoursJson; }

        public String getCapabilitiesJson() { return capabilitiesJson; }
        public void setCapabilitiesJson(String capabilitiesJson) { this.capabilitiesJson = capabilitiesJson; }

        public String getPrimaryCurrencyCode() { return primaryCurrencyCode; }
        public void setPrimaryCurrencyCode(String primaryCurrencyCode) { this.primaryCurrencyCode = primaryCurrencyCode; }
    }

    /**
     * DTO for updating a store
     */
    public static class UpdateStoreDto {
        private String name;
        private LocationType type;
        private Location.LocationStatus status;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String stateProvince;
        private String postalCode;
        private String countryCode;

        @DecimalMin(value = "-90", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90", message = "Latitude must be between -90 and 90")
        private BigDecimal latitude;

        @DecimalMin(value = "-180", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180", message = "Longitude must be between -180 and 180")
        private BigDecimal longitude;

        private String timezone;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public LocationType getType() { return type; }
        public void setType(LocationType type) { this.type = type; }

        public Location.LocationStatus getStatus() { return status; }
        public void setStatus(Location.LocationStatus status) { this.status = status; }

        public String getAddressLine1() { return addressLine1; }
        public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

        public String getAddressLine2() { return addressLine2; }
        public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getStateProvince() { return stateProvince; }
        public void setStateProvince(String stateProvince) { this.stateProvince = stateProvince; }

        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

        public BigDecimal getLatitude() { return latitude; }
        public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

        public BigDecimal getLongitude() { return longitude; }
        public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }
    }

    /**
     * Response DTO for location data
     */
    public static class LocationResponse {
        private Long id;
        private Long tenantId;
        private String code;
        private String name;
        private LocationType type;
        private Location.LocationStatus status;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String stateProvince;
        private String postalCode;
        private String countryCode;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String timezone;
        private Long parentLocationId;
        private Long storeManagerId;
        private Long taxJurisdictionId;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getTenantId() { return tenantId; }
        public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public LocationType getType() { return type; }
        public void setType(LocationType type) { this.type = type; }

        public Location.LocationStatus getStatus() { return status; }
        public void setStatus(Location.LocationStatus status) { this.status = status; }

        public String getAddressLine1() { return addressLine1; }
        public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

        public String getAddressLine2() { return addressLine2; }
        public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getStateProvince() { return stateProvince; }
        public void setStateProvince(String stateProvince) { this.stateProvince = stateProvince; }

        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

        public BigDecimal getLatitude() { return latitude; }
        public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

        public BigDecimal getLongitude() { return longitude; }
        public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }

        public Long getParentLocationId() { return parentLocationId; }
        public void setParentLocationId(Long parentLocationId) { this.parentLocationId = parentLocationId; }

        public Long getStoreManagerId() { return storeManagerId; }
        public void setStoreManagerId(Long storeManagerId) { this.storeManagerId = storeManagerId; }

        public Long getTaxJurisdictionId() { return taxJurisdictionId; }
        public void setTaxJurisdictionId(Long taxJurisdictionId) { this.taxJurisdictionId = taxJurisdictionId; }

        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

        public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }
}