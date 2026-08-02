package com.ecommerce.service;

import com.ecommerce.dto.request.CartItemRequest;
import com.ecommerce.dto.response.WishlistDTO;
import com.ecommerce.dto.response.WishlistItemDTO;
import com.ecommerce.entity.*;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * WishlistService — all wishlist operations for authenticated users.
 *
 * Design Decisions:
 * - getOrCreateWishlist ensures every user always has a wishlist on first access
 * - Duplicate check at service layer (existsByWishlistIdAndProductId) — DB unique
 *   constraint is the safety net
 * - moveToCart delegates to CartService.addItem — no duplicated stock/price logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartService cartService;

    // ─── Get Wishlist ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public WishlistDTO getWishlist(Long userId) {
        Wishlist wishlist = wishlistRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> createWishlist(userId));
        return mapToDTO(wishlist);
    }

    // ─── Add to Wishlist ──────────────────────────────────────────

    @Transactional
    public WishlistDTO addToWishlist(Long userId, Long productId) {
        Wishlist wishlist = getOrCreateWishlist(userId);

        if (wishlistItemRepository.existsByWishlistIdAndProductId(wishlist.getId(), productId)) {
            throw new BadRequestException("Product is already in your wishlist");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        WishlistItem item = WishlistItem.builder()
                .product(product)
                .build();
        wishlist.addItem(item);

        wishlistRepository.save(wishlist);
        log.info("Product {} added to wishlist for user {}", productId, userId);
        return mapToDTO(wishlistRepository.findByUserIdWithItems(userId).orElse(wishlist));
    }

    // ─── Remove from Wishlist ─────────────────────────────────────

    @Transactional
    public WishlistDTO removeFromWishlist(Long userId, Long productId) {
        Wishlist wishlist = getOrCreateWishlist(userId);

        WishlistItem item = wishlistItemRepository
                .findByWishlistIdAndProductId(wishlist.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("WishlistItem", "productId", productId));

        wishlist.removeItem(item);
        wishlistRepository.save(wishlist);
        log.info("Product {} removed from wishlist for user {}", productId, userId);
        return mapToDTO(wishlistRepository.findByUserIdWithItems(userId).orElse(wishlist));
    }

    // ─── Move to Cart ─────────────────────────────────────────────

    @Transactional
    public WishlistDTO moveToCart(Long userId, Long productId) {
        // Add to cart first (validates stock, prices)
        CartItemRequest cartRequest = new CartItemRequest();
        cartRequest.setProductId(productId);
        cartRequest.setQuantity(1);
        cartService.addItem(userId, cartRequest);

        // Remove from wishlist after successful cart add
        return removeFromWishlist(userId, productId);
    }

    // ─── Private helpers ──────────────────────────────────────────

    private Wishlist getOrCreateWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId)
                .orElseGet(() -> createWishlist(userId));
    }

    private Wishlist createWishlist(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return wishlistRepository.save(Wishlist.builder().user(user).build());
    }

    // ─── DTO Mapping ──────────────────────────────────────────────

    private WishlistDTO mapToDTO(Wishlist wishlist) {
        List<WishlistItemDTO> items = wishlist.getItems().stream()
                .map(this::mapItemToDTO)
                .collect(Collectors.toList());

        return WishlistDTO.builder()
                .id(wishlist.getId())
                .items(items)
                .totalItems(items.size())
                .build();
    }

    private WishlistItemDTO mapItemToDTO(WishlistItem item) {
        Product p = item.getProduct();
        Inventory inv = p.getInventory();

        return WishlistItemDTO.builder()
                .id(item.getId())
                .productId(p.getId())
                .productName(p.getName())
                .productSlug(p.getSlug())
                .productImageUrl(p.getPrimaryImageUrl())
                .price(p.getPrice())
                .mrp(p.getMrp())
                .discountPct(p.getDiscountPct())
                .inStock(inv != null && inv.isInStock())
                .addedAt(item.getAddedAt())
                .build();
    }
}
