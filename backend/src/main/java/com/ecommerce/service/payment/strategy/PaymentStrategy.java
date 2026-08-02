package com.ecommerce.service.payment.strategy;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.Payment;
import com.ecommerce.entity.PaymentMethod;

/**
 * PaymentStrategy interface — Strategy Pattern for processing payments.
 */
public interface PaymentStrategy {

    /** Returns the payment method handled by this strategy. */
    PaymentMethod getPaymentMethod();

    /** Processes payment for the given order. */
    Payment processPayment(Order order);
}
