package com.ecommerce.dto.request;

import com.ecommerce.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * CheckoutRequest DTO — Payload for placing an order from the current cart.
 */
@Data
public class CheckoutRequest {

    @NotNull(message = "Shipping address ID is required")
    private Long shippingAddressId;

    private Long billingAddressId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String notes;
}
