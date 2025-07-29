package com.example.projectlibary.dto.request;

import com.example.projectlibary.common.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRoleRequest {
    @NotNull
    private UserRole role;
}
