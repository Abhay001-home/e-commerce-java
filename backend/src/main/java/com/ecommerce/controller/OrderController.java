package com.ecommerce.controller;

import com.ecommerce.dto.request.CheckoutRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.OrderDetailDTO;
import com.ecommerce.dto.response.OrderDTO;
import com.ecommerce.dto.response.PagedResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.InvoicePdfService;
import com.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * OrderController — REST endpoints for customer order checkout, order listing, cancellation,
 * and PDF invoice downloading.
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Customer Order & Checkout APIs")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final InvoicePdfService invoicePdfService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Operation(summary = "Place order from current active cart (Checkout)")
    @PostMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> checkout(
            Authentication auth,
            @Valid @RequestBody CheckoutRequest request) {
        OrderDetailDTO order = orderService.checkout(resolveUserId(auth), request);
        return ResponseEntity.ok(ApiResponse.success(order, "Order placed successfully"));
    }

    @Operation(summary = "Get user's order history with pagination")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PagedResponse<OrderDTO>>> getUserOrders(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PagedResponse<OrderDTO> orders = orderService.getUserOrders(resolveUserId(auth), page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @Operation(summary = "Get detailed order information by order ID")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> getOrderDetail(
            Authentication auth,
            @PathVariable Long id) {
        OrderDetailDTO order = orderService.getOrderDetailForUser(resolveUserId(auth), id);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @Operation(summary = "Cancel an order")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> cancelOrder(
            Authentication auth,
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        OrderDetailDTO order = orderService.cancelOrder(resolveUserId(auth), id, reason);
        return ResponseEntity.ok(ApiResponse.success(order, "Order cancelled successfully"));
    }

    @Operation(summary = "Download PDF invoice for an order")
    @GetMapping("/{id}/invoice")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadInvoice(
            Authentication auth,
            @PathVariable Long id) {
        Long userId = resolveUserId(auth);
        Order order = orderRepository.findByIdAndUserIdWithDetails(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        byte[] pdfBytes = invoicePdfService.generateInvoicePdf(order);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + order.getOrderNumber() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    private Long resolveUserId(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email))
                .getId();
    }
}
