package com.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * OrderItemDTO — response representation of an individual line item in an order.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderItemDTO {

    private Long id;
    private Long productId;
    private String productName;
    private String productSlug;
    private String productImageUrl;
    private String productSku;
    private Long variantId;
    private String variantName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
}
