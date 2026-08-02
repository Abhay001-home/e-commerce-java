package com.ecommerce.service;

import com.ecommerce.dto.response.DashboardSummaryDTO;
import com.ecommerce.dto.response.SalesTrendDTO;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.repository.InventoryRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsService Unit Tests")
class AnalyticsServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private OrderService orderService;
    @Mock private ProductService productService;
    @Mock private InventoryService inventoryService;

    @InjectMocks
    private AnalyticsService analyticsService;

    private List<Order> orders;

    @BeforeEach
    void setUp() {
        orders = new ArrayList<>();

        Order o1 = Order.builder()
                .id(1L)
                .orderStatus(OrderStatus.DELIVERED)
                .grandTotal(new BigDecimal("1000.00"))
                .createdAt(LocalDateTime.of(2026, 8, 1, 10, 0))
                .build();

        Order o2 = Order.builder()
                .id(2L)
                .orderStatus(OrderStatus.PROCESSING)
                .grandTotal(new BigDecimal("500.00"))
                .createdAt(LocalDateTime.of(2026, 8, 1, 14, 0))
                .build();

        Order o3 = Order.builder()
                .id(3L)
                .orderStatus(OrderStatus.CANCELLED)
                .grandTotal(new BigDecimal("300.00"))
                .createdAt(LocalDateTime.of(2026, 8, 2, 9, 0))
                .build();

        orders.add(o1);
        orders.add(o2);
        orders.add(o3);
    }

    @Test
    @DisplayName("Should aggregate store metrics correctly in Dashboard summary")
    void getDashboardSummary_Success() {
        when(orderRepository.findAll()).thenReturn(orders);
        when(userRepository.count()).thenReturn(15L);
        when(productRepository.count()).thenReturn(50L);
        when(orderRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));
        when(productRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));
        when(inventoryService.getLowStockAlerts()).thenReturn(Collections.emptyList());

        DashboardSummaryDTO summary = analyticsService.getDashboardSummary();

        assertThat(summary).isNotNull();
        // 1000 + 500 = 1500 (excluding 300 cancelled)
        assertThat(summary.getTotalRevenue()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(summary.getTotalOrders()).isEqualTo(3);
        assertThat(summary.getDeliveredOrders()).isEqualTo(1);
        assertThat(summary.getProcessingOrders()).isEqualTo(1);
        assertThat(summary.getCancelledOrders()).isEqualTo(1);
        assertThat(summary.getTotalCustomers()).isEqualTo(15);
        assertThat(summary.getTotalProducts()).isEqualTo(50);
    }

    @Test
    @DisplayName("Should compute sales trend grouped by date")
    void getSalesTrend_Success() {
        when(orderRepository.findAll()).thenReturn(orders);

        List<SalesTrendDTO> trend = analyticsService.getSalesTrend();

        assertThat(trend).isNotNull();
        assertThat(trend).hasSize(1); // Only 2026-08-01 has non-cancelled orders
        assertThat(trend.get(0).getPeriod()).isEqualTo("2026-08-01");
        assertThat(trend.get(0).getRevenue()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(trend.get(0).getOrderCount()).isEqualTo(2);
    }
}
