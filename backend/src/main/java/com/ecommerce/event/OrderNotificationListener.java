package com.ecommerce.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * OrderNotificationListener — Observer pattern implementation.
 * Listens to order events and dispatches notifications asynchronously.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderNotificationListener {

    @EventListener
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("[NOTIFICATION OBSERVER] Order placed event received for Order #: {} | Total: ₹{} | User: {}",
                event.getOrder().getOrderNumber(),
                event.getOrder().getGrandTotal(),
                event.getOrder().getUser().getEmail());
        // Simulating email dispatch logic
    }

    @EventListener
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info("[NOTIFICATION OBSERVER] Order status changed for Order #: {} | Transition: {} -> {} | User: {}",
                event.getOrder().getOrderNumber(),
                event.getPreviousStatus(),
                event.getNewStatus(),
                event.getOrder().getUser().getEmail());
        // Simulating status change alert dispatch logic
    }
}
