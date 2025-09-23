package com.inventory.controller;

import com.inventory.entity.Brand;
import com.inventory.entity.UserAccount;
import com.inventory.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/brands")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @GetMapping
    public ResponseEntity<Page<Brand>> getAllBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        // TODO: Get tenant ID from authentication
        Long tenantId = 1L; // Placeholder
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Brand> brands = brandService.getBrandsByTenant(tenantId, pageable);
        return ResponseEntity.ok(brands);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Brand>> getActiveBrands() {
        // TODO: Get tenant ID from authentication
        Long tenantId = 1L; // Placeholder
        
        List<Brand> brands = brandService.getActiveBrandsByTenant(tenantId);
        return ResponseEntity.ok(brands);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Brand> getBrandById(@PathVariable Long id) {
        // TODO: Get tenant ID from authentication
        Long tenantId = 1L; // Placeholder
        
        Brand brand = brandService.getBrandByIdAndTenant(id, tenantId);
        return ResponseEntity.ok(brand);
    }

    @PostMapping
    public ResponseEntity<Brand> createBrand(@Valid @RequestBody Brand brand) {
        // TODO: Get current user from authentication
        UserAccount currentUser = new UserAccount(); // Placeholder
        currentUser.setId(1L);
        
        Brand createdBrand = brandService.createBrand(brand, currentUser);
        return ResponseEntity.ok(createdBrand);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Brand> updateBrand(@PathVariable Long id, @Valid @RequestBody Brand brand) {
        // TODO: Get current user from authentication
        UserAccount currentUser = new UserAccount(); // Placeholder
        currentUser.setId(1L);
        
        Brand updatedBrand = brandService.updateBrand(id, brand, currentUser);
        return ResponseEntity.ok(updatedBrand);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        // TODO: Get tenant ID and current user from authentication
        Long tenantId = 1L; // Placeholder
        UserAccount currentUser = new UserAccount(); // Placeholder
        currentUser.setId(1L);
        
        brandService.deleteBrand(id, tenantId, currentUser);
        return ResponseEntity.ok().build();
    }
}