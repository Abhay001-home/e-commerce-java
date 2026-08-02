package com.ecommerce.service;

import com.ecommerce.dto.request.CouponRequest;
import com.ecommerce.dto.response.CouponDTO;
import com.ecommerce.entity.Coupon;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CouponService Unit Tests")
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    private Coupon percentageCoupon;
    private Coupon fixedCoupon;
    private Coupon expiredCoupon;

    @BeforeEach
    void setUp() {
        percentageCoupon = Coupon.builder()
                .id(1L)
                .code("SUMMER20")
                .discountType(Coupon.DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("20.00"))
                .maxDiscountAmount(new BigDecimal("500.00"))
                .minOrderAmount(new BigDecimal("500.00"))
                .isActive(true)
                .usedCount(0)
                .build();

        fixedCoupon = Coupon.builder()
                .id(2L)
                .code("FLAT100")
                .discountType(Coupon.DiscountType.FIXED_AMOUNT)
                .discountValue(new BigDecimal("100.00"))
                .minOrderAmount(new BigDecimal("300.00"))
                .isActive(true)
                .usedCount(0)
                .build();

        expiredCoupon = Coupon.builder()
                .id(3L)
                .code("EXPIRED")
                .discountType(Coupon.DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("10.00"))
                .isActive(true)
                .endDate(LocalDateTime.now().minusDays(1))  // yesterday — expired
                .usedCount(0)
                .build();
    }

    // ─── Create Coupon ────────────────────────────────────────────

    @Test
    @DisplayName("Should create coupon successfully when code is unique")
    void createCoupon_Success() {
        CouponRequest request = new CouponRequest();
        request.setCode("NEWCODE");
        request.setDiscountType(Coupon.DiscountType.PERCENTAGE);
        request.setDiscountValue(new BigDecimal("10.00"));
        request.setIsActive(true);

        when(couponRepository.existsByCode("NEWCODE")).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenReturn(percentageCoupon);

        CouponDTO result = couponService.createCoupon(request);

        assertThat(result).isNotNull();
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when coupon code already exists")
    void createCoupon_DuplicateCode() {
        CouponRequest request = new CouponRequest();
        request.setCode("SUMMER20");
        request.setDiscountType(Coupon.DiscountType.PERCENTAGE);
        request.setDiscountValue(new BigDecimal("20.00"));

        when(couponRepository.existsByCode("SUMMER20")).thenReturn(true);

        assertThatThrownBy(() -> couponService.createCoupon(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");

        verify(couponRepository, never()).save(any());
    }

    // ─── Validate Coupon ──────────────────────────────────────────

    @Test
    @DisplayName("Should calculate PERCENTAGE discount with cap correctly")
    void validateCoupon_PercentageWithCap() {
        // 20% of 3000 = 600, but capped at 500
        when(couponRepository.findByCode("SUMMER20")).thenReturn(Optional.of(percentageCoupon));

        CouponDTO result = couponService.validateAndCalculateDiscount("SUMMER20", new BigDecimal("3000.00"));

        assertThat(result.getCalculatedDiscount()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("Should calculate PERCENTAGE discount without hitting cap")
    void validateCoupon_PercentageWithoutCap() {
        // 20% of 1000 = 200, under the 500 cap
        when(couponRepository.findByCode("SUMMER20")).thenReturn(Optional.of(percentageCoupon));

        CouponDTO result = couponService.validateAndCalculateDiscount("SUMMER20", new BigDecimal("1000.00"));

        assertThat(result.getCalculatedDiscount()).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("Should calculate FIXED_AMOUNT discount correctly")
    void validateCoupon_FixedAmount() {
        when(couponRepository.findByCode("FLAT100")).thenReturn(Optional.of(fixedCoupon));

        CouponDTO result = couponService.validateAndCalculateDiscount("FLAT100", new BigDecimal("500.00"));

        assertThat(result.getCalculatedDiscount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should throw BadRequestException for expired coupon")
    void validateCoupon_Expired() {
        when(couponRepository.findByCode("EXPIRED")).thenReturn(Optional.of(expiredCoupon));

        assertThatThrownBy(() ->
                couponService.validateAndCalculateDiscount("EXPIRED", new BigDecimal("500.00"))
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired or inactive");
    }

    @Test
    @DisplayName("Should throw BadRequestException when order amount is below minimum")
    void validateCoupon_BelowMinimumOrder() {
        // minOrderAmount = 500; we pass 200
        when(couponRepository.findByCode("SUMMER20")).thenReturn(Optional.of(percentageCoupon));

        assertThatThrownBy(() ->
                couponService.validateAndCalculateDiscount("SUMMER20", new BigDecimal("200.00"))
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Minimum order amount");
    }

    @Test
    @DisplayName("Should throw BadRequestException when coupon has exceeded usage limit")
    void validateCoupon_UsageLimitExceeded() {
        Coupon limitedCoupon = Coupon.builder()
                .id(4L)
                .code("LIMITED")
                .discountType(Coupon.DiscountType.FIXED_AMOUNT)
                .discountValue(new BigDecimal("50.00"))
                .isActive(true)
                .usageLimit(10)
                .usedCount(10)  // already at limit
                .build();

        when(couponRepository.findByCode("LIMITED")).thenReturn(Optional.of(limitedCoupon));

        assertThatThrownBy(() ->
                couponService.validateAndCalculateDiscount("LIMITED", new BigDecimal("500.00"))
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("usage limit");
    }

    @Test
    @DisplayName("Should throw BadRequestException when coupon code does not exist")
    void validateCoupon_NotFound() {
        when(couponRepository.findByCode("NONEXISTENT")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                couponService.validateAndCalculateDiscount("NONEXISTENT", new BigDecimal("500.00"))
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("does not exist");
    }
}
