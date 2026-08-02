package com.ecommerce.controller;

import com.ecommerce.dto.request.ReviewRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.PagedResponse;
import com.ecommerce.dto.response.ProductReviewSummaryDTO;
import com.ecommerce.dto.response.ReviewDTO;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * ReviewController — public product review browsing & authenticated customer review submission.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Product Reviews", description = "Public product review browsing and customer review submission APIs")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @Operation(summary = "Get reviews for a product with pagination")
    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewDTO>>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<ReviewDTO> reviews = reviewService.getProductReviews(productId, page, size);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @Operation(summary = "Get rating distribution summary for a product")
    @GetMapping("/products/{productId}/reviews/summary")
    public ResponseEntity<ApiResponse<ProductReviewSummaryDTO>> getProductReviewSummary(
            @PathVariable Long productId) {
        ProductReviewSummaryDTO summary = reviewService.getProductReviewSummary(productId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @Operation(summary = "Add a review for a product (Auth Required)")
    @PostMapping("/products/{productId}/reviews")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<ReviewDTO>> addReview(
            Authentication auth,
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequest request) {
        Long userId = resolveUserId(auth);
        ReviewDTO review = reviewService.addReview(userId, productId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(review, "Review added successfully"));
    }

    @Operation(summary = "Update an existing review (Auth Required)")
    @PutMapping("/reviews/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<ReviewDTO>> updateReview(
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request) {
        Long userId = resolveUserId(auth);
        ReviewDTO review = reviewService.updateReview(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(review, "Review updated successfully"));
    }

    @Operation(summary = "Delete a review (Auth Required)")
    @DeleteMapping("/reviews/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            Authentication auth,
            @PathVariable Long id) {
        Long userId = resolveUserId(auth);
        reviewService.deleteReview(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Review deleted successfully"));
    }

    private Long resolveUserId(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email))
                .getId();
    }
}
