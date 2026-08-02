package com.ecommerce.service.payment.strategy;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.Payment;
import com.ecommerce.entity.PaymentMethod;
import com.ecommerce.entity.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * CashOnDeliveryPaymentStrategy — Handles Cash On Delivery orders.
 * Payment status remains PENDING until delivery.
 */
@Component
public class CashOnDeliveryPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.CASH_ON_DELIVERY;
    }

    @Override
    public Payment processPayment(Order order) {
        String txnId = "COD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return Payment.builder()
                .order(order)
                .paymentMethod(PaymentMethod.CASH_ON_DELIVERY)
                .paymentStatus(PaymentStatus.PENDING)
                .transactionId(txnId)
                .amount(order.getGrandTotal())
                .gatewayResponse("COD order initialized. Payment due upon delivery.")
                .paidAt(null)
                .build();
    }
}
