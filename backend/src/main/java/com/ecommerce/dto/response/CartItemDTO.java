package com.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * CartItemDTO — response payload for a single cart line item.
 *
 * Includes a product snapshot (name, image, slug) so the frontend doesn't
 * need a second API call to display cart items.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartItemDTO {

    private Long id;

    // ─── Product snapshot ─────────────────────────────────────────
    private Long productId;
    private String productName;
    private String productSlug;
    private String productImageUrl;

    // ─── Variant snapshot (null if no variant) ────────────────────
    private Long variantId;
    private String variantName;

    // ─── Pricing & Quantity ───────────────────────────────────────
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    /** Whether the current stock covers the requested quantity. */
    private Boolean inStock;

    /** Current available stock quantity — shown as "Only X left" warning. */
    private Integer availableStock;

    private Boolean savedForLater;
}
