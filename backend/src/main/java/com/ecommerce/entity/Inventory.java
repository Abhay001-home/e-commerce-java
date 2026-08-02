package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Inventory entity — tracks stock levels for each product.
 *
 * Design Decisions:
 * - OneToOne with Product (product_id UNIQUE constraint)
 * - Optional OneToOne with ProductVariant for variant-level stock
 * - lowStockQty threshold triggers admin alerts (Phase 5)
 *
 * Data Structure: The inventory table acts as a HashMap<ProductId, StockLevel>
 * in relational form — O(1) lookup by product_id index.
 */
@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", unique = true)
    private ProductVariant variant;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    /**
     * Low stock alert threshold.
     * When quantity drops below this value, admin is alerted.
     */
    @Column(name = "low_stock_qty")
    @Builder.Default
    private Integer lowStockQty = 10;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ─── Helper methods ───────────────────────────────────────────

    public boolean isInStock() {
        return quantity > 0;
    }

    public boolean isLowStock() {
        return quantity > 0 && quantity <= lowStockQty;
    }

    public boolean isOutOfStock() {
        return quantity <= 0;
    }

    /**
     * Decrements stock — throws if insufficient quantity.
     * Called on order placement.
     */
    public void decrementStock(int qty) {
        if (this.quantity < qty) {
            throw new IllegalStateException(
                    "Insufficient stock. Available: " + this.quantity + ", Requested: " + qty
            );
        }
        this.quantity -= qty;
    }

    /**
     * Increments stock — called on order cancellation/return.
     */
    public void incrementStock(int qty) {
        this.quantity += qty;
    }
}
