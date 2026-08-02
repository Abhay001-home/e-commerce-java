package com.ecommerce.controller;

import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.WishlistDTO;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * WishlistController — REST endpoints for the authenticated user's wishlist.
 *
 * User identity is extracted from JWT token (same IDOR-safe pattern as CartController).
 */
@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "Wishlist management for authenticated users")
@SecurityRequirement(name = "bearerAuth")
public class WishlistController {

    private final WishlistService wishlistService;
    private final UserRepository userRepository;

    @Operation(summary = "Get current user's wishlist")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WishlistDTO>> getWishlist(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(wishlistService.getWishlist(resolveUserId(auth))));
    }

    @Operation(summary = "Add product to wishlist")
    @PostMapping("/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WishlistDTO>> addToWishlist(
            Authentication auth,
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(
                wishlistService.addToWishlist(resolveUserId(auth), productId),
                "Product added to wishlist"
        ));
    }

    @Operation(summary = "Remove product from wishlist")
    @DeleteMapping("/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WishlistDTO>> removeFromWishlist(
            Authentication auth,
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(
                wishlistService.removeFromWishlist(resolveUserId(auth), productId),
                "Product removed from wishlist"
        ));
    }

    @Operation(summary = "Move product from wishlist to cart (adds 1 unit)")
    @PostMapping("/{productId}/move-to-cart")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WishlistDTO>> moveToCart(
            Authentication auth,
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(
                wishlistService.moveToCart(resolveUserId(auth), productId),
                "Product moved to cart"
        ));
    }

    // ─── Resolve logged-in user ID from JWT principal ─────────────

    private Long resolveUserId(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email))
                .getId();
    }
}
