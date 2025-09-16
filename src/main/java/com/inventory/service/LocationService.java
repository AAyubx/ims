package com.inventory.service;

import com.inventory.entity.Location;
import com.inventory.entity.Location.LocationStatus;
import com.inventory.entity.Location.LocationType;
import com.inventory.entity.LocationCurrency;
import com.inventory.entity.TaxJurisdiction;
import com.inventory.entity.Tenant;
import com.inventory.entity.UserAccount;
import com.inventory.repository.LocationRepository;
import com.inventory.repository.LocationCurrencyRepository;
import com.inventory.repository.TaxJurisdictionRepository;
import com.inventory.repository.TenantRepository;
import com.inventory.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LocationService {

    private final LocationRepository locationRepository;
    private final LocationCurrencyRepository locationCurrencyRepository;
    private final TaxJurisdictionRepository taxJurisdictionRepository;
    private final TenantRepository tenantRepository;
    private final UserAccountRepository userAccountRepository;

    /**
     * Create a new store location with geographical and hierarchy data
     */
    public Location createStore(CreateStoreRequest request) {
        log.info("Creating new store: {} for tenant: {}", request.getCode(), request.getTenantId());

        // Validate tenant exists
        Tenant tenant = tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + request.getTenantId()));

        // Validate unique code within tenant
        if (locationRepository.existsByTenant_IdAndCode(request.getTenantId(), request.getCode())) {
            throw new IllegalArgumentException("Location code already exists: " + request.getCode());
        }

        // Validate parent location if specified
        Location parentLocation = null;
        if (request.getParentLocationId() != null) {
            parentLocation = locationRepository.findById(request.getParentLocationId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent location not found: " + request.getParentLocationId()));
            
            // Ensure parent belongs to same tenant
            if (!parentLocation.getTenant().getId().equals(request.getTenantId())) {
                throw new IllegalArgumentException("Parent location must belong to the same tenant");
            }
        }

        // Validate store manager if specified
        UserAccount storeManager = null;
        if (request.getStoreManagerId() != null) {
            storeManager = userAccountRepository.findById(request.getStoreManagerId())
                    .orElseThrow(() -> new IllegalArgumentException("Store manager not found: " + request.getStoreManagerId()));
        }

        // Validate tax jurisdiction if specified
        TaxJurisdiction taxJurisdiction = null;
        if (request.getTaxJurisdictionId() != null) {
            taxJurisdiction = taxJurisdictionRepository.findById(request.getTaxJurisdictionId())
                    .orElseThrow(() -> new IllegalArgumentException("Tax jurisdiction not found: " + request.getTaxJurisdictionId()));
        }

        // Create the location
        Location location = new Location();
        location.setTenant(tenant);
        location.setCode(request.getCode());
        location.setName(request.getName());
        location.setType(request.getType());
        location.setStatus(LocationStatus.ACTIVE);

        // Set geographical data
        location.setAddressLine1(request.getAddressLine1());
        location.setAddressLine2(request.getAddressLine2());
        location.setCity(request.getCity());
        location.setStateProvince(request.getStateProvince());
        location.setPostalCode(request.getPostalCode());
        location.setCountryCode(request.getCountryCode());
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setTimezone(request.getTimezone());

        // Set relationships
        location.setParentLocation(parentLocation);
        location.setStoreManager(storeManager);
        location.setTaxJurisdiction(taxJurisdiction);

        // Set configuration JSON (for now as strings)
        location.setBusinessHoursJson(request.getBusinessHoursJson());
        location.setCapabilitiesJson(request.getCapabilitiesJson());

        // Save the location
        location = locationRepository.save(location);

        // Create default currency configuration if specified
        if (request.getPrimaryCurrencyCode() != null) {
            createPrimaryCurrency(location, request.getPrimaryCurrencyCode());
        }

        log.info("Successfully created store: {} (ID: {})", location.getCode(), location.getId());
        return location;
    }

    /**
     * Update store location data
     */
    public Location updateStore(Long locationId, UpdateStoreRequest request) {
        log.info("Updating store with ID: {}", locationId);

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + locationId));

        // Update basic fields
        if (request.getName() != null) {
            location.setName(request.getName());
        }
        if (request.getType() != null) {
            location.setType(request.getType());
        }
        if (request.getStatus() != null) {
            location.setStatus(request.getStatus());
        }

        // Update geographical data
        if (request.getAddressLine1() != null) {
            location.setAddressLine1(request.getAddressLine1());
        }
        if (request.getAddressLine2() != null) {
            location.setAddressLine2(request.getAddressLine2());
        }
        if (request.getCity() != null) {
            location.setCity(request.getCity());
        }
        if (request.getStateProvince() != null) {
            location.setStateProvince(request.getStateProvince());
        }
        if (request.getPostalCode() != null) {
            location.setPostalCode(request.getPostalCode());
        }
        if (request.getCountryCode() != null) {
            location.setCountryCode(request.getCountryCode());
        }
        if (request.getLatitude() != null) {
            location.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            location.setLongitude(request.getLongitude());
        }
        if (request.getTimezone() != null) {
            location.setTimezone(request.getTimezone());
        }

        return locationRepository.save(location);
    }

    /**
     * Get store by ID
     */
    @Transactional(readOnly = true)
    public Optional<Location> getStoreById(Long locationId) {
        return locationRepository.findById(locationId);
    }

    /**
     * Get all stores for a tenant
     */
    @Transactional(readOnly = true)
    public Page<Location> getStoresByTenant(Long tenantId, Pageable pageable) {
        return locationRepository.findByTenant_IdAndStatusNot(tenantId, LocationStatus.INACTIVE, pageable);
    }

    /**
     * Get stores by country
     */
    @Transactional(readOnly = true)
    public List<Location> getStoresByCountry(Long tenantId, String countryCode) {
        return locationRepository.findByTenant_IdAndCountryCodeAndStatus(tenantId, countryCode, LocationStatus.ACTIVE);
    }

    /**
     * Find stores within a geographical radius
     */
    @Transactional(readOnly = true)
    public List<Location> findStoresWithinRadius(Long tenantId, BigDecimal latitude, BigDecimal longitude, Double radiusKm) {
        return locationRepository.findLocationsWithinRadius(tenantId, latitude, longitude, radiusKm, LocationStatus.ACTIVE);
    }

    /**
     * Get store hierarchy (children of a parent)
     */
    @Transactional(readOnly = true)
    public List<Location> getStoreHierarchy(Long parentLocationId) {
        return locationRepository.findDirectChildren(parentLocationId);
    }

    /**
     * Create primary currency for a location
     */
    private void createPrimaryCurrency(Location location, String currencyCode) {
        LocationCurrency locationCurrency = new LocationCurrency();
        locationCurrency.setTenant(location.getTenant());
        locationCurrency.setLocation(location);
        locationCurrency.setCurrencyCode(currencyCode);
        locationCurrency.setIsPrimary(true);
        locationCurrency.setExchangeRate(BigDecimal.ONE);
        
        locationCurrencyRepository.save(locationCurrency);
        log.info("Created primary currency {} for location {}", currencyCode, location.getCode());
    }

    /**
     * Request DTO for creating a store
     */
    public static class CreateStoreRequest {
        private Long tenantId;
        private String code;
        private String name;
        private LocationType type;
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
     * Request DTO for updating a store
     */
    public static class UpdateStoreRequest {
        private String name;
        private LocationType type;
        private LocationStatus status;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String stateProvince;
        private String postalCode;
        private String countryCode;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String timezone;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public LocationType getType() { return type; }
        public void setType(LocationType type) { this.type = type; }

        public LocationStatus getStatus() { return status; }
        public void setStatus(LocationStatus status) { this.status = status; }

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
}