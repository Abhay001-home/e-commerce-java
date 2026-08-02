package com.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * WishlistDTO — full wishlist response payload.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WishlistDTO {

    private Long id;
    private List<WishlistItemDTO> items;
    private Integer totalItems;
}
