package com.ecommerce.dto.response;

import com.ecommerce.entity.OrderStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OrderDetailDTO — comprehensive representation of an order including line items, address, payment, shipment, and history.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDetailDTO {

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
    private String shippingAddressSnapshot;
    private String billingAddressSnapshot;
    private String notes;

    private List<OrderItemDTO> items;
    private PaymentDTO payment;
    private ShipmentDTO shipment;
    private List<OrderStatusHistoryDTO> statusHistory;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
