package com.ecommerce.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * CartItemRequest DTO — add a product to the cart.
 *
 * variantId is optional (null = product without variant selection).
 * quantity defaults to 1 if not supplied.
 */
@Data
public class CartItemRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    /** Optional — null for products without variants. */
    private Long variantId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 100, message = "Cannot add more than 100 units at once")
    private Integer quantity = 1;
}
