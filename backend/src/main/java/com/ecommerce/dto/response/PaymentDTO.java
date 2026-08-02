package com.ecommerce.dto.response;

import com.ecommerce.entity.PaymentMethod;
import com.ecommerce.entity.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PaymentDTO — response representation of an order's payment details.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentDTO {

    private Long id;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String transactionId;
    private BigDecimal amount;
    private String gatewayResponse;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
