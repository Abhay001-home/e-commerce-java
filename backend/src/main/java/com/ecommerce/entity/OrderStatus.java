package com.ecommerce.entity;

/**
 * OrderStatus enum — represents the state of an order in its lifecycle.
 */
public enum OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
}
