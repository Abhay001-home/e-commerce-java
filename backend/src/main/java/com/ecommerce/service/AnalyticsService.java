package com.ecommerce.service;

import com.ecommerce.dto.response.*;
import com.ecommerce.entity.Inventory;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.InventoryRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AnalyticsService — computes dashboard metrics, revenue aggregates, best sellers,
 * low stock alerts, and sales trend charts for Admin.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderService orderService;
    private final ProductService productService;
    private final InventoryService inventoryService;

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getDashboardSummary() {
        List<Order> allOrders = orderRepository.findAll();

        BigDecimal totalRevenue = allOrders.stream()
                .filter(o -> o.getOrderStatus() != OrderStatus.CANCELLED)
                .map(Order::getGrandTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = allOrders.size();
        long pending = countByStatus(allOrders, OrderStatus.PENDING);
        long processing = countByStatus(allOrders, OrderStatus.PROCESSING);
        long shipped = countByStatus(allOrders, OrderStatus.SHIPPED);
        long delivered = countByStatus(allOrders, OrderStatus.DELIVERED);
        long cancelled = countByStatus(allOrders, OrderStatus.CANCELLED);

        long totalCustomers = userRepository.count();
        long totalProducts = productRepository.count();

        // Top 5 recent orders
        Pageable recentPageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        Page<Order> recentOrdersPage = orderRepository.findAll(recentPageable);
        List<OrderDTO> recentOrders = recentOrdersPage.getContent().stream()
                .map(orderService::mapToDTO)
                .collect(Collectors.toList());

        // Top 5 best selling products (by soldCount)
        Pageable topSellingPageable = PageRequest.of(0, 5, Sort.by("soldCount").descending());
        Page<Product> topSellingPage = productRepository.findAll(topSellingPageable);
        List<ProductDTO> topSellingProducts = topSellingPage.getContent().stream()
                .map(productService::mapToProductDTO)
                .collect(Collectors.toList());

        // Low stock alerts
        List<InventoryDTO> lowStockAlerts = inventoryService.getLowStockAlerts();

        log.info("Generated dashboard summary: Total Revenue = ₹{}", totalRevenue);

        return DashboardSummaryDTO.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .pendingOrders(pending)
                .processingOrders(processing)
                .shippedOrders(shipped)
                .deliveredOrders(delivered)
                .cancelledOrders(cancelled)
                .totalCustomers(totalCustomers)
                .totalProducts(totalProducts)
                .recentOrders(recentOrders)
                .topSellingProducts(topSellingProducts)
                .lowStockAlerts(lowStockAlerts)
                .build();
    }

    @Transactional(readOnly = true)
    public List<SalesTrendDTO> getSalesTrend() {
        List<Order> validOrders = orderRepository.findAll().stream()
                .filter(o -> o.getOrderStatus() != OrderStatus.CANCELLED && o.getCreatedAt() != null)
                .collect(Collectors.toList());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Map<String, List<Order>> groupedByDate = validOrders.stream()
                .collect(Collectors.groupingBy(o -> o.getCreatedAt().format(formatter)));

        List<SalesTrendDTO> trend = new ArrayList<>();
        groupedByDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String dateStr = entry.getKey();
                    List<Order> dateOrders = entry.getValue();
                    BigDecimal dayRevenue = dateOrders.stream()
                            .map(Order::getGrandTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    trend.add(new SalesTrendDTO(dateStr, dayRevenue, (long) dateOrders.size()));
                });

        return trend;
    }

    private long countByStatus(List<Order> orders, OrderStatus status) {
        return orders.stream().filter(o -> o.getOrderStatus() == status).count();
    }
}
