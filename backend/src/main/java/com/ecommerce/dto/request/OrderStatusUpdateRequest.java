package com.ecommerce.dto.request;

import com.ecommerce.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * OrderStatusUpdateRequest DTO — Payload for updating order status (Admin).
 */
@Data
public class OrderStatusUpdateRequest {

    @NotNull(message = "New order status is required")
    private OrderStatus status;

    private String remarks;
}
