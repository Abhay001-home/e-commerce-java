package com.ecommerce.service;

import com.ecommerce.dto.request.ReviewRequest;
import com.ecommerce.dto.response.PagedResponse;
import com.ecommerce.dto.response.ProductReviewSummaryDTO;
import com.ecommerce.dto.response.ReviewDTO;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Review;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ReviewRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ReviewService — manages product reviews, star ratings, verified purchase checks,
 * and automatic denormalized Product avgRating/reviewCount recalculations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public ReviewDTO addReview(Long userId, Long productId, ReviewRequest request) {
        if (reviewRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new BadRequestException("You have already submitted a review for this product");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Check if verified purchase (user has a DELIVERED order containing this product)
        boolean isVerified = isVerifiedPurchase(userId, productId);

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .title(request.getTitle())
                .comment(request.getComment())
                .isVerifiedPurchase(isVerified)
                .isActive(true)
                .build();

        Review savedReview = reviewRepository.save(review);
        recalculateProductRating(productId);

        log.info("Review added for product {} by user {}: rating {}", productId, userId, request.getRating());
        return mapToDTO(savedReview);
    }

    @Transactional
    public ReviewDTO updateReview(Long userId, Long reviewId, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!review.getUser().getId().equals(userId)) {
            throw new BadRequestException("You are not authorized to edit this review");
        }

        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setComment(request.getComment());

        Review updatedReview = reviewRepository.save(review);
        recalculateProductRating(review.getProduct().getId());

        log.info("Review {} updated by user {}", reviewId, userId);
        return mapToDTO(updatedReview);
    }

    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!review.getUser().getId().equals(userId)) {
            throw new BadRequestException("You are not authorized to delete this review");
        }

        Long productId = review.getProduct().getId();
        reviewRepository.delete(review);
        recalculateProductRating(productId);

        log.info("Review {} deleted by user {}", reviewId, userId);
    }

    @Transactional
    public void adminDeleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        Long productId = review.getProduct().getId();
        reviewRepository.delete(review);
        recalculateProductRating(productId);

        log.info("Review {} deleted by Admin", reviewId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ReviewDTO> getProductReviews(Long productId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviewPage = reviewRepository.findByProductIdAndIsActiveTrue(productId, pageable);
        return PagedResponse.from(reviewPage.map(this::mapToDTO));
    }

    @Transactional(readOnly = true)
    public ProductReviewSummaryDTO getProductReviewSummary(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        List<Object[]> rawCounts = reviewRepository.getRatingCountsByProductId(productId);
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }
        for (Object[] row : rawCounts) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            distribution.put(rating, count);
        }

        return ProductReviewSummaryDTO.builder()
                .productId(productId)
                .averageRating(product.getAvgRating())
                .totalReviews(product.getReviewCount())
                .ratingDistribution(distribution)
                .build();
    }

    @Transactional(readOnly = true)
    public PagedResponse<ReviewDTO> getAllReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviewPage = reviewRepository.findAll(pageable);
        return PagedResponse.from(reviewPage.map(this::mapToDTO));
    }

    // ─── Rating Recalculation Helper ──────────────────────────────

    public void recalculateProductRating(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return;

        BigDecimal avg = reviewRepository.calculateAverageRating(productId);
        Integer count = reviewRepository.countActiveReviewsByProductId(productId);

        product.setAvgRating(avg != null ? avg.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        product.setReviewCount(count != null ? count : 0);

        productRepository.save(product);
        log.info("Recalculated rating for Product {}: avg={}, count={}", productId, product.getAvgRating(), product.getReviewCount());
    }

    // ─── Private Helpers ──────────────────────────────────────────

    private boolean isVerifiedPurchase(Long userId, Long productId) {
        Page<Order> userOrders = orderRepository.findByUserId(userId, PageRequest.of(0, 100));
        return userOrders.getContent().stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.DELIVERED)
                .flatMap(o -> o.getItems().stream())
                .anyMatch(item -> item.getProduct().getId().equals(productId));
    }

    public ReviewDTO mapToDTO(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFullName())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .rating(review.getRating())
                .title(review.getTitle())
                .comment(review.getComment())
                .isVerifiedPurchase(review.getIsVerifiedPurchase())
                .isActive(review.getIsActive())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
