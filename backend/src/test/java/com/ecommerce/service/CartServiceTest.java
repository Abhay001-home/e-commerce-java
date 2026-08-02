package com.ecommerce.service;

import com.ecommerce.dto.request.CartItemRequest;
import com.ecommerce.dto.response.CartDTO;
import com.ecommerce.entity.*;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Unit Tests")
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private CouponService couponService;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Product product;
    private Inventory inventory;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@example.com").firstName("John").lastName("Doe").build();

        product = Product.builder()
                .id(10L)
                .name("Wireless Mouse")
                .slug("wireless-mouse")
                .price(new BigDecimal("999.00"))
                .isActive(true)
                .build();

        inventory = Inventory.builder()
                .id(1L)
                .product(product)
                .quantity(50)
                .lowStockQty(5)
                .build();

        product.setInventory(inventory);

        cart = Cart.builder()
                .id(1L)
                .user(user)
                .items(new ArrayList<>())
                .subtotal(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .shippingAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .grandTotal(BigDecimal.ZERO)
                .totalItems(0)
                .build();
    }

    // ─── Add Item ─────────────────────────────────────────────────

    @Test
    @DisplayName("Should add new item to empty cart")
    void addItem_NewItemToEmptyCart() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(10L);
        request.setQuantity(2);

        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartDTO result = cartService.addItem(1L, request);

        assertThat(result).isNotNull();
        // Cart now has 1 item line
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(2);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should increment quantity when same product already in cart")
    void addItem_ExistingProductIncrementsQuantity() {
        // Pre-populate cart with one item of the same product
        CartItem existingItem = CartItem.builder()
                .id(100L)
                .product(product)
                .quantity(1)
                .unitPrice(product.getPrice())
                .totalPrice(product.getPrice())
                .savedForLater(false)
                .build();
        cart.addItem(existingItem);

        CartItemRequest request = new CartItemRequest();
        request.setProductId(10L);
        request.setQuantity(2);

        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.addItem(1L, request);

        // Quantity should be incremented from 1 to 3
        assertThat(existingItem.getQuantity()).isEqualTo(3);
        // Still only 1 item line (no duplicate row)
        assertThat(cart.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("Should throw BadRequestException when product is inactive")
    void addItem_InactiveProduct() {
        product.setIsActive(false);

        CartItemRequest request = new CartItemRequest();
        request.setProductId(10L);
        request.setQuantity(1);

        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cartService.addItem(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not available");
    }

    @Test
    @DisplayName("Should throw BadRequestException when stock is insufficient")
    void addItem_InsufficientStock() {
        inventory.setQuantity(1); // only 1 in stock

        CartItemRequest request = new CartItemRequest();
        request.setProductId(10L);
        request.setQuantity(5); // requesting 5

        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cartService.addItem(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock");
    }

    // ─── Remove Item ──────────────────────────────────────────────

    @Test
    @DisplayName("Should remove item from cart")
    void removeItem_Success() {
        CartItem item = CartItem.builder()
                .id(200L)
                .product(product)
                .quantity(1)
                .unitPrice(product.getPrice())
                .totalPrice(product.getPrice())
                .savedForLater(false)
                .build();
        cart.addItem(item);

        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.removeItem(1L, 200L);

        assertThat(cart.getItems()).isEmpty();
    }

    // ─── Save For Later ───────────────────────────────────────────

    @Test
    @DisplayName("Should save item for later — sets savedForLater=true, excludes from totals")
    void saveForLater_Success() {
        CartItem item = CartItem.builder()
                .id(300L)
                .product(product)
                .quantity(1)
                .unitPrice(product.getPrice())
                .totalPrice(product.getPrice())
                .savedForLater(false)
                .build();
        cart.addItem(item);

        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartDTO result = cartService.saveForLater(1L, 300L);

        assertThat(item.getSavedForLater()).isTrue();
        // Active items should now be empty (1 item, all saved)
        assertThat(cart.getActiveItems()).isEmpty();
    }

    // ─── Apply Coupon ─────────────────────────────────────────────

    @Test
    @DisplayName("Should apply valid coupon to cart")
    void applyCoupon_Success() {
        Coupon coupon = Coupon.builder()
                .id(1L)
                .code("SAVE10")
                .discountType(Coupon.DiscountType.FIXED_AMOUNT)
                .discountValue(new BigDecimal("10.00"))
                .isActive(true)
                .usedCount(0)
                .build();

        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(couponService.getValidatedCoupon(eq("SAVE10"), any())).thenReturn(coupon);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartDTO result = cartService.applyCoupon(1L, "SAVE10");

        assertThat(cart.getAppliedCouponCode()).isEqualTo("SAVE10");
    }

    // ─── Clear Cart ───────────────────────────────────────────────

    @Test
    @DisplayName("Should clear all items from cart")
    void clearCart_Success() {
        CartItem item = CartItem.builder()
                .id(400L)
                .product(product)
                .quantity(1)
                .unitPrice(product.getPrice())
                .totalPrice(product.getPrice())
                .savedForLater(false)
                .build();
        cart.addItem(item);
        cart.setAppliedCouponCode("SOMECODE");

        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.clearCart(1L);

        assertThat(cart.getItems()).isEmpty();
        assertThat(cart.getAppliedCouponCode()).isNull();
        assertThat(cart.getGrandTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
