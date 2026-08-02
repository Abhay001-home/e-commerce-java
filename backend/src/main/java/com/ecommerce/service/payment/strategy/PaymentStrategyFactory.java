package com.ecommerce.service.payment.strategy;

import com.ecommerce.entity.PaymentMethod;
import com.ecommerce.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * PaymentStrategyFactory — Factory for selecting appropriate PaymentStrategy based on PaymentMethod.
 */
@Component
public class PaymentStrategyFactory {

    private final Map<PaymentMethod, PaymentStrategy> strategyMap = new EnumMap<>(PaymentMethod.class);

    public PaymentStrategyFactory(List<PaymentStrategy> strategies) {
        for (PaymentStrategy strategy : strategies) {
            strategyMap.put(strategy.getPaymentMethod(), strategy);
        }
    }

    public PaymentStrategy getStrategy(PaymentMethod method) {
        if (method == PaymentMethod.CASH_ON_DELIVERY) {
            return strategyMap.get(PaymentMethod.CASH_ON_DELIVERY);
        }
        // Online payment methods (CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING) default to CardPaymentStrategy
        PaymentStrategy strategy = strategyMap.get(method);
        if (strategy == null) {
            return strategyMap.get(PaymentMethod.CREDIT_CARD);
        }
        return strategy;
    }
}
