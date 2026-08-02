package com.ecommerce.service.order.state;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.exception.BadRequestException;

import java.util.EnumMap;
import java.util.Map;

/**
 * OrderStateFactory — Factory pattern for resolving OrderState implementation
 * based on the current OrderStatus.
 */
public class OrderStateFactory {

    private static final Map<OrderStatus, OrderState> STATES = new EnumMap<>(OrderStatus.class);

    static {
        STATES.put(OrderStatus.PENDING, new PendingOrderState());
        STATES.put(OrderStatus.PROCESSING, new ProcessingOrderState());
        STATES.put(OrderStatus.SHIPPED, new ShippedOrderState());
        STATES.put(OrderStatus.DELIVERED, new DeliveredOrderState());
        STATES.put(OrderStatus.CANCELLED, new CancelledOrderState());
        STATES.put(OrderStatus.REFUNDED, new RefundedOrderState());
    }

    public static OrderState getState(OrderStatus status) {
        OrderState state = STATES.get(status);
        if (state == null) {
            throw new BadRequestException("Unknown order status: " + status);
        }
        return state;
    }

    public static OrderState getState(Order order) {
        return getState(order.getOrderStatus());
    }
}
