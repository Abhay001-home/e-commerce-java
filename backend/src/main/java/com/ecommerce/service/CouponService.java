package com.ecommerce.service;

import com.ecommerce.dto.request.CouponRequest;
import com.ecommerce.dto.response.CouponDTO;
import com.ecommerce.entity.Coupon;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CouponService — business logic for coupon management and validation.
 *
 * Design Decisions:
 * - Codes are normalized to UPPERCASE on create/validate to ensure case-insensitivity
 * - validateAndCalculateDiscount is a pure read — it does NOT increment usedCount
 *   (that happens in Phase 4 OrderService.placeOrder to avoid double-counting)
 * - incrementUsage is a separate @Transactional method called by OrderService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final CouponRepository couponRepository;

    // ─── Admin CRUD ───────────────────────────────────────────────

    @Transactional
    public CouponDTO createCoupon(CouponRequest request) {
        String code = request.getCode().toUpperCase();
        if (couponRepository.existsByCode(code)) {
            throw new BadRequestException("Coupon code '" + code + "' already exists");
        }
        validateRequest(request);

        Coupon coupon = Coupon.builder()
                .code(code)
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .minOrderAmount(request.getMinOrderAmount())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .usageLimit(request.getUsageLimit())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Coupon saved = couponRepository.save(coupon);
        log.info("Coupon created: {}", saved.getCode());
        return mapToDTO(saved);
    }

    @Transactional
    public CouponDTO updateCoupon(Long id, CouponRequest request) {
        Coupon coupon = findCouponById(id);
        validateRequest(request);

        String code = request.getCode().toUpperCase();
        // Allow same code; reject if code belongs to a different coupon
        if (!coupon.getCode().equals(code) && couponRepository.existsByCode(code)) {
            throw new BadRequestException("Coupon code '" + code + "' is already used by another coupon");
        }

        coupon.setCode(code);
        coupon.setDescription(request.getDescription());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
        coupon.setMinOrderAmount(request.getMinOrderAmount());
        coupon.setStartDate(request.getStartDate());
        coupon.setEndDate(request.getEndDate());
        coupon.setUsageLimit(request.getUsageLimit());
        if (request.getIsActive() != null) coupon.setIsActive(request.getIsActive());

        log.info("Coupon updated: {}", coupon.getCode());
        return mapToDTO(couponRepository.save(coupon));
    }

    @Transactional
    public void deleteCoupon(Long id) {
        Coupon coupon = findCouponById(id);
        couponRepository.delete(coupon);
        log.info("Coupon deleted: {}", coupon.getCode());
    }

    @Transactional(readOnly = true)
    public List<CouponDTO> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CouponDTO getCouponById(Long id) {
        return mapToDTO(findCouponById(id));
    }

    // ─── User-facing coupon validation ────────────────────────────

    /**
     * Validates a coupon code against the given order amount and returns the
     * calculated discount. Does NOT increment usedCount (see class-level docs).
     *
     * @param code        the coupon code (case-insensitive)
     * @param orderAmount the current cart subtotal to validate against minOrderAmount
     * @return CouponDTO with calculatedDiscount populated
     * @throws BadRequestException if the coupon is invalid for any reason
     */
    @Transactional(readOnly = true)
    public CouponDTO validateAndCalculateDiscount(String code, BigDecimal orderAmount) {
        String normalizedCode = code.toUpperCase();
        Coupon coupon = couponRepository.findByCode(normalizedCode)
                .orElseThrow(() -> new BadRequestException("Coupon '" + normalizedCode + "' does not exist"));

        assertCouponApplicable(coupon, orderAmount);

        BigDecimal discount = computeDiscount(coupon, orderAmount);
        CouponDTO dto = mapToDTO(coupon);
        dto.setCalculatedDiscount(discount);
        return dto;
    }

    /**
     * Fetches a validated, active coupon by code for internal use (e.g., CartService).
     * Throws BadRequestException if invalid.
     */
    @Transactional(readOnly = true)
    public Coupon getValidatedCoupon(String code, BigDecimal subtotal) {
        String normalizedCode = code.toUpperCase();
        Coupon coupon = couponRepository.findByCode(normalizedCode)
                .orElseThrow(() -> new BadRequestException("Coupon '" + normalizedCode + "' does not exist"));
        assertCouponApplicable(coupon, subtotal);
        return coupon;
    }

    /**
     * Increments the usage counter — called by OrderService on order placement.
     * Separated from validation to avoid incrementing on browse/apply.
     */
    @Transactional
    public void incrementUsage(String code) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "code", code));
        coupon.incrementUsage();
        couponRepository.save(coupon);
    }

    // ─── Private helpers ──────────────────────────────────────────

    private void assertCouponApplicable(Coupon coupon, BigDecimal orderAmount) {
        if (!coupon.isCurrentlyValid()) {
            throw new BadRequestException("Coupon '" + coupon.getCode() + "' is expired or inactive");
        }
        if (!coupon.isWithinUsageLimit()) {
            throw new BadRequestException("Coupon '" + coupon.getCode() + "' has reached its usage limit");
        }
        if (coupon.getMinOrderAmount() != null
                && orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new BadRequestException(
                    "Minimum order amount of ₹" + coupon.getMinOrderAmount() +
                    " required to use coupon '" + coupon.getCode() + "'"
            );
        }
    }

    private BigDecimal computeDiscount(Coupon coupon, BigDecimal subtotal) {
        if (coupon.getDiscountType() == Coupon.DiscountType.PERCENTAGE) {
            BigDecimal discount = subtotal
                    .multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (coupon.getMaxDiscountAmount() != null) {
                discount = discount.min(coupon.getMaxDiscountAmount());
            }
            return discount;
        } else {
            return coupon.getDiscountValue().setScale(2, RoundingMode.HALF_UP);
        }
    }

    private void validateRequest(CouponRequest request) {
        if (request.getDiscountType() == Coupon.DiscountType.PERCENTAGE) {
            if (request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BadRequestException("Percentage discount cannot exceed 100%");
            }
        }
        if (request.getEndDate() != null && request.getStartDate() != null
                && request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }
    }

    private Coupon findCouponById(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));
    }

    // ─── Mapping ──────────────────────────────────────────────────

    public CouponDTO mapToDTO(Coupon coupon) {
        return CouponDTO.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .minOrderAmount(coupon.getMinOrderAmount())
                .startDate(coupon.getStartDate())
                .endDate(coupon.getEndDate())
                .usageLimit(coupon.getUsageLimit())
                .usedCount(coupon.getUsedCount())
                .isActive(coupon.getIsActive())
                .createdAt(coupon.getCreatedAt())
                .build();
    }
}
