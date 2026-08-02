package com.ecommerce.event;

import com.ecommerce.entity.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * OrderPlacedEvent — event published when a new order is placed.
 */
@Getter
public class OrderPlacedEvent extends ApplicationEvent {

    private final Order order;

    public OrderPlacedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
}
