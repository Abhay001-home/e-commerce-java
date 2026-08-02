package com.ecommerce.service;

import com.ecommerce.dto.request.ReviewRequest;
import com.ecommerce.dto.response.ProductReviewSummaryDTO;
import com.ecommerce.dto.response.ReviewDTO;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Review;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ReviewRepository;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService Unit Tests")
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrderRepository orderRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User user;
    private Product product;
    private Review review;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("jane@example.com").firstName("Jane").lastName("Doe").build();

        product = Product.builder()
                .id(10L)
                .name("Smart Watch")
                .avgRating(BigDecimal.ZERO)
                .reviewCount(0)
                .build();

        review = Review.builder()
                .id(100L)
                .user(user)
                .product(product)
                .rating(5)
                .title("Great product!")
                .comment("Extremely satisfied with the build quality.")
                .isVerifiedPurchase(true)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should add review successfully and recalculate product rating")
    void addReview_Success() {
        ReviewRequest request = new ReviewRequest();
        request.setRating(5);
        request.setTitle("Great product!");
        request.setComment("Extremely satisfied.");

        when(reviewRepository.existsByUserIdAndProductId(1L, 10L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(orderRepository.findByUserId(eq(1L), any())).thenReturn(new PageImpl<>(Collections.emptyList()));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(reviewRepository.calculateAverageRating(10L)).thenReturn(new BigDecimal("5.00"));
        when(reviewRepository.countActiveReviewsByProductId(10L)).thenReturn(1);

        ReviewDTO result = reviewService.addReview(1L, 10L, request);

        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(product.getAvgRating()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(product.getReviewCount()).isEqualTo(1);

        verify(reviewRepository).save(any(Review.class));
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should throw BadRequestException if user already reviewed the product")
    void addReview_DuplicateReview() {
        when(reviewRepository.existsByUserIdAndProductId(1L, 10L)).thenReturn(true);

        ReviewRequest request = new ReviewRequest();
        request.setRating(4);

        assertThatThrownBy(() -> reviewService.addReview(1L, 10L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already submitted a review");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get product review summary distribution")
    void getProductReviewSummary_Success() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(reviewRepository.getRatingCountsByProductId(10L)).thenReturn(new ArrayList<>());

        ProductReviewSummaryDTO summary = reviewService.getProductReviewSummary(10L);

        assertThat(summary).isNotNull();
        assertThat(summary.getProductId()).isEqualTo(10L);
        assertThat(summary.getRatingDistribution()).hasSize(5);
    }
}
