package com.ecommerce.event;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * OrderStatusChangedEvent — event published when an order status is updated.
 */
@Getter
public class OrderStatusChangedEvent extends ApplicationEvent {

    private final Order order;
    private final OrderStatus previousStatus;
    private final OrderStatus newStatus;

    public OrderStatusChangedEvent(Object source, Order order, OrderStatus previousStatus, OrderStatus newStatus) {
        super(source);
        this.order = order;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }
}
