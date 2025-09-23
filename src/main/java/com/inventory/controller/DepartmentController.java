package com.inventory.controller;

import com.inventory.entity.Department;
import com.inventory.entity.UserAccount;
import com.inventory.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<Page<Department>> getAllDepartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        // TODO: Get tenant ID from authentication
        Long tenantId = 1L; // Placeholder
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Department> departments = departmentService.getDepartmentsByTenant(tenantId, pageable);
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Department>> getActiveDepartments() {
        // TODO: Get tenant ID from authentication
        Long tenantId = 1L; // Placeholder
        
        List<Department> departments = departmentService.getActiveDepartmentsByTenant(tenantId);
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Long id) {
        // TODO: Get tenant ID from authentication
        Long tenantId = 1L; // Placeholder
        
        Department department = departmentService.getDepartmentByIdAndTenant(id, tenantId);
        return ResponseEntity.ok(department);
    }

    @PostMapping
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody Department department) {
        // TODO: Get current user from authentication
        UserAccount currentUser = new UserAccount(); // Placeholder
        currentUser.setId(1L);
        
        Department createdDepartment = departmentService.createDepartment(department, currentUser);
        return ResponseEntity.ok(createdDepartment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Department> updateDepartment(@PathVariable Long id, @Valid @RequestBody Department department) {
        // TODO: Get current user from authentication
        UserAccount currentUser = new UserAccount(); // Placeholder
        currentUser.setId(1L);
        
        Department updatedDepartment = departmentService.updateDepartment(id, department, currentUser);
        return ResponseEntity.ok(updatedDepartment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        // TODO: Get tenant ID and current user from authentication
        Long tenantId = 1L; // Placeholder
        UserAccount currentUser = new UserAccount(); // Placeholder
        currentUser.setId(1L);
        
        departmentService.deleteDepartment(id, tenantId, currentUser);
        return ResponseEntity.ok().build();
    }
}