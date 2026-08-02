package com.ecommerce.service.order.state;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;

public class ShippedOrderState extends AbstractOrderState {

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.SHIPPED;
    }

    @Override
    public void deliver(Order order, String remarks, String changedBy) {
        changeState(order, OrderStatus.DELIVERED, remarks, changedBy);
    }

    @Override
    public void cancel(Order order, String remarks, String changedBy) {
        changeState(order, OrderStatus.CANCELLED, remarks, changedBy);
    }
}
