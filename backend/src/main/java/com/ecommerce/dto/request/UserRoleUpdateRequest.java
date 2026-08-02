package com.ecommerce.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

/**
 * UserRoleUpdateRequest DTO — Payload for updating user roles (Admin).
 */
@Data
public class UserRoleUpdateRequest {

    @NotEmpty(message = "At least one role must be specified")
    private Set<String> roles;
}
