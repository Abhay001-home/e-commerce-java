package com.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * DashboardSummaryDTO — aggregate metrics for the Admin Sales & Store Analytics Dashboard.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardSummaryDTO {

    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Long pendingOrders;
    private Long processingOrders;
    private Long shippedOrders;
    private Long deliveredOrders;
    private Long cancelledOrders;

    private Long totalCustomers;
    private Long totalProducts;

    private List<OrderDTO> recentOrders;
    private List<ProductDTO> topSellingProducts;
    private List<InventoryDTO> lowStockAlerts;
}
