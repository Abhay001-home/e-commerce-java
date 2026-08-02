package com.ecommerce.service;

import com.ecommerce.dto.request.CartItemRequest;
import com.ecommerce.dto.response.CartDTO;
import com.ecommerce.dto.response.CartItemDTO;
import com.ecommerce.entity.*;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.*;
import com.ecommerce.service.pricing.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CartService — all cart operations for authenticated users.
 *
 * Design Decisions:
 * - getOrCreateCart ensures every authenticated user always has a cart (lazy init)
 * - recalculateTotals builds the Decorator chain fresh each time so business
 *   rule changes (tax rate, shipping threshold) only need updating in their
 *   respective decorator classes
 * - Price is snapshotted at add-time; we intentionally do NOT re-read product
 *   price on subsequent fetches (prevents silent changes; user sees what they added)
 * - saveForLater / moveToCart share the same CartItem row — only the flag changes
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final CouponService couponService;

    // ─── Get Cart ─────────────────────────────────────────────────

    @Transactional
    public CartDTO getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        recalculateTotals(cart);
        cartRepository.save(cart);
        return mapToDTO(cart);
    }

    // ─── Add Item ─────────────────────────────────────────────────

    @Transactional
    public CartDTO addItem(Long userId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new BadRequestException("Product is not available");
        }

        // Resolve variant (optional)
        ProductVariant variant = resolveVariant(product, request.getVariantId());

        // Check stock
        Inventory inventory = product.getInventory();
        if (inventory == null || inventory.getQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock for product: " + product.getName());
        }

        // Determine effective unit price (variant overrides product price if set)
        BigDecimal unitPrice = (variant != null && variant.getPrice() != null)
                ? variant.getPrice()
                : product.getPrice();

        // Check if same product+variant already in active cart
        Optional<CartItem> existingItem = findExistingActiveItem(cart, product.getId(),
                request.getVariantId());

        if (existingItem.isPresent()) {
            // Increment quantity
            CartItem item = existingItem.get();
            int newQty = item.getQuantity() + request.getQuantity();
            if (inventory.getQuantity() < newQty) {
                throw new BadRequestException("Only " + inventory.getQuantity() + " units available");
            }
            item.setQuantity(newQty);
            item.recalculateTotal();
        } else {
            // Add new item
            CartItem newItem = CartItem.builder()
                    .product(product)
                    .variant(variant)
                    .quantity(request.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(unitPrice.multiply(BigDecimal.valueOf(request.getQuantity())))
                    .savedForLater(false)
                    .build();
            cart.addItem(newItem);
        }

        recalculateTotals(cart);
        cartRepository.save(cart);
        log.info("Item added to cart for user {}: product {}", userId, product.getId());
        return mapToDTO(cart);
    }

    // ─── Update Item Quantity ─────────────────────────────────────

    @Transactional
    public CartDTO updateItemQuantity(Long userId, Long itemId, int quantity) {
        Cart cart = getCartForUser(userId);
        CartItem item = getCartItem(cart, itemId);

        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be at least 1. Use removeItem to delete.");
        }

        Inventory inventory = item.getProduct().getInventory();
        if (inventory == null || inventory.getQuantity() < quantity) {
            throw new BadRequestException(
                    "Insufficient stock. Available: " +
                    (inventory != null ? inventory.getQuantity() : 0)
            );
        }

        item.setQuantity(quantity);
        item.recalculateTotal();

        recalculateTotals(cart);
        cartRepository.save(cart);
        return mapToDTO(cart);
    }

    // ─── Remove Item ──────────────────────────────────────────────

    @Transactional
    public CartDTO removeItem(Long userId, Long itemId) {
        Cart cart = getCartForUser(userId);
        CartItem item = getCartItem(cart, itemId);
        cart.removeItem(item);

        recalculateTotals(cart);
        cartRepository.save(cart);
        log.info("Item {} removed from cart for user {}", itemId, userId);
        return mapToDTO(cart);
    }

    // ─── Save For Later / Move to Cart ────────────────────────────

    @Transactional
    public CartDTO saveForLater(Long userId, Long itemId) {
        Cart cart = getCartForUser(userId);
        CartItem item = getCartItem(cart, itemId);
        item.setSavedForLater(true);

        recalculateTotals(cart);
        cartRepository.save(cart);
        return mapToDTO(cart);
    }

    @Transactional
    public CartDTO moveToCart(Long userId, Long itemId) {
        Cart cart = getCartForUser(userId);
        CartItem item = getCartItem(cart, itemId);

        // Re-check stock before moving back
        Inventory inventory = item.getProduct().getInventory();
        if (inventory == null || inventory.getQuantity() < item.getQuantity()) {
            throw new BadRequestException(
                    "Insufficient stock to move item back to cart. Available: " +
                    (inventory != null ? inventory.getQuantity() : 0)
            );
        }

        item.setSavedForLater(false);
        recalculateTotals(cart);
        cartRepository.save(cart);
        return mapToDTO(cart);
    }

    // ─── Coupon Apply / Remove ────────────────────────────────────

    @Transactional
    public CartDTO applyCoupon(Long userId, String code) {
        Cart cart = getCartForUser(userId);

        // Validate coupon against current subtotal (full recompute first)
        recalculateTotals(cart);
        couponService.getValidatedCoupon(code, cart.getSubtotal());

        cart.setAppliedCouponCode(code.toUpperCase());
        recalculateTotals(cart);
        cartRepository.save(cart);
        log.info("Coupon '{}' applied to cart for user {}", code, userId);
        return mapToDTO(cart);
    }

    @Transactional
    public CartDTO removeCoupon(Long userId) {
        Cart cart = getCartForUser(userId);
        cart.setAppliedCouponCode(null);
        recalculateTotals(cart);
        cartRepository.save(cart);
        return mapToDTO(cart);
    }

    // ─── Clear Cart ───────────────────────────────────────────────

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getCartForUser(userId);
        cart.getItems().clear();
        cart.setAppliedCouponCode(null);
        cart.setSubtotal(BigDecimal.ZERO);
        cart.setTaxAmount(BigDecimal.ZERO);
        cart.setShippingAmount(BigDecimal.ZERO);
        cart.setDiscountAmount(BigDecimal.ZERO);
        cart.setGrandTotal(BigDecimal.ZERO);
        cart.setTotalItems(0);
        cartRepository.save(cart);
        log.info("Cart cleared for user {}", userId);
    }

    // ─── Recalculate Totals (Decorator chain) ────────────────────

    /**
     * Rebuilds the cart pricing Decorator chain and applies it to the cart.
     *
     * Chain:  CouponDecorator → TaxDecorator → ShippingDecorator → BaseCartPricer
     *
     * Resolves the applied coupon from DB (validates it is still valid).
     * If coupon is expired/deleted, it is silently removed from the cart.
     */
    public void recalculateTotals(Cart cart) {
        Coupon coupon = null;
        if (cart.getAppliedCouponCode() != null) {
            // Revalidate coupon — may have expired since last apply
            try {
                coupon = couponService.getValidatedCoupon(
                        cart.getAppliedCouponCode(), computeSubtotal(cart));
            } catch (BadRequestException e) {
                // Coupon no longer valid — silently remove
                log.warn("Coupon '{}' no longer valid on cart {}; removing",
                        cart.getAppliedCouponCode(), cart.getId());
                cart.setAppliedCouponCode(null);
            }
        }

        CartPricingDecorator pricer = new CouponDecorator(
                new TaxDecorator(
                        new ShippingDecorator(
                                new BaseCartPricer()
                        )
                ),
                coupon
        );

        pricer.applyPricing(cart);
    }

    // ─── Private helpers ──────────────────────────────────────────

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
                    Cart newCart = Cart.builder().user(user).build();
                    return cartRepository.save(newCart);
                });
    }

    private Cart getCartForUser(Long userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));
    }

    private CartItem getCartItem(Cart cart, Long itemId) {
        return cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));
    }

    private Optional<CartItem> findExistingActiveItem(Cart cart, Long productId, Long variantId) {
        return cart.getActiveItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId)
                        && ((variantId == null && i.getVariant() == null)
                            || (variantId != null && i.getVariant() != null
                                && i.getVariant().getId().equals(variantId))))
                .findFirst();
    }

    private ProductVariant resolveVariant(Product product, Long variantId) {
        if (variantId == null) return null;
        return product.getVariants().stream()
                .filter(v -> v.getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        "Variant " + variantId + " does not belong to product " + product.getId()
                ));
    }

    private BigDecimal computeSubtotal(Cart cart) {
        return cart.getActiveItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ─── DTO Mapping ──────────────────────────────────────────────

    public CartDTO mapToDTO(Cart cart) {
        List<CartItemDTO> activeItems = cart.getActiveItems().stream()
                .map(this::mapItemToDTO)
                .collect(Collectors.toList());

        List<CartItemDTO> savedItems = cart.getSavedItems().stream()
                .map(this::mapItemToDTO)
                .collect(Collectors.toList());

        BigDecimal freeShippingThreshold = new BigDecimal("999.00");

        return CartDTO.builder()
                .id(cart.getId())
                .items(activeItems)
                .savedForLaterItems(savedItems)
                .totalItems(cart.getTotalItems())
                .subtotal(cart.getSubtotal())
                .taxAmount(cart.getTaxAmount())
                .shippingAmount(cart.getShippingAmount())
                .discountAmount(cart.getDiscountAmount())
                .grandTotal(cart.getGrandTotal())
                .appliedCouponCode(cart.getAppliedCouponCode())
                .freeShipping(cart.getSubtotal().compareTo(freeShippingThreshold) >= 0)
                .build();
    }

    private CartItemDTO mapItemToDTO(CartItem item) {
        Product p = item.getProduct();
        Inventory inv = p.getInventory();
        int stock = (inv != null) ? inv.getQuantity() : 0;

        return CartItemDTO.builder()
                .id(item.getId())
                .productId(p.getId())
                .productName(p.getName())
                .productSlug(p.getSlug())
                .productImageUrl(p.getPrimaryImageUrl())
                .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                .variantName(item.getVariant() != null ? item.getVariant().getVariantName() : null)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .inStock(stock >= item.getQuantity())
                .availableStock(stock)
                .savedForLater(item.getSavedForLater())
                .build();
    }
}
