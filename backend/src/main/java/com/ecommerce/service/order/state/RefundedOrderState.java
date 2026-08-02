package com.ecommerce.service.order.state;

import com.ecommerce.entity.OrderStatus;

public class RefundedOrderState extends AbstractOrderState {

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.REFUNDED;
    }
}
