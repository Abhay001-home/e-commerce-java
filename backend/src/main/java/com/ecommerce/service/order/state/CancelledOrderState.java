package com.ecommerce.service.order.state;

import com.ecommerce.entity.OrderStatus;

public class CancelledOrderState extends AbstractOrderState {

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.CANCELLED;
    }
}
