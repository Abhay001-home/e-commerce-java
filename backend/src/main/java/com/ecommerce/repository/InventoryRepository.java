package com.ecommerce.repository;

import com.ecommerce.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * InventoryRepository — stock level queries.
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);

    Optional<Inventory> findByVariantId(Long variantId);

    /** Low stock alert query — quantity below threshold. */
    @Query("""
            SELECT i FROM Inventory i
            JOIN FETCH i.product p
            WHERE i.quantity <= i.lowStockQty AND p.isActive = true
            ORDER BY i.quantity ASC
            """)
    List<Inventory> findLowStockInventory();

    /** Out of stock products. */
    @Query("""
            SELECT i FROM Inventory i
            JOIN FETCH i.product p
            WHERE i.quantity = 0 AND p.isActive = true
            """)
    List<Inventory> findOutOfStockInventory();
}
