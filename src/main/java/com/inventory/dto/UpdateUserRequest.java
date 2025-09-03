package com.inventory.dto;

import com.inventory.entity.UserAccount;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(max = 255, message = "Display name must not exceed 255 characters")
    private String displayName;

    @Size(min = 3, max = 32, message = "Employee code must be between 3 and 32 characters")
    private String employeeCode;

    private Set<Long> roleIds;

    private UserAccount.UserStatus status;

    private Boolean mustChangePassword;
}