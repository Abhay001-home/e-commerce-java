package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * ProductRepository — extends JpaSpecificationExecutor for dynamic filter/sort.
 *
 * JpaSpecificationExecutor enables Specification<Product> for:
 *   - Dynamic WHERE clauses (category, brand, price range, name search)
 *   - Combined with Pageable for pagination and sorting
 *
 * This follows the Specification pattern (a form of Interpreter pattern)
 * for building complex queries without large JPQL strings.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    Optional<Product> findBySlug(String slug);

    Optional<Product> findBySkuIgnoreCase(String sku);

    boolean existsBySlug(String slug);

    boolean existsBySku(String sku);

    // ─── Category / Brand queries ─────────────────────────────────

    Page<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

    Page<Product> findByBrandIdAndIsActiveTrue(Long brandId, Pageable pageable);

    // ─── Featured products ────────────────────────────────────────

    List<Product> findByIsFeaturedTrueAndIsActiveTrueOrderBySoldCountDesc();

    // ─── Search (LIKE) ────────────────────────────────────────────

    @Query("""
            SELECT p FROM Product p
            WHERE p.isActive = true
            AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
                 OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))
                 OR LOWER(p.shortDesc) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<Product> searchByKeyword(@Param("query") String query, Pageable pageable);

    // ─── Stats updates (used by review/order services) ────────────

    /**
     * Bulk update avgRating and reviewCount — called after review save/delete.
     * More efficient than loading the entity, modifying, and saving.
     */
    @Modifying
    @Transactional
    @Query("""
            UPDATE Product p
            SET p.avgRating = :avgRating, p.reviewCount = :reviewCount
            WHERE p.id = :productId
            """)
    void updateRatingStats(@Param("productId") Long productId,
                           @Param("avgRating") BigDecimal avgRating,
                           @Param("reviewCount") int reviewCount);

    /**
     * Increment sold count on order placement.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.soldCount = p.soldCount + :qty WHERE p.id = :productId")
    void incrementSoldCount(@Param("productId") Long productId, @Param("qty") int qty);

    // ─── Admin analytics ──────────────────────────────────────────

    /** Best selling products (for admin dashboard). */
    List<Product> findTop10ByIsActiveTrueOrderBySoldCountDesc();

    /** Products with low stock (joined via inventory). */
    @Query("""
            SELECT p FROM Product p
            JOIN p.inventory i
            WHERE i.quantity <= i.lowStockQty AND p.isActive = true
            ORDER BY i.quantity ASC
            """)
    List<Product> findLowStockProducts();
}
