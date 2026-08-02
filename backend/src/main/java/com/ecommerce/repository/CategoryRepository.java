package com.ecommerce.repository;

import com.ecommerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CategoryRepository — manages category persistence and hierarchy queries.
 *
 * Data Structure note: Categories form a tree structure.
 * findByParentIdIsNull() fetches root nodes → children loaded lazily.
 * This avoids N+1 by fetching only what the UI needs.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    /** Fetch all top-level (root) categories — used for navigation menu. */
    List<Category> findByParentIsNullAndIsActiveTrue();

    /** Fetch all active categories (flat list for admin). */
    List<Category> findByIsActiveTrue();

    /** Fetch children of a given parent. */
    List<Category> findByParentIdAndIsActiveTrue(Long parentId);

    /** Fetch category with children eagerly for tree rendering. */
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.id = :id")
    Optional<Category> findByIdWithChildren(Long id);

    /** All categories for admin with product counts. */
    @Query("""
            SELECT c FROM Category c
            WHERE (:isActive IS NULL OR c.isActive = :isActive)
            ORDER BY c.name
            """)
    List<Category> findAllWithFilter(Boolean isActive);
}
