package com.ecommerce.specification;

import com.ecommerce.entity.Product;
import jakarta.persistence.criteria.*;
import lombok.Builder;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductSpecification — dynamic product filter using JPA Criteria API.
 *
 * Design Pattern: Specification Pattern (Interpreter variant)
 *
 * Enables building complex queries dynamically without if-else JPQL strings:
 *   ProductSpecification.builder()
 *       .categoryId(1L)
 *       .minPrice(new BigDecimal("100"))
 *       .keyword("phone")
 *       .build()
 *       .toSpec()
 *
 * Data Structure: ArrayList<Predicate> for collecting WHERE clauses.
 * Only non-null fields contribute predicates (clean filtering).
 */
@Builder
public class ProductSpecification {

    private String keyword;
    private Long categoryId;
    private Long brandId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean isActive;
    private Boolean isFeatured;
    private Boolean inStock;

    /**
     * Converts this builder object into a JPA Specification<Product>.
     * All conditions are combined with AND.
     */
    public Specification<Product> toSpec() {
        return (root, query, cb) -> {
            // ArrayList of predicates — collected and ANDed at the end
            List<Predicate> predicates = new ArrayList<>();

            // ── Keyword search (name, description, shortDesc) ────────
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                Predicate namePred = cb.like(cb.lower(root.get("name")), pattern);
                Predicate descPred = cb.like(cb.lower(root.get("description")), pattern);
                Predicate shortPred = cb.like(cb.lower(root.get("shortDesc")), pattern);
                predicates.add(cb.or(namePred, descPred, shortPred));
            }

            // ── Category filter ──────────────────────────────────────
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            // ── Brand filter ─────────────────────────────────────────
            if (brandId != null) {
                predicates.add(cb.equal(root.get("brand").get("id"), brandId));
            }

            // ── Price range ───────────────────────────────────────────
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // ── Active status ─────────────────────────────────────────
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            } else {
                // Default: only show active products on public endpoints
                predicates.add(cb.equal(root.get("isActive"), true));
            }

            // ── Featured flag ─────────────────────────────────────────
            if (isFeatured != null) {
                predicates.add(cb.equal(root.get("isFeatured"), isFeatured));
            }

            // ── In stock filter ───────────────────────────────────────
            if (Boolean.TRUE.equals(inStock)) {
                Join<Object, Object> inventory = root.join("inventory", JoinType.LEFT);
                predicates.add(cb.greaterThan(inventory.get("quantity"), 0));
            }

            // Avoid duplicate results from joins
            if (query != null) {
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
