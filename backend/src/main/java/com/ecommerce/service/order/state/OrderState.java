package com.ecommerce.service.order.state;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;

/**
 * OrderState interface — State Pattern for Order lifecycle transitions.
 *
 * Enforces valid state transitions and encapsulates status transition behavior.
 */
public interface OrderState {

    /** Current status represented by this state. */
    OrderStatus getStatus();

    /** Transition from current state to PROCESSING state. */
    void process(Order order, String remarks, String changedBy);

    /** Transition from current state to SHIPPED state. */
    void ship(Order order, String remarks, String changedBy);

    /** Transition from current state to DELIVERED state. */
    void deliver(Order order, String remarks, String changedBy);

    /** Transition from current state to CANCELLED state. */
    void cancel(Order order, String remarks, String changedBy);

    /** Transition from current state to REFUNDED state. */
    void refund(Order order, String remarks, String changedBy);
}
