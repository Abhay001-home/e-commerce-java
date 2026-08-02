package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cart entity — represents a user's active shopping cart.
 *
 * Design Decisions:
 * - OneToOne with User (each user has exactly one persistent cart)
 * - Totals (subtotal, tax, shipping, discount, grandTotal) are stored as computed
 *   columns updated via the Decorator Pattern in CartService.recalculateTotals()
 * - savedForLater items remain in CartItems with savedForLater=true (list separation
 *   done at the DTO layer — no second table needed)
 * - appliedCouponCode is stored on cart; discount is recalculated fresh on each
 *   cart fetch to handle coupon expiry edge cases
 */
@Entity
@Table(name = "carts", indexes = {
        @Index(name = "idx_carts_user", columnList = "user_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** OneToOne with User — unique user_id FK ensures one cart per user. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * CartItems — ArrayList preserves insertion order.
     * orphanRemoval=true removes items when they are removed from the list.
     */
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    // ─── Computed totals (updated by CartService.recalculateTotals) ─────────

    /** Sum of (unitPrice × quantity) for all active (non-savedForLater) items. */
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    /** Tax amount applied on subtotal (18% GST via TaxDecorator). */
    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    /** Shipping charge (free ≥ ₹999 subtotal, else ₹79 via ShippingDecorator). */
    @Column(name = "shipping_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal shippingAmount = BigDecimal.ZERO;

    /** Coupon discount amount — computed by CouponDecorator. */
    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /** grandTotal = subtotal + tax + shipping − discount */
    @Column(name = "grand_total", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal grandTotal = BigDecimal.ZERO;

    /** Total count of distinct active item lines (not savedForLater). */
    @Column(name = "total_items")
    @Builder.Default
    private Integer totalItems = 0;

    /** Coupon code currently applied to this cart. Null = no coupon. */
    @Column(name = "applied_coupon_code", length = 50)
    private String appliedCouponCode;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ─── Helper methods ───────────────────────────────────────────

    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
    }

    /** Returns only items that are in the active cart (not saved for later). */
    public List<CartItem> getActiveItems() {
        return items.stream()
                .filter(i -> !Boolean.TRUE.equals(i.getSavedForLater()))
                .collect(java.util.stream.Collectors.toList());
    }

    /** Returns only items that have been saved for later. */
    public List<CartItem> getSavedItems() {
        return items.stream()
                .filter(i -> Boolean.TRUE.equals(i.getSavedForLater()))
                .collect(java.util.stream.Collectors.toList());
    }
}
