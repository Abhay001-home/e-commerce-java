package com.ecommerce.repository;

import com.ecommerce.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * OrderStatusHistoryRepository — Spring Data JPA repository for OrderStatusHistory.
 */
@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    List<OrderStatusHistory> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}
