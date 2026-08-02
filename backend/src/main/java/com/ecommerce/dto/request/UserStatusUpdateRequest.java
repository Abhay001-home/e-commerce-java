package com.ecommerce.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * UserStatusUpdateRequest DTO — Payload for enabling/disabling a user account (Admin).
 */
@Data
public class UserStatusUpdateRequest {

    @NotNull(message = "Active status is required")
    private Boolean isActive;
}
