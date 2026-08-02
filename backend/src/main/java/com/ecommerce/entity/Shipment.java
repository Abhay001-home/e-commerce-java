package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Shipment entity — tracks physical shipment dispatch, carrier, and tracking.
 */
@Entity
@Table(name = "shipments", indexes = {
        @Index(name = "idx_shipments_order", columnList = "order_id", unique = true),
        @Index(name = "idx_shipments_tracking", columnList = "tracking_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "carrier_name", length = 100)
    private String carrierName;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipment_status", nullable = false, length = 30)
    @Builder.Default
    private ShipmentStatus shipmentStatus = ShipmentStatus.PENDING;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
