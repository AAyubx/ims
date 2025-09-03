package com.inventory.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "Employee code is required")
    @Size(min = 3, max = 32, message = "Employee code must be between 3 and 32 characters")
    private String employeeCode;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 320, message = "Email must not exceed 320 characters")
    private String email;

    @NotBlank(message = "Display name is required")
    @Size(max = 255, message = "Display name must not exceed 255 characters")
    private String displayName;

    @NotEmpty(message = "At least one role must be assigned")
    private Set<Long> roleIds;

    @Size(min = 8, max = 128, message = "Initial password must be between 8 and 128 characters")
    private String initialPassword; // Optional, will generate if null

    private boolean mustChangePassword = true;
}