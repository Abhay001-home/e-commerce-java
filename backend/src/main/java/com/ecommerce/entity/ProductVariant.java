package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ProductVariant entity — size/color/storage variants of a product.
 *
 * Example: iPhone 14 → "128GB Black", "256GB Blue"
 * Each variant can have its own price and SKU.
 * Inventory can optionally be tracked at the variant level.
 */
@Entity
@Table(name = "product_variants", indexes = {
        @Index(name = "idx_variants_product", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "variant_name", nullable = false, length = 200)
    private String variantName;  // "Size: L | Color: Red"

    @Column(unique = true, length = 100)
    private String sku;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;    // overrides product price if set

    @Column(precision = 10, scale = 2)
    private BigDecimal mrp;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
