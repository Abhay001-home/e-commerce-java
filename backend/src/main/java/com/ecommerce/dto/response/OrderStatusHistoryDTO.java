package com.ecommerce.dto.response;

import com.ecommerce.entity.OrderStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * OrderStatusHistoryDTO — response representation of order state transition history.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderStatusHistoryDTO {

    private Long id;
    private OrderStatus fromStatus;
    private OrderStatus toStatus;
    private String remarks;
    private String changedBy;
    private LocalDateTime createdAt;
}
