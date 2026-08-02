package com.ecommerce.controller;

import com.ecommerce.dto.request.CartItemRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.CartDTO;
import com.ecommerce.security.JwtUtil;
import com.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * CartController — REST endpoints for the authenticated user's cart.
 *
 * All endpoints require authentication (enforced via SecurityConfig anyRequest().authenticated()
 * plus @PreAuthorize at method level for explicit documentation).
 *
 * User identity is extracted from the JWT token via Authentication object
 * and resolved to userId via UserRepository — no userId in the request path
 * (prevents IDOR — a user can only access their own cart).
 */
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping Cart management for authenticated users")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;
    private final JwtUtil jwtUtil;
    private final com.ecommerce.repository.UserRepository userRepository;

    @Operation(summary = "Get current user's cart with totals breakdown")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CartDTO>> getCart(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart(resolveUserId(auth))));
    }

    @Operation(summary = "Add product to cart (or increment quantity if already present)")
    @PostMapping("/items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CartDTO>> addItem(
            Authentication auth,
            @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                cartService.addItem(resolveUserId(auth), request),
                "Item added to cart"
        ));
    }

    @Operation(summary = "Update quantity of a cart item")
    @PutMapping("/items/{itemId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CartDTO>> updateItemQuantity(
            Authentication auth,
            @PathVariable Long itemId,
            @RequestParam @Min(1) @Max(100) int quantity) {
        return ResponseEntity.ok(ApiResponse.success(
                cartService.updateItemQuantity(resolveUserId(auth), itemId, quantity),
                "Cart item updated"
        ));
    }

    @Operation(summary = "Remove item from cart")
    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CartDTO>> removeItem(
            Authentication auth,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(ApiResponse.success(
                cartService.removeItem(resolveUserId(auth), itemId),
                "Item removed from cart"
        ));
    }

    @Operation(summary = "Save a cart item for later")
    @PostMapping("/items/{itemId}/save-for-later")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CartDTO>> saveForLater(
            Authentication auth,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(ApiResponse.success(
                cartService.saveForLater(resolveUserId(auth), itemId),
                "Item saved for later"
        ));
    }

    @Operation(summary = "Move a saved-for-later item back to active cart")
    @PostMapping("/items/{itemId}/move-to-cart")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CartDTO>> moveToCart(
            Authentication auth,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(ApiResponse.success(
                cartService.moveToCart(resolveUserId(auth), itemId),
                "Item moved to cart"
        ));
    }

    @Operation(summary = "Apply a coupon code to the cart")
    @PostMapping("/coupon")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CartDTO>> applyCoupon(
            Authentication auth,
            @RequestParam String code) {
        return ResponseEntity.ok(ApiResponse.success(
                cartService.applyCoupon(resolveUserId(auth), code),
                "Coupon applied successfully"
        ));
    }

    @Operation(summary = "Remove the applied coupon from the cart")
    @DeleteMapping("/coupon")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CartDTO>> removeCoupon(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                cartService.removeCoupon(resolveUserId(auth)),
                "Coupon removed"
        ));
    }

    @Operation(summary = "Clear all items from the cart")
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> clearCart(Authentication auth) {
        cartService.clearCart(resolveUserId(auth));
        return ResponseEntity.ok(ApiResponse.success(null, "Cart cleared"));
    }

    // ─── Resolve logged-in user ID from JWT principal ─────────────

    private Long resolveUserId(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new com.ecommerce.exception.ResourceNotFoundException("User", "email", email))
                .getId();
    }
}
