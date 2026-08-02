package com.ecommerce.service.pricing;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;

import java.math.BigDecimal;

/**
 * BaseCartPricer — the innermost component of the Decorator chain.
 *
 * Computes subtotal from the sum of all active (non-savedForLater) item line totals.
 * Resets tax, shipping, and discount to zero (they are set by outer decorators).
 * Sets totalItems count from active item lines.
 *
 * This class has no dependency on any outer decorator — it is the concrete base.
 */
public class BaseCartPricer implements CartPricingDecorator {

    @Override
    public void applyPricing(Cart cart) {
        // Sum totalPrice of all active (not savedForLater) items
        BigDecimal subtotal = cart.getActiveItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setSubtotal(subtotal);
        cart.setTaxAmount(BigDecimal.ZERO);
        cart.setShippingAmount(BigDecimal.ZERO);
        cart.setDiscountAmount(BigDecimal.ZERO);
        cart.setTotalItems(cart.getActiveItems().size());

        // grandTotal will be overwritten by outer decorators
        cart.setGrandTotal(subtotal);
    }
}
