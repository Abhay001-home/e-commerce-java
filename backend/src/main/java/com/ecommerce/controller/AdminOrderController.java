package com.ecommerce.controller;

import com.ecommerce.dto.request.OrderStatusUpdateRequest;
import com.ecommerce.dto.request.ShipmentUpdateRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.OrderDetailDTO;
import com.ecommerce.dto.response.OrderDTO;
import com.ecommerce.dto.response.PagedResponse;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * AdminOrderController — REST endpoints for Admin order management and status transitions.
 */
@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@Tag(name = "Admin Order Management", description = "Admin Order management and shipment tracking APIs")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminOrderController {

    private final OrderService orderService;

    @Operation(summary = "Get all orders with pagination and optional status filter (ADMIN)")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<OrderDTO>>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PagedResponse<OrderDTO> orders = orderService.getAllOrders(status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @Operation(summary = "Get detailed order information by ID (ADMIN)")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderByIdForAdmin(id)));
    }

    @Operation(summary = "Update order status via State Pattern (ADMIN)")
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> updateOrderStatus(
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        String adminEmail = auth.getName();
        OrderDetailDTO updatedOrder = orderService.updateOrderStatus(id, request, adminEmail);
        return ResponseEntity.ok(ApiResponse.success(updatedOrder, "Order status updated to " + request.getStatus()));
    }

    @Operation(summary = "Update shipment tracking information (ADMIN)")
    @PutMapping("/{id}/shipment")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> updateShipment(
            @PathVariable Long id,
            @Valid @RequestBody ShipmentUpdateRequest request) {
        OrderDetailDTO updatedOrder = orderService.updateShipment(id, request);
        return ResponseEntity.ok(ApiResponse.success(updatedOrder, "Shipment tracking updated"));
    }
}
