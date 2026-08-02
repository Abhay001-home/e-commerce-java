package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Coupon entity — represents a discount coupon that can be applied to a cart.
 *
 * Design Decisions:
 * - discountType enum (PERCENTAGE / FIXED_AMOUNT) avoids magic strings
 * - maxDiscountAmount caps PERCENTAGE coupons (e.g., 20% but max ₹500 off)
 * - minOrderAmount ensures a minimum cart value before coupon is valid
 * - usedCount is incremented on order placement (Phase 4) for rate-limiting
 */
@Entity
@Table(name = "coupons", indexes = {
        @Index(name = "idx_coupons_code", columnList = "code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique coupon code — case-insensitive by convention (stored UPPERCASE). */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(length = 255)
    private String description;

    /**
     * Discount type — controls how discountValue is interpreted.
     * PERCENTAGE: discount = subtotal * (discountValue / 100), capped at maxDiscountAmount.
     * FIXED_AMOUNT: discount = discountValue (flat rupee deduction).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    /** The discount value — percentage (0-100) or flat rupee amount. */
    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    /**
     * Maximum discount amount cap — only meaningful for PERCENTAGE coupons.
     * E.g., 20% off but max ₹500. Null = no cap.
     */
    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    /**
     * Minimum order amount required to apply this coupon.
     * Null = no minimum.
     */
    @Column(name = "min_order_amount", precision = 10, scale = 2)
    private BigDecimal minOrderAmount;

    /** Coupon becomes valid from this date-time. Null = always valid from past. */
    @Column(name = "start_date")
    private LocalDateTime startDate;

    /** Coupon expires at this date-time. Null = never expires. */
    @Column(name = "end_date")
    private LocalDateTime endDate;

    /**
     * Total number of times this coupon can be used across all users.
     * Null = unlimited.
     */
    @Column(name = "usage_limit")
    private Integer usageLimit;

    /**
     * Number of times this coupon has been used — incremented on order placement.
     * Prevents exceeding usageLimit via optimistic concurrency.
     */
    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // ─── Audit ───────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ─── Enum ─────────────────────────────────────────────────────

    public enum DiscountType {
        PERCENTAGE,
        FIXED_AMOUNT
    }

    // ─── Helper methods ───────────────────────────────────────────

    /** Returns true if the coupon is currently active and within its date range. */
    public boolean isCurrentlyValid() {
        if (!Boolean.TRUE.equals(isActive)) return false;
        LocalDateTime now = LocalDateTime.now();
        if (startDate != null && now.isBefore(startDate)) return false;
        if (endDate != null && now.isAfter(endDate)) return false;
        return true;
    }

    /** Returns true if the coupon has not exceeded its usage limit. */
    public boolean isWithinUsageLimit() {
        return usageLimit == null || usedCount < usageLimit;
    }

    /** Increments the usage counter — must be persisted by the caller. */
    public void incrementUsage() {
        this.usedCount++;
    }
}
