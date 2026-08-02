package com.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ProductDetailDTO — full product details for the Product Detail page.
 * Includes all images, variants, specifications, and inventory info.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDTO {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String shortDesc;
    private String sku;
    private BigDecimal price;
    private BigDecimal mrp;
    private BigDecimal discountPct;

    // ── Category & Brand ─────────────────────────────────────────
    private Long categoryId;
    private String categoryName;
    private String categorySlug;
    private Long brandId;
    private String brandName;
    private String brandSlug;

    // ── Images ──────────────────────────────────────────────────
    private List<ImageDTO> images;

    // ── Variants ─────────────────────────────────────────────────
    private List<VariantDTO> variants;

    // ── Specifications ───────────────────────────────────────────
    private List<SpecDTO> specifications;

    // ── Inventory ────────────────────────────────────────────────
    private Boolean inStock;
    private Integer stockQuantity;

    // ── Stats ────────────────────────────────────────────────────
    private BigDecimal avgRating;
    private Integer reviewCount;
    private Integer soldCount;
    private Boolean isActive;
    private Boolean isFeatured;
    private LocalDateTime createdAt;

    // ─── Nested DTOs ─────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageDTO {
        private Long id;
        private String imageUrl;
        private String altText;
        private Boolean isPrimary;
        private Integer sortOrder;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantDTO {
        private Long id;
        private String variantName;
        private String sku;
        private BigDecimal price;
        private BigDecimal mrp;
        private String imageUrl;
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecDTO {
        private Long id;
        private String specKey;
        private String specValue;
        private Integer sortOrder;
    }
}
