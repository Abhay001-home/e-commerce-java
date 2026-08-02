package com.ecommerce.service.order.state;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.entity.OrderStatusHistory;
import com.ecommerce.exception.BadRequestException;

/**
 * AbstractOrderState — base class for OrderState implementations.
 * Provides default behavior (throwing BadRequestException for illegal state transitions)
 * and helper methods for updating order status and creating history audit records.
 */
public abstract class AbstractOrderState implements OrderState {

    protected void changeState(Order order, OrderStatus nextStatus, String remarks, String changedBy) {
        OrderStatus previousStatus = order.getOrderStatus();
        order.setOrderStatus(nextStatus);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .fromStatus(previousStatus)
                .toStatus(nextStatus)
                .remarks(remarks)
                .changedBy(changedBy)
                .build();

        order.addStatusHistory(history);
    }

    @Override
    public void process(Order order, String remarks, String changedBy) {
        throw new BadRequestException("Cannot transition order from " + getStatus() + " to PROCESSING");
    }

    @Override
    public void ship(Order order, String remarks, String changedBy) {
        throw new BadRequestException("Cannot transition order from " + getStatus() + " to SHIPPED");
    }

    @Override
    public void deliver(Order order, String remarks, String changedBy) {
        throw new BadRequestException("Cannot transition order from " + getStatus() + " to DELIVERED");
    }

    @Override
    public void cancel(Order order, String remarks, String changedBy) {
        throw new BadRequestException("Cannot transition order from " + getStatus() + " to CANCELLED");
    }

    @Override
    public void refund(Order order, String remarks, String changedBy) {
        throw new BadRequestException("Cannot transition order from " + getStatus() + " to REFUNDED");
    }
}
