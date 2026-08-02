package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * CartRepository — persistence for Cart entities.
 *
 * The JOIN FETCH query eagerly loads CartItems + Product + Variant in a single
 * SQL join to avoid N+1 selects when building CartDTO.
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long userId);

    /**
     * Fetches cart with all items, their products, and optional variants
     * in a single query — avoids N+1 selects when mapping to CartDTO.
     */
    @Query("""
            SELECT DISTINCT c FROM Cart c
            LEFT JOIN FETCH c.items i
            LEFT JOIN FETCH i.product p
            LEFT JOIN FETCH i.variant
            WHERE c.user.id = :userId
            """)
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);
}
