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
 * Product entity — core of the e-commerce platform.
 *
 * Design Decisions:
 * - Uses BigDecimal for monetary values (no floating-point rounding errors)
 * - ManyToOne to Category and Brand (null-safe — product can exist without them)
 * - OneToMany to images, variants, specifications (ArrayList — ordered)
 * - OneToOne to Inventory (each product has exactly one inventory record)
 * - avgRating and reviewCount are denormalized for performance (updated on review save)
 * - soldCount is denormalized (incremented on order placement)
 * - FULLTEXT index on name/description for full-text search
 */
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_products_category", columnList = "category_id"),
        @Index(name = "idx_products_brand", columnList = "brand_id"),
        @Index(name = "idx_products_price", columnList = "price"),
        @Index(name = "idx_products_slug", columnList = "slug")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(nullable = false, unique = true, length = 500)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_desc", length = 1000)
    private String shortDesc;

    @Column(unique = true, length = 100)
    private String sku;

    /**
     * Selling price — BigDecimal for precision (monetary value).
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Maximum Retail Price — displayed with strikethrough when price < mrp.
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal mrp;

    /**
     * Discount percentage — computed for display; source of truth is price vs mrp.
     */
    @Column(name = "discount_pct", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPct = BigDecimal.ZERO;

    // ─── Relationships ────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    /**
     * Product images — ArrayList for ordered display (primary image first via is_primary flag).
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    /**
     * Product variants (Size, Color, etc.) — ArrayList for ordered display.
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    /**
     * Product specifications (key-value pairs) — ArrayList for ordered display.
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<ProductSpecification> specifications = new ArrayList<>();

    /**
     * Inventory — OneToOne with product (each product has one inventory record).
     */
    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Inventory inventory;

    // ─── Status & Stats ───────────────────────────────────────────

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    /**
     * Denormalized average rating — updated via ReviewService on each review.
     * Avoids expensive AVG() query on every product list fetch.
     */
    @Column(name = "avg_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal avgRating = BigDecimal.ZERO;

    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;

    /**
     * Denormalized sold count — incremented on order placement.
     * Used for "Best Seller" sorting (avoids JOIN with order_items).
     */
    @Column(name = "sold_count")
    @Builder.Default
    private Integer soldCount = 0;

    // ─── Audit ───────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ─── Helper methods ───────────────────────────────────────────

    /** Returns the primary image URL, or null if no images. */
    public String getPrimaryImageUrl() {
        return images.stream()
                .filter(ProductImage::getIsPrimary)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(images.isEmpty() ? null : images.get(0).getImageUrl());
    }

    /** Calculates and sets the discount percentage from price and MRP. */
    public void recalculateDiscount() {
        if (mrp != null && mrp.compareTo(BigDecimal.ZERO) > 0 && price != null) {
            BigDecimal discount = mrp.subtract(price)
                    .divide(mrp, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            this.discountPct = discount;
        }
    }

    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }

    public void addVariant(ProductVariant variant) {
        variants.add(variant);
        variant.setProduct(this);
    }

    public void addSpecification(ProductSpecification spec) {
        specifications.add(spec);
        spec.setProduct(this);
    }
}
