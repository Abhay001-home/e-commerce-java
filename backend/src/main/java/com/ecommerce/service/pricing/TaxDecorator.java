package com.ecommerce.service.pricing;

import com.ecommerce.entity.Cart;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * TaxDecorator — applies GST tax on the subtotal after inner pricing.
 *
 * Business Rules:
 * - GST rate: 18% on subtotal (pre-discount, tax-inclusive pricing model)
 * - Tax is computed on subtotal only — shipping and discount are tax-exempt
 *
 * TaxDecorator wraps ShippingDecorator (it is the third layer in the chain).
 * grandTotal = subtotal + shipping + tax  (discount applied by CouponDecorator on top)
 */
public class TaxDecorator implements CartPricingDecorator {

    /** GST rate — 18% expressed as a multiplier (18/100). */
    private static final BigDecimal GST_RATE = new BigDecimal("0.18");

    private final CartPricingDecorator inner;

    public TaxDecorator(CartPricingDecorator inner) {
        this.inner = inner;
    }

    @Override
    public void applyPricing(Cart cart) {
        // Step 1: run inner decorators (Base → Shipping)
        inner.applyPricing(cart);

        // Step 2: compute GST on subtotal
        BigDecimal tax = cart.getSubtotal()
                .multiply(GST_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        cart.setTaxAmount(tax);
        // grandTotal now = subtotal + shipping + tax
        cart.setGrandTotal(cart.getGrandTotal().add(tax));
    }
}
