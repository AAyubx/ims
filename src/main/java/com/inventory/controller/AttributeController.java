package com.inventory.controller;

import com.inventory.entity.AttributeDefinition;
import com.inventory.entity.AttributeSet;
import com.inventory.entity.UserAccount;
import com.inventory.service.AttributeService;
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
@RequestMapping("/api/attributes")
public class AttributeController {

    @Autowired
    private AttributeService attributeService;

    @GetMapping("/definitions")
    public ResponseEntity<Page<AttributeDefinition>> getAllAttributeDefinitions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        // TODO: Get tenant ID from authentication
        Long tenantId = 1L; // Placeholder
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AttributeDefinition> definitions = attributeService.getAttributeDefinitionsByTenant(tenantId, pageable);
        return ResponseEntity.ok(definitions);
    }

    @GetMapping("/definitions/{id}")
    public ResponseEntity<AttributeDefinition> getAttributeDefinitionById(@PathVariable Long id) {
        // TODO: Get tenant ID from authentication
        Long tenantId = 1L; // Placeholder
        
        AttributeDefinition definition = attributeService.getAttributeDefinitionByIdAndTenant(id, tenantId);
        return ResponseEntity.ok(definition);
    }

    @PostMapping("/definitions")
    public ResponseEntity<AttributeDefinition> createAttributeDefinition(@Valid @RequestBody AttributeDefinition definition) {
        // TODO: Get current user from authentication
        UserAccount currentUser = new UserAccount(); // Placeholder
        currentUser.setId(1L);
        
        AttributeDefinition created = attributeService.createAttributeDefinition(definition, currentUser);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/definitions/{id}")
    public ResponseEntity<AttributeDefinition> updateAttributeDefinition(@PathVariable Long id, @Valid @RequestBody AttributeDefinition definition) {
        // TODO: Get current user from authentication
        UserAccount currentUser = new UserAccount(); // Placeholder
        currentUser.setId(1L);
        
        AttributeDefinition updated = attributeService.updateAttributeDefinition(id, definition, currentUser);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/definitions/{id}")
    public ResponseEntity<Void> deleteAttributeDefinition(@PathVariable Long id) {
        // TODO: Get tenant ID and current user from authentication
        Long tenantId = 1L; // Placeholder
        UserAccount currentUser = new UserAccount(); // Placeholder
        currentUser.setId(1L);
        
        attributeService.deleteAttributeDefinition(id, tenantId, currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sets")
    public ResponseEntity<AttributeSet> createAttributeSet(@Valid @RequestBody AttributeSet attributeSet) {
        // TODO: Get current user from authentication
        UserAccount currentUser = new UserAccount(); // Placeholder
        currentUser.setId(1L);
        
        AttributeSet created = attributeService.createAttributeSet(attributeSet, currentUser);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/sets/category/{categoryId}")
    public ResponseEntity<List<AttributeSet>> getAttributeSetsByCategory(@PathVariable Long categoryId) {
        // TODO: Get tenant ID from authentication
        Long tenantId = 1L; // Placeholder
        
        List<AttributeSet> sets = attributeService.getAttributeSetsByCategory(tenantId, categoryId);
        return ResponseEntity.ok(sets);
    }
}