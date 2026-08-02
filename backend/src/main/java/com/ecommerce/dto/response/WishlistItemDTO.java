package com.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * WishlistItemDTO — response payload for a single wishlist entry.
 *
 * Contains a product snapshot so the wishlist page renders without additional API calls.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WishlistItemDTO {

    private Long id;

    // ─── Product snapshot ─────────────────────────────────────────
    private Long productId;
    private String productName;
    private String productSlug;
    private String productImageUrl;
    private BigDecimal price;
    private BigDecimal mrp;
    private BigDecimal discountPct;

    /** Current stock availability — lets user see "Out of Stock" on wishlist. */
    private Boolean inStock;

    private LocalDateTime addedAt;
}
