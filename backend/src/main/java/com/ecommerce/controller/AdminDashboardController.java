package com.ecommerce.controller;

import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.DashboardSummaryDTO;
import com.ecommerce.dto.response.SalesTrendDTO;
import com.ecommerce.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AdminDashboardController — Sales analytics, store metrics, best sellers, and revenue reports for Admin.
 */
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Analytics Dashboard", description = "Admin Sales Analytics, Revenue, Best Sellers, and Inventory Alerts APIs")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminDashboardController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Get overall dashboard summary metrics (ADMIN)")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDTO>> getDashboardSummary() {
        DashboardSummaryDTO summary = analyticsService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @Operation(summary = "Get periodic sales trend revenue data (ADMIN)")
    @GetMapping("/sales-trend")
    public ResponseEntity<ApiResponse<List<SalesTrendDTO>>> getSalesTrend() {
        List<SalesTrendDTO> trend = analyticsService.getSalesTrend();
        return ResponseEntity.ok(ApiResponse.success(trend));
    }
}
