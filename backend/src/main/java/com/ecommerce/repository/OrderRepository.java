package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * OrderRepository — Spring Data JPA repository for Order entity.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByOrderStatus(OrderStatus status, Pageable pageable);

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    @Query("""
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.items i
            LEFT JOIN FETCH i.product
            LEFT JOIN FETCH o.payment
            LEFT JOIN FETCH o.shipment
            WHERE o.id = :id
            """)
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.items i
            LEFT JOIN FETCH i.product
            LEFT JOIN FETCH o.payment
            LEFT JOIN FETCH o.shipment
            WHERE o.id = :id AND o.user.id = :userId
            """)
    Optional<Order> findByIdAndUserIdWithDetails(@Param("id") Long id, @Param("userId") Long userId);
}
