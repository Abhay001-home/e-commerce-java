package com.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * ProductReviewSummaryDTO — aggregate rating breakdown and 1-5 star distribution map.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductReviewSummaryDTO {

    private Long productId;
    private BigDecimal averageRating;
    private Integer totalReviews;

    /** Map of star rating (1-5) to count of reviews. */
    private Map<Integer, Long> ratingDistribution;
}
