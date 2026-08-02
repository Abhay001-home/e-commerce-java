package com.ecommerce.service.pricing;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.Coupon;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * CouponDecorator — applies a coupon discount as the outermost pricing layer.
 *
 * Business Rules:
 * - PERCENTAGE: discount = subtotal * (discountValue / 100), capped at maxDiscountAmount
 * - FIXED_AMOUNT: discount = discountValue (flat rupee deduction, capped at grandTotal)
 * - grandTotal = grandTotal − discountAmount  (never goes below 0)
 *
 * This is the outermost decorator — it runs after tax and shipping are computed,
 * so the coupon reduces the total order payment amount (not the pre-tax base).
 *
 * If coupon is null (no coupon applied), discount remains 0 and grandTotal is unchanged.
 */
public class CouponDecorator implements CartPricingDecorator {

    private final CartPricingDecorator inner;
    private final Coupon coupon;   // null = no coupon

    public CouponDecorator(CartPricingDecorator inner, Coupon coupon) {
        this.inner = inner;
        this.coupon = coupon;
    }

    @Override
    public void applyPricing(Cart cart) {
        // Step 1: run inner chain (Base → Shipping → Tax)
        inner.applyPricing(cart);

        // Step 2: apply coupon discount
        if (coupon == null) {
            cart.setDiscountAmount(BigDecimal.ZERO);
            return;
        }

        BigDecimal discount = calculateDiscount(coupon, cart.getSubtotal());

        // Discount cannot exceed current grandTotal (prevents negative total)
        discount = discount.min(cart.getGrandTotal()).setScale(2, RoundingMode.HALF_UP);

        cart.setDiscountAmount(discount);
        cart.setGrandTotal(cart.getGrandTotal().subtract(discount).max(BigDecimal.ZERO));
    }

    // ─── Private helpers ──────────────────────────────────────────

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotal) {
        if (coupon.getDiscountType() == Coupon.DiscountType.PERCENTAGE) {
            BigDecimal discount = subtotal
                    .multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            // Apply cap if set
            if (coupon.getMaxDiscountAmount() != null) {
                discount = discount.min(coupon.getMaxDiscountAmount());
            }
            return discount;

        } else { // FIXED_AMOUNT
            return coupon.getDiscountValue().setScale(2, RoundingMode.HALF_UP);
        }
    }
}
