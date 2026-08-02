package com.ecommerce.service.order.state;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;

public class PendingOrderState extends AbstractOrderState {

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.PENDING;
    }

    @Override
    public void process(Order order, String remarks, String changedBy) {
        changeState(order, OrderStatus.PROCESSING, remarks, changedBy);
    }

    @Override
    public void cancel(Order order, String remarks, String changedBy) {
        changeState(order, OrderStatus.CANCELLED, remarks, changedBy);
    }
}
