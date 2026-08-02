package com.ecommerce.dto.request;

import com.ecommerce.entity.Coupon;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CouponRequest DTO — create or update a coupon (Admin only).
 */
@Data
public class CouponRequest {

    @NotBlank(message = "Coupon code is required")
    @Size(min = 3, max = 50, message = "Coupon code must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Coupon code must contain only uppercase letters, digits, hyphens, or underscores")
    private String code;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotNull(message = "Discount type is required")
    private Coupon.DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    private BigDecimal discountValue;

    /** Max discount cap — required for PERCENTAGE type; ignored for FIXED_AMOUNT. */
    @DecimalMin(value = "0.01", message = "Max discount amount must be greater than 0")
    private BigDecimal maxDiscountAmount;

    @DecimalMin(value = "0.00", message = "Min order amount cannot be negative")
    private BigDecimal minOrderAmount;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Min(value = 1, message = "Usage limit must be at least 1")
    private Integer usageLimit;

    private Boolean isActive = true;
}
