package com.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * CartDTO — full cart response payload.
 *
 * Separates active items from "saved for later" items at the DTO layer
 * (both live in CartItem table, distinguished by savedForLater flag).
 *
 * Total breakdown matches the Decorator chain:
 *   subtotal → + tax → + shipping → − discount = grandTotal
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartDTO {

    private Long id;

    /** Active cart items (savedForLater = false). */
    private List<CartItemDTO> items;

    /** Items saved for later (savedForLater = true). */
    private List<CartItemDTO> savedForLaterItems;

    // ─── Totals breakdown ─────────────────────────────────────────

    private Integer totalItems;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal shippingAmount;
    private BigDecimal discountAmount;
    private BigDecimal grandTotal;

    // ─── Coupon ───────────────────────────────────────────────────

    private String appliedCouponCode;

    /** True if subtotal qualifies for free shipping (≥ ₹999). */
    private Boolean freeShipping;
}
