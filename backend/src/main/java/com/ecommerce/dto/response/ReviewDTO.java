package com.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ReviewDTO — response representation of a product review.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewDTO {

    private Long id;
    private Long userId;
    private String userName;
    private Long productId;
    private String productName;
    private Integer rating;
    private String title;
    private String comment;
    private Boolean isVerifiedPurchase;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
