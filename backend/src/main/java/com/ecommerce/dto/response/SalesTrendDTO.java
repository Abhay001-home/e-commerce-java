package com.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * SalesTrendDTO — data point representing revenue for a specific period (date/month).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SalesTrendDTO {

    private String period; // e.g. "2026-08-01" or "2026-08"
    private BigDecimal revenue;
    private Long orderCount;
}
