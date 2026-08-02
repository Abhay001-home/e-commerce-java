package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order entity — represents a customer order in the e-commerce system.
 *
 * Design Decisions:
 * - Uses orderNumber string (e.g. ORD-20260803-XXXX) for customer reference
 * - Address snapshot stored as a string/JSON so address edits after order placement
 *   do not modify historic order records
 * - State Pattern used via OrderStatus transition checks
 */
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_user", columnList = "user_id"),
        @Index(name = "idx_orders_number", columnList = "order_number", unique = true),
        @Index(name = "idx_orders_status", columnList = "order_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 100)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 30)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "shipping_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingAmount;

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "grand_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal grandTotal;

    @Column(name = "applied_coupon_code", length = 50)
    private String appliedCouponCode;

    /** Address snapshot captured at checkout time. */
    @Column(name = "shipping_address_snapshot", columnDefinition = "TEXT", nullable = false)
    private String shippingAddressSnapshot;

    @Column(name = "billing_address_snapshot", columnDefinition = "TEXT")
    private String billingAddressSnapshot;

    @Column(length = 500)
    private String notes;

    // ─── Relationships ────────────────────────────────────────────

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    @Builder.Default
    private List<OrderStatusHistory> statusHistories = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Shipment shipment;

    // ─── Audit ───────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ─── Helper methods ───────────────────────────────────────────

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void addStatusHistory(OrderStatusHistory history) {
        statusHistories.add(history);
        history.setOrder(this);
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
        if (payment != null) {
            payment.setOrder(this);
        }
    }

    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
        if (shipment != null) {
            shipment.setOrder(this);
        }
    }
}
