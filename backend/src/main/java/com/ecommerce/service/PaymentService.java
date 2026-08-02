package com.ecommerce.service;

import com.ecommerce.dto.response.PaymentDTO;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.Payment;
import com.ecommerce.entity.PaymentMethod;
import com.ecommerce.entity.PaymentStatus;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.PaymentRepository;
import com.ecommerce.service.payment.strategy.PaymentStrategy;
import com.ecommerce.service.payment.strategy.PaymentStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * PaymentService — manages payment processing via Strategy Pattern.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentStrategyFactory paymentStrategyFactory;

    @Transactional
    public Payment processOrderPayment(Order order, PaymentMethod method) {
        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(method);
        Payment payment = strategy.processPayment(order);
        payment = paymentRepository.save(payment);
        log.info("Processed payment for Order #{}: method={}, status={}, txnId={}",
                order.getOrderNumber(), method, payment.getPaymentStatus(), payment.getTransactionId());
        return payment;
    }

    @Transactional(readOnly = true)
    public PaymentDTO getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
        return mapToDTO(payment);
    }

    @Transactional
    public PaymentDTO updatePaymentStatus(Long orderId, PaymentStatus status) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));

        payment.setPaymentStatus(status);
        if (status == PaymentStatus.COMPLETED && payment.getPaidAt() == null) {
            payment.setPaidAt(LocalDateTime.now());
        }
        log.info("Updated payment status for Order #{}: {}", orderId, status);
        return mapToDTO(paymentRepository.save(payment));
    }

    public PaymentDTO mapToDTO(Payment payment) {
        if (payment == null) return null;
        return PaymentDTO.builder()
                .id(payment.getId())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .transactionId(payment.getTransactionId())
                .amount(payment.getAmount())
                .gatewayResponse(payment.getGatewayResponse())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
