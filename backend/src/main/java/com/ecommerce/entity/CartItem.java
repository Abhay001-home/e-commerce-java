package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * CartItem entity — one line in a user's cart.
 *
 * Design Decisions:
 * - unitPrice is snapshotted from Product.price at add-time so price changes
 *   after adding don't silently change the cart total
 * - totalPrice = unitPrice × quantity (denormalized for display; recomputed on qty change)
 * - savedForLater flag keeps "save for later" in the same table (no extra join/table)
 * - ManyToOne to ProductVariant is nullable — products without variants have null here
 */
@Entity
@Table(name = "cart_items", indexes = {
        @Index(name = "idx_cart_items_cart", columnList = "cart_id"),
        @Index(name = "idx_cart_items_product", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Selected variant (Size, Color, etc.) — nullable for products without variants.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    /**
     * Price snapshotted at add-time — prevents silent total changes when product
     * price is updated by admin.
     */
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /**
     * totalPrice = unitPrice × quantity — denormalized for fast display.
     * Recalculated whenever quantity changes.
     */
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    /**
     * When true, this item is in the "Saved for Later" list and is excluded
     * from cart totals computation.
     */
    @Column(name = "saved_for_later")
    @Builder.Default
    private Boolean savedForLater = false;

    // ─── Helper methods ───────────────────────────────────────────

    /** Recalculates totalPrice from unitPrice and current quantity. */
    public void recalculateTotal() {
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
