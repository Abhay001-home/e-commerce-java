package com.ecommerce.dto.response;

import com.ecommerce.entity.Coupon;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CouponDTO — response payload for coupon data.
 *
 * usageLimit and usedCount are included for Admin views.
 * For user-facing validation responses, only relevant fields are populated.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CouponDTO {

    private Long id;
    private String code;
    private String description;
    private Coupon.DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer usageLimit;
    private Integer usedCount;
    private Boolean isActive;
    private LocalDateTime createdAt;

    /** Calculated discount amount — populated during cart apply, not on list/get. */
    private BigDecimal calculatedDiscount;
}
