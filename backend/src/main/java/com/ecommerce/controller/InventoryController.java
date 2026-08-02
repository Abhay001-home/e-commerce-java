package com.ecommerce.controller;

import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.InventoryDTO;
import com.ecommerce.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * InventoryController — REST endpoints for inventory management and stock alerts.
 */
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Inventory Management", description = "Stock Management & Low Stock Alerts APIs (ADMIN)")
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "Get inventory for a product (ADMIN)")
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InventoryDTO>> getInventoryByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getInventoryByProductId(productId)));
    }

    @Operation(summary = "Update product stock quantity and low stock threshold (ADMIN)")
    @PutMapping("/product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InventoryDTO>> updateStock(
            @PathVariable Long productId,
            @RequestParam(required = false) Integer quantity,
            @RequestParam(required = false) Integer lowStockQty) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.updateStock(productId, quantity, lowStockQty),
                "Stock updated successfully"
        ));
    }

    @Operation(summary = "Get low stock alert list (ADMIN)")
    @GetMapping("/alerts/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<InventoryDTO>>> getLowStockAlerts() {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getLowStockAlerts()));
    }

    @Operation(summary = "Get out of stock list (ADMIN)")
    @GetMapping("/alerts/out-of-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<InventoryDTO>>> getOutOfStockAlerts() {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getOutOfStockAlerts()));
    }
}
