package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * WishlistItem entity — one product entry in a user's wishlist.
 *
 * Design Decisions:
 * - ManyToOne to Wishlist + Product (LAZY — never need full graph on list fetch)
 * - Unique constraint (wishlist_id, product_id) prevents duplicates at DB level
 * - addedAt is set at creation (immutable) — useful for "recently added" ordering
 * - No variant tracking in wishlist (user may decide variant at checkout)
 */
@Entity
@Table(name = "wishlist_items", indexes = {
        @Index(name = "idx_wishlist_items_wishlist", columnList = "wishlist_id"),
        @Index(name = "idx_wishlist_items_product", columnList = "product_id")
},
uniqueConstraints = {
        @UniqueConstraint(name = "uq_wishlist_product",
                columnNames = {"wishlist_id", "product_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wishlist_id", nullable = false)
    private Wishlist wishlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Timestamp when this product was added — immutable after creation. */
    @CreationTimestamp
    @Column(name = "added_at", updatable = false)
    private LocalDateTime addedAt;
}
