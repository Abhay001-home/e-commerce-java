package com.ecommerce.service.pricing;

import com.ecommerce.entity.Cart;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * ShippingDecorator — applies shipping charges after the inner pricer.
 *
 * Business Rules:
 * - Free shipping when subtotal >= FREE_SHIPPING_THRESHOLD (₹999)
 * - Flat SHIPPING_CHARGE (₹79) otherwise
 *
 * ShippingDecorator wraps BaseCartPricer (it is the second layer).
 * It reads the subtotal already set by BaseCartPricer and updates grandTotal.
 */
public class ShippingDecorator implements CartPricingDecorator {

    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("999.00");
    private static final BigDecimal SHIPPING_CHARGE = new BigDecimal("79.00");

    private final CartPricingDecorator inner;

    public ShippingDecorator(CartPricingDecorator inner) {
        this.inner = inner;
    }

    @Override
    public void applyPricing(Cart cart) {
        // Step 1: run the inner pricer first (BaseCartPricer)
        inner.applyPricing(cart);

        // Step 2: apply shipping based on subtotal set by inner pricer
        BigDecimal subtotal = cart.getSubtotal();
        BigDecimal shipping = subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0
                ? BigDecimal.ZERO
                : SHIPPING_CHARGE;

        cart.setShippingAmount(shipping.setScale(2, RoundingMode.HALF_UP));
        // Update grandTotal (tax & discount are added/subtracted by outer decorators)
        cart.setGrandTotal(subtotal.add(shipping));
    }
}
