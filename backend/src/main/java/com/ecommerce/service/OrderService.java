package com.ecommerce.service;

import com.ecommerce.dto.request.CheckoutRequest;
import com.ecommerce.dto.request.OrderStatusUpdateRequest;
import com.ecommerce.dto.request.ShipmentUpdateRequest;
import com.ecommerce.dto.response.*;
import com.ecommerce.entity.*;
import com.ecommerce.event.OrderPlacedEvent;
import com.ecommerce.event.OrderStatusChangedEvent;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.*;
import com.ecommerce.service.order.state.OrderState;
import com.ecommerce.service.order.state.OrderStateFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * OrderService — Business logic for checkout execution, order management, status updates,
 * and state transitions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final ShipmentRepository shipmentRepository;
    private final CartService cartService;
    private final CouponService couponService;
    private final PaymentService paymentService;
    private final ApplicationEventPublisher eventPublisher;

    // ─── User Checkout ───────────────────────────────────────────

    @Transactional
    public OrderDetailDTO checkout(Long userId, CheckoutRequest request) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new BadRequestException("Cart is empty"));

        List<CartItem> activeItems = cart.getActiveItems();
        if (activeItems.isEmpty()) {
            throw new BadRequestException("Cart has no items to checkout");
        }

        // Validate stock for all items
        for (CartItem item : activeItems) {
            Inventory inv = item.getProduct().getInventory();
            if (inv == null || inv.getQuantity() < item.getQuantity()) {
                throw new BadRequestException("Insufficient stock for item: " + item.getProduct().getName());
            }
        }

        // Validate shipping address
        Address shippingAddress = addressRepository.findById(request.getShippingAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", request.getShippingAddressId()));

        if (!shippingAddress.getUser().getId().equals(userId)) {
            throw new BadRequestException("Selected shipping address does not belong to user");
        }

        String addressSnapshot = formatAddressSnapshot(shippingAddress);

        // Recalculate totals to ensure accurate pricing
        cartService.recalculateTotals(cart);

        // Generate Order Number
        String orderNumber = generateOrderNumber();

        // Build Order
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(cart.getUser())
                .orderStatus(OrderStatus.PENDING)
                .subtotal(cart.getSubtotal())
                .taxAmount(cart.getTaxAmount())
                .shippingAmount(cart.getShippingAmount())
                .discountAmount(cart.getDiscountAmount())
                .grandTotal(cart.getGrandTotal())
                .appliedCouponCode(cart.getAppliedCouponCode())
                .shippingAddressSnapshot(addressSnapshot)
                .notes(request.getNotes())
                .build();

        // Build OrderItems & Decrement Inventory Stock
        for (CartItem ci : activeItems) {
            OrderItem orderItem = OrderItem.builder()
                    .product(ci.getProduct())
                    .variant(ci.getVariant())
                    .productName(ci.getProduct().getName())
                    .productSku(ci.getVariant() != null ? ci.getVariant().getSku() : ci.getProduct().getSku())
                    .unitPrice(ci.getUnitPrice())
                    .quantity(ci.getQuantity())
                    .totalPrice(ci.getTotalPrice())
                    .build();
            order.addItem(orderItem);

            // Decrement Stock
            Inventory inv = ci.getProduct().getInventory();
            inv.decrementStock(ci.getQuantity());
            inventoryRepository.save(inv);
        }

        // Add Initial Status History
        OrderStatusHistory initialHistory = OrderStatusHistory.builder()
                .fromStatus(null)
                .toStatus(OrderStatus.PENDING)
                .remarks("Order placed via checkout")
                .changedBy(cart.getUser().getEmail())
                .build();
        order.addStatusHistory(initialHistory);

        // Save initial order
        Order savedOrder = orderRepository.save(order);

        // Increment Coupon Usage if applicable
        if (cart.getAppliedCouponCode() != null) {
            try {
                couponService.incrementUsage(cart.getAppliedCouponCode());
            } catch (Exception e) {
                log.warn("Could not increment usage for coupon {}", cart.getAppliedCouponCode(), e);
            }
        }

        // Process Payment via Strategy
        Payment payment = paymentService.processOrderPayment(savedOrder, request.getPaymentMethod());
        savedOrder.setPayment(payment);

        // Create Shipment
        Shipment shipment = Shipment.builder()
                .order(savedOrder)
                .shipmentStatus(ShipmentStatus.PENDING)
                .build();
        shipment = shipmentRepository.save(shipment);
        savedOrder.setShipment(shipment);

        // Clear active cart items
        cartService.clearCart(userId);

        // Save finalized order
        savedOrder = orderRepository.save(savedOrder);

        // Publish Order Placed Event (Observer Pattern)
        eventPublisher.publishEvent(new OrderPlacedEvent(this, savedOrder));

        log.info("Successfully created Order #{} for user {}", savedOrder.getOrderNumber(), userId);
        return mapToDetailDTO(savedOrder);
    }

    // ─── User Order Queries & Cancellation ───────────────────────

    @Transactional(readOnly = true)
    public PagedResponse<OrderDTO> getUserOrders(Long userId, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Order> orderPage = orderRepository.findByUserId(userId, pageable);
        Page<OrderDTO> dtoPage = orderPage.map(this::mapToDTO);
        return PagedResponse.from(dtoPage);
    }

    @Transactional(readOnly = true)
    public OrderDetailDTO getOrderDetailForUser(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserIdWithDetails(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return mapToDetailDTO(order);
    }

    @Transactional
    public OrderDetailDTO cancelOrder(Long userId, Long orderId, String reason) {
        Order order = orderRepository.findByIdAndUserIdWithDetails(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        OrderStatus previousStatus = order.getOrderStatus();
        OrderState state = OrderStateFactory.getState(order);
        state.cancel(order, reason != null ? reason : "Cancelled by user", order.getUser().getEmail());

        // Restore inventory stock
        for (OrderItem item : order.getItems()) {
            Inventory inv = item.getProduct().getInventory();
            if (inv != null) {
                inv.incrementStock(item.getQuantity());
                inventoryRepository.save(inv);
            }
        }

        // Update payment status if already paid
        if (order.getPayment() != null && order.getPayment().getPaymentStatus() == PaymentStatus.COMPLETED) {
            paymentService.updatePaymentStatus(orderId, PaymentStatus.REFUNDED);
        }

        Order saved = orderRepository.save(order);
        eventPublisher.publishEvent(new OrderStatusChangedEvent(this, saved, previousStatus, OrderStatus.CANCELLED));

        log.info("Order #{} cancelled by user {}", saved.getOrderNumber(), userId);
        return mapToDetailDTO(saved);
    }

    // ─── Admin Order Management ───────────────────────────────────

    @Transactional(readOnly = true)
    public PagedResponse<OrderDTO> getAllOrders(OrderStatus status, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Order> orderPage = (status != null)
                ? orderRepository.findByOrderStatus(status, pageable)
                : orderRepository.findAll(pageable);

        Page<OrderDTO> dtoPage = orderPage.map(this::mapToDTO);
        return PagedResponse.from(dtoPage);
    }

    @Transactional(readOnly = true)
    public OrderDetailDTO getOrderByIdForAdmin(Long orderId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return mapToDetailDTO(order);
    }

    @Transactional
    public OrderDetailDTO updateOrderStatus(Long orderId, OrderStatusUpdateRequest request, String adminEmail) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        OrderStatus previousStatus = order.getOrderStatus();
        OrderState state = OrderStateFactory.getState(order);

        // Execute State Pattern transition based on target status
        switch (request.getStatus()) {
            case PROCESSING -> state.process(order, request.getRemarks(), adminEmail);
            case SHIPPED -> state.ship(order, request.getRemarks(), adminEmail);
            case DELIVERED -> {
                state.deliver(order, request.getRemarks(), adminEmail);
                if (order.getPayment() != null && order.getPayment().getPaymentStatus() == PaymentStatus.PENDING) {
                    paymentService.updatePaymentStatus(orderId, PaymentStatus.COMPLETED);
                }
                if (order.getShipment() != null) {
                    order.getShipment().setShipmentStatus(ShipmentStatus.DELIVERED);
                    order.getShipment().setDeliveredAt(LocalDateTime.now());
                }
            }
            case CANCELLED -> {
                state.cancel(order, request.getRemarks(), adminEmail);
                for (OrderItem item : order.getItems()) {
                    Inventory inv = item.getProduct().getInventory();
                    if (inv != null) {
                        inv.incrementStock(item.getQuantity());
                        inventoryRepository.save(inv);
                    }
                }
            }
            case REFUNDED -> state.refund(order, request.getRemarks(), adminEmail);
            default -> throw new BadRequestException("Invalid target status: " + request.getStatus());
        }

        Order saved = orderRepository.save(order);
        eventPublisher.publishEvent(new OrderStatusChangedEvent(this, saved, previousStatus, request.getStatus()));

        log.info("Order #{} status updated to {} by Admin {}", saved.getOrderNumber(), request.getStatus(), adminEmail);
        return mapToDetailDTO(saved);
    }

    @Transactional
    public OrderDetailDTO updateShipment(Long orderId, ShipmentUpdateRequest request) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        Shipment shipment = order.getShipment();
        if (shipment == null) {
            shipment = Shipment.builder().order(order).build();
        }

        shipment.setCarrierName(request.getCarrierName());
        shipment.setTrackingNumber(request.getTrackingNumber());
        shipment.setShipmentStatus(request.getShipmentStatus());
        if (request.getEstimatedDeliveryDate() != null) {
            shipment.setEstimatedDeliveryDate(request.getEstimatedDeliveryDate());
        }
        if (request.getShipmentStatus() == ShipmentStatus.DISPATCHED && shipment.getShippedAt() == null) {
            shipment.setShippedAt(LocalDateTime.now());
        } else if (request.getShipmentStatus() == ShipmentStatus.DELIVERED && shipment.getDeliveredAt() == null) {
            shipment.setDeliveredAt(LocalDateTime.now());
        }

        shipmentRepository.save(shipment);
        order.setShipment(shipment);

        log.info("Shipment updated for Order #{}: carrier={}, tracking={}", order.getOrderNumber(), request.getCarrierName(), request.getTrackingNumber());
        return mapToDetailDTO(orderRepository.save(order));
    }

    // ─── Private Helpers ──────────────────────────────────────────

    private String generateOrderNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "ORD-" + datePart + "-" + randomPart;
    }

    private String formatAddressSnapshot(Address addr) {
        return addr.getFullName() + "\n" +
                addr.getStreet() + "\n" +
                addr.getCity() + ", " + addr.getState() + " " + addr.getZipCode() + "\n" +
                addr.getCountry() + "\nPhone: " + addr.getPhone();
    }

    // ─── DTO Mapping ──────────────────────────────────────────────

    public OrderDTO mapToDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser().getId())
                .userFullName(order.getUser().getFullName())
                .userEmail(order.getUser().getEmail())
                .orderStatus(order.getOrderStatus())
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .shippingAmount(order.getShippingAmount())
                .discountAmount(order.getDiscountAmount())
                .grandTotal(order.getGrandTotal())
                .appliedCouponCode(order.getAppliedCouponCode())
                .totalItems(order.getItems() != null ? order.getItems().size() : 0)
                .payment(paymentService.mapToDTO(order.getPayment()))
                .shipment(mapShipmentToDTO(order.getShipment()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public OrderDetailDTO mapToDetailDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(this::mapItemToDTO)
                .collect(Collectors.toList());

        List<OrderStatusHistoryDTO> historyDTOs = order.getStatusHistories().stream()
                .map(this::mapHistoryToDTO)
                .collect(Collectors.toList());

        return OrderDetailDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser().getId())
                .userFullName(order.getUser().getFullName())
                .userEmail(order.getUser().getEmail())
                .orderStatus(order.getOrderStatus())
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .shippingAmount(order.getShippingAmount())
                .discountAmount(order.getDiscountAmount())
                .grandTotal(order.getGrandTotal())
                .appliedCouponCode(order.getAppliedCouponCode())
                .shippingAddressSnapshot(order.getShippingAddressSnapshot())
                .billingAddressSnapshot(order.getBillingAddressSnapshot())
                .notes(order.getNotes())
                .items(itemDTOs)
                .payment(paymentService.mapToDTO(order.getPayment()))
                .shipment(mapShipmentToDTO(order.getShipment()))
                .statusHistory(historyDTOs)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemDTO mapItemToDTO(OrderItem item) {
        Product p = item.getProduct();
        return OrderItemDTO.builder()
                .id(item.getId())
                .productId(p != null ? p.getId() : null)
                .productName(item.getProductName())
                .productSlug(p != null ? p.getSlug() : null)
                .productImageUrl(p != null ? p.getPrimaryImageUrl() : null)
                .productSku(item.getProductSku())
                .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                .variantName(item.getVariant() != null ? item.getVariant().getVariantName() : null)
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .totalPrice(item.getTotalPrice())
                .build();
    }

    private ShipmentDTO mapShipmentToDTO(Shipment s) {
        if (s == null) return null;
        return ShipmentDTO.builder()
                .id(s.getId())
                .carrierName(s.getCarrierName())
                .trackingNumber(s.getTrackingNumber())
                .shipmentStatus(s.getShipmentStatus())
                .shippedAt(s.getShippedAt())
                .estimatedDeliveryDate(s.getEstimatedDeliveryDate())
                .deliveredAt(s.getDeliveredAt())
                .build();
    }

    private OrderStatusHistoryDTO mapHistoryToDTO(OrderStatusHistory h) {
        return OrderStatusHistoryDTO.builder()
                .id(h.getId())
                .fromStatus(h.getFromStatus())
                .toStatus(h.getToStatus())
                .remarks(h.getRemarks())
                .changedBy(h.getChangedBy())
                .createdAt(h.getCreatedAt())
                .build();
    }
}
