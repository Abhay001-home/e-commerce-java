package com.ecommerce.service.pricing;

import com.ecommerce.entity.Cart;

/**
 * CartPricingDecorator — strategy interface for the cart pricing Decorator pattern.
 *
 * The Decorator chain computes cart totals in a composable, extensible way:
 *
 *   CouponDecorator
 *     └── TaxDecorator
 *           └── ShippingDecorator
 *                 └── BaseCartPricer
 *
 * Each decorator calls the inner pricer first, then applies its own logic.
 * This means order matters:
 *   1. BaseCartPricer sets subtotal from item line totals
 *   2. ShippingDecorator sets shippingAmount based on subtotal
 *   3. TaxDecorator sets taxAmount on subtotal (GST is pre-discount)
 *   4. CouponDecorator sets discountAmount and final grandTotal
 *
 * To add a new pricing concern (e.g., loyalty points deduction), simply
 * create a new decorator implementing this interface — no changes to existing classes.
 */
public interface CartPricingDecorator {

    /**
     * Applies this pricing step to the cart, mutating totals in place.
     * Implementations must call the inner decorator before their own logic.
     *
     * @param cart the cart entity whose total fields will be updated
     */
    void applyPricing(Cart cart);
}
