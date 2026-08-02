package com.ecommerce.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * ProductRequest DTO — create or update a product.
 *
 * Includes all product fields plus lists for images, variants, and specifications.
 * The service layer maps these to the entity hierarchy.
 */
@Data
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 500, message = "Name must not exceed 500 characters")
    private String name;

    private String description;

    @Size(max = 1000, message = "Short description must not exceed 1000 characters")
    private String shortDesc;

    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "MRP must be greater than 0")
    private BigDecimal mrp;

    private Long categoryId;

    private Long brandId;

    private Boolean isActive = true;

    private Boolean isFeatured = false;

    /** Initial stock quantity for inventory creation. */
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity = 0;

    @Min(value = 1, message = "Low stock threshold must be at least 1")
    private Integer lowStockQty = 10;

    private List<VariantRequest> variants;

    private List<SpecificationRequest> specifications;

    // ─── Nested DTOs ─────────────────────────────────────────────

    @Data
    public static class VariantRequest {
        @NotBlank(message = "Variant name is required")
        private String variantName;
        private String sku;
        private BigDecimal price;
        private BigDecimal mrp;
        private String imageUrl;
        private Boolean isActive = true;
    }

    @Data
    public static class SpecificationRequest {
        @NotBlank(message = "Spec key is required")
        private String specKey;
        @NotBlank(message = "Spec value is required")
        private String specValue;
        private Integer sortOrder = 0;
    }
}
