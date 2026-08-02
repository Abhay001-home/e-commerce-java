package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Wishlist entity — represents a user's saved product list.
 *
 * Design Decisions:
 * - OneToOne with User (each user has exactly one persistent wishlist)
 * - OneToMany to WishlistItem (product + timestamp per entry)
 * - No total computation needed — wishlist is purely a product list
 */
@Entity
@Table(name = "wishlists", indexes = {
        @Index(name = "idx_wishlists_user", columnList = "user_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Wishlist items — ArrayList preserves insertion (add) order.
     * orphanRemoval removes items when detached from this list.
     */
    @OneToMany(mappedBy = "wishlist", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("addedAt DESC")
    @Builder.Default
    private List<WishlistItem> items = new ArrayList<>();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ─── Helper methods ───────────────────────────────────────────

    public void addItem(WishlistItem item) {
        items.add(item);
        item.setWishlist(this);
    }

    public void removeItem(WishlistItem item) {
        items.remove(item);
        item.setWishlist(null);
    }

    public boolean containsProduct(Long productId) {
        return items.stream()
                .anyMatch(i -> i.getProduct().getId().equals(productId));
    }
}
