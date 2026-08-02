package com.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ProductDTO — summary view for product listing pages.
 * Contains only the fields needed for cards/grids (performance optimization).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long id;
    private String name;
    private String slug;
    private String shortDesc;
    private String sku;
    private BigDecimal price;
    private BigDecimal mrp;
    private BigDecimal discountPct;
    private String primaryImageUrl;
    private String categoryName;
    private Long categoryId;
    private String brandName;
    private Long brandId;
    private BigDecimal avgRating;
    private Integer reviewCount;
    private Integer soldCount;
    private Boolean isActive;
    private Boolean isFeatured;
    private Boolean inStock;
    private Integer stockQuantity;
    private LocalDateTime createdAt;
}
