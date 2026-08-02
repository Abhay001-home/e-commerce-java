package com.ecommerce.service;

import com.ecommerce.dto.request.CheckoutRequest;
import com.ecommerce.dto.request.OrderStatusUpdateRequest;
import com.ecommerce.dto.response.OrderDetailDTO;
import com.ecommerce.entity.*;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.repository.*;
import com.ecommerce.service.payment.strategy.PaymentStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private UserRepository userRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private CartService cartService;
    @Mock private CouponService couponService;
    @Mock private PaymentService paymentService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Address address;
    private Product product;
    private Inventory inventory;
    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("john@example.com").firstName("John").lastName("Doe").build();

        address = Address.builder()
                .id(10L)
                .user(user)
                .fullName("John Doe")
                .street("123 Main St")
                .city("Tech City")
                .state("State")
                .zipCode("123456")
                .country("India")
                .phone("9876543210")
                .build();

        product = Product.builder()
                .id(100L)
                .name("Wireless Headset")
                .sku("HEAD-001")
                .price(new BigDecimal("1500.00"))
                .isActive(true)
                .build();

        inventory = Inventory.builder()
                .id(1L)
                .product(product)
                .quantity(10)
                .build();
        product.setInventory(inventory);

        cart = Cart.builder()
                .id(50L)
                .user(user)
                .items(new ArrayList<>())
                .subtotal(new BigDecimal("1500.00"))
                .taxAmount(new BigDecimal("270.00"))
                .shippingAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .grandTotal(new BigDecimal("1770.00"))
                .build();

        cartItem = CartItem.builder()
                .id(500L)
                .cart(cart)
                .product(product)
                .quantity(1)
                .unitPrice(new BigDecimal("1500.00"))
                .totalPrice(new BigDecimal("1500.00"))
                .savedForLater(false)
                .build();
        cart.getItems().add(cartItem);
    }

    @Test
    @DisplayName("Should successfully checkout cart and create order")
    void checkout_Success() {
        CheckoutRequest request = new CheckoutRequest();
        request.setShippingAddressId(10L);
        request.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            if (o.getId() == null) o.setId(1000L);
            return o;
        });

        Payment dummyPayment = Payment.builder()
                .paymentMethod(PaymentMethod.CASH_ON_DELIVERY)
                .paymentStatus(PaymentStatus.PENDING)
                .amount(new BigDecimal("1770.00"))
                .build();
        when(paymentService.processOrderPayment(any(), any())).thenReturn(dummyPayment);

        Shipment dummyShipment = Shipment.builder().shipmentStatus(ShipmentStatus.PENDING).build();
        when(shipmentRepository.save(any())).thenReturn(dummyShipment);

        OrderDetailDTO result = orderService.checkout(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.getGrandTotal()).isEqualByComparingTo(new BigDecimal("1770.00"));
        assertThat(inventory.getQuantity()).isEqualTo(9); // stock decremented 10 -> 9

        verify(inventoryRepository).save(inventory);
        verify(cartService).clearCart(1L);
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    @DisplayName("Should throw BadRequestException during checkout if cart is empty")
    void checkout_EmptyCart() {
        cart.getItems().clear();
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));

        CheckoutRequest request = new CheckoutRequest();
        request.setShippingAddressId(10L);
        request.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        assertThatThrownBy(() -> orderService.checkout(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("no items to checkout");
    }

    @Test
    @DisplayName("Should cancel order and restore stock")
    void cancelOrder_Success() {
        Order order = Order.builder()
                .id(1000L)
                .user(user)
                .orderNumber("ORD-20260803-001")
                .orderStatus(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        OrderItem item = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(2)
                .build();
        order.getItems().add(item);

        when(orderRepository.findByIdAndUserIdWithDetails(1000L, 1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        OrderDetailDTO result = orderService.cancelOrder(1L, 1000L, "Changed my mind");

        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(inventory.getQuantity()).isEqualTo(12); // stock restored 10 -> 12

        verify(inventoryRepository).save(inventory);
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    @DisplayName("Should update order status via Admin and State Pattern")
    void updateOrderStatus_AdminSuccess() {
        Order order = Order.builder()
                .id(1000L)
                .user(user)
                .orderNumber("ORD-20260803-001")
                .orderStatus(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        when(orderRepository.findByIdWithDetails(1000L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
        request.setStatus(OrderStatus.PROCESSING);
        request.setRemarks("Payment verified");

        OrderDetailDTO result = orderService.updateOrderStatus(1000L, request, "admin@test.com");

        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.PROCESSING);
        verify(orderRepository).save(order);
        verify(eventPublisher).publishEvent(any());
    }
}
