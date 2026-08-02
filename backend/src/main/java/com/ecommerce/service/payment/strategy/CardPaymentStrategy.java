package com.ecommerce.service.payment.strategy;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.Payment;
import com.ecommerce.entity.PaymentMethod;
import com.ecommerce.entity.PaymentStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * CardPaymentStrategy — Handles Credit/Debit Card, UPI, and NetBanking online payments
 * via a simulated payment gateway.
 */
@Component
public class CardPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.CREDIT_CARD;
    }

    @Override
    public Payment processPayment(Order order) {
        String txnId = "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

        return Payment.builder()
                .order(order)
                .paymentMethod(order.getPayment() != null ? order.getPayment().getPaymentMethod() : PaymentMethod.CREDIT_CARD)
                .paymentStatus(PaymentStatus.COMPLETED)
                .transactionId(txnId)
                .amount(order.getGrandTotal())
                .gatewayResponse("Simulated Gateway: Payment processed successfully")
                .paidAt(LocalDateTime.now())
                .build();
    }
}
