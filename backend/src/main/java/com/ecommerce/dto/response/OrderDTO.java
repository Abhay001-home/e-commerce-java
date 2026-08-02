package com.ecommerce.dto.response;

import com.ecommerce.entity.OrderStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OrderDTO — summary representation of an order for list views.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDTO {

    private Long id;
    private String orderNumber;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private OrderStatus orderStatus;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal shippingAmount;
    private BigDecimal discountAmount;
    private BigDecimal grandTotal;
    private String appliedCouponCode;
    private Integer totalItems;
    private PaymentDTO payment;
    private ShipmentDTO shipment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
