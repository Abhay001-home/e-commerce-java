package com.ecommerce.repository;

import com.ecommerce.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * WishlistRepository — persistence for Wishlist entities.
 */
@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    Optional<Wishlist> findByUserId(Long userId);

    /**
     * Fetches wishlist with items and their products in a single join.
     */
    @Query("""
            SELECT DISTINCT w FROM Wishlist w
            LEFT JOIN FETCH w.items i
            LEFT JOIN FETCH i.product p
            LEFT JOIN FETCH p.inventory
            WHERE w.user.id = :userId
            """)
    Optional<Wishlist> findByUserIdWithItems(@Param("userId") Long userId);
}
