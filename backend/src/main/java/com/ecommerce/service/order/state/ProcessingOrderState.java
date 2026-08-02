package com.ecommerce.service.order.state;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;

public class ProcessingOrderState extends AbstractOrderState {

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.PROCESSING;
    }

    @Override
    public void ship(Order order, String remarks, String changedBy) {
        changeState(order, OrderStatus.SHIPPED, remarks, changedBy);
    }

    @Override
    public void cancel(Order order, String remarks, String changedBy) {
        changeState(order, OrderStatus.CANCELLED, remarks, changedBy);
    }
}
