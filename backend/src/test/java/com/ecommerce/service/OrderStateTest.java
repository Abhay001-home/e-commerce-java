package com.ecommerce.service.order.state;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OrderState State Pattern Unit Tests")
class OrderStateTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .id(1L)
                .orderNumber("ORD-TEST-001")
                .orderStatus(OrderStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("PendingOrderState should allow transition to PROCESSING and CANCELLED")
    void pendingState_ValidTransitions() {
        OrderState pending = OrderStateFactory.getState(OrderStatus.PENDING);

        pending.process(order, "Processing order", "admin@test.com");
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PROCESSING);
        assertThat(order.getStatusHistories()).hasSize(1);

        OrderState processing = OrderStateFactory.getState(order);
        processing.cancel(order, "User cancelled", "user@test.com");
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getStatusHistories()).hasSize(2);
    }

    @Test
    @DisplayName("PendingOrderState should reject transition to SHIPPED or DELIVERED directly")
    void pendingState_InvalidTransitions() {
        OrderState pending = OrderStateFactory.getState(OrderStatus.PENDING);

        assertThatThrownBy(() -> pending.ship(order, "Ship", "admin"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot transition order from PENDING to SHIPPED");

        assertThatThrownBy(() -> pending.deliver(order, "Deliver", "admin"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot transition order from PENDING to DELIVERED");
    }

    @Test
    @DisplayName("ProcessingOrderState should allow transition to SHIPPED")
    void processingState_ToShipped() {
        order.setOrderStatus(OrderStatus.PROCESSING);
        OrderState processing = OrderStateFactory.getState(order);

        processing.ship(order, "Dispatched", "admin@test.com");
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    @DisplayName("ShippedOrderState should allow transition to DELIVERED")
    void shippedState_ToDelivered() {
        order.setOrderStatus(OrderStatus.SHIPPED);
        OrderState shipped = OrderStateFactory.getState(order);

        shipped.deliver(order, "Delivered to customer", "admin@test.com");
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("DeliveredOrderState should reject direct transition to SHIPPED or PENDING")
    void deliveredState_InvalidTransitions() {
        order.setOrderStatus(OrderStatus.DELIVERED);
        OrderState delivered = OrderStateFactory.getState(order);

        assertThatThrownBy(() -> delivered.process(order, "Re-process", "admin"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot transition order from DELIVERED to PROCESSING");
    }

    @Test
    @DisplayName("CancelledOrderState should reject all transitions")
    void cancelledState_Terminal() {
        order.setOrderStatus(OrderStatus.CANCELLED);
        OrderState cancelled = OrderStateFactory.getState(order);

        assertThatThrownBy(() -> cancelled.process(order, "Try process", "admin"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot transition order from CANCELLED to PROCESSING");
    }
}
