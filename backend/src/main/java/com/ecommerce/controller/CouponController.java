package com.ecommerce.controller;

import com.ecommerce.dto.request.CouponRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.CouponDTO;
import com.ecommerce.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * CouponController — Admin CRUD for coupons + public coupon validation endpoint.
 */
@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupon Management", description = "Admin CRUD for coupons and user-facing coupon validation")
public class CouponController {

    private final CouponService couponService;

    // ─── Public endpoints ─────────────────────────────────────────

    @Operation(summary = "Validate a coupon code against an order amount")
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<CouponDTO>> validateCoupon(
            @RequestParam String code,
            @RequestParam(defaultValue = "0") BigDecimal orderAmount) {
        CouponDTO dto = couponService.validateAndCalculateDiscount(code, orderAmount);
        return ResponseEntity.ok(ApiResponse.success(dto, "Coupon is valid"));
    }

    // ─── Admin endpoints ──────────────────────────────────────────

    @Operation(summary = "Get all coupons (ADMIN)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<CouponDTO>>> getAllCoupons() {
        return ResponseEntity.ok(ApiResponse.success(couponService.getAllCoupons()));
    }

    @Operation(summary = "Get coupon by ID (ADMIN)")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<CouponDTO>> getCouponById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(couponService.getCouponById(id)));
    }

    @Operation(summary = "Create a new coupon (ADMIN)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<CouponDTO>> createCoupon(
            @Valid @RequestBody CouponRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(couponService.createCoupon(request), "Coupon created successfully"));
    }

    @Operation(summary = "Update coupon (ADMIN)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<CouponDTO>> updateCoupon(
            @PathVariable Long id,
            @Valid @RequestBody CouponRequest request) {
        return ResponseEntity.ok(ApiResponse.success(couponService.updateCoupon(id, request), "Coupon updated successfully"));
    }

    @Operation(summary = "Delete coupon (ADMIN)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Coupon deleted successfully"));
    }
}
