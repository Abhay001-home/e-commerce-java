package com.ecommerce.service.order.state;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;

public class DeliveredOrderState extends AbstractOrderState {

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.DELIVERED;
    }

    @Override
    public void refund(Order order, String remarks, String changedBy) {
        changeState(order, OrderStatus.REFUNDED, remarks, changedBy);
    }
}
