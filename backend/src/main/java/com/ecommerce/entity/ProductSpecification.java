package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * ProductSpecification entity — key-value specification pairs for a product.
 *
 * Example:
 *   Display: 6.7" OLED
 *   Processor: A16 Bionic
 *   RAM: 6GB
 *   Battery: 4352 mAh
 *
 * Uses ArrayList in Product (ordered by sort_order) for consistent UI display.
 */
@Entity
@Table(name = "product_specifications", indexes = {
        @Index(name = "idx_specs_product", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSpecification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "spec_key", nullable = false, length = 200)
    private String specKey;

    @Column(name = "spec_value", nullable = false, length = 500)
    private String specValue;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;
}
