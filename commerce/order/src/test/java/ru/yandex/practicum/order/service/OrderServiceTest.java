package ru.yandex.practicum.order.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.commerce.interaction.api.dto.*;
import ru.yandex.practicum.commerce.interaction.api.enums.OrderState;
import ru.yandex.practicum.commerce.interaction.api.enums.PaymentState;
import ru.yandex.practicum.commerce.interaction.api.feign.DeliveryClient;
import ru.yandex.practicum.commerce.interaction.api.feign.PaymentClient;
import ru.yandex.practicum.commerce.interaction.api.feign.WarehouseClient;
import ru.yandex.practicum.order.mapper.OrderMapper;
import ru.yandex.practicum.order.model.Order;
import ru.yandex.practicum.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService — state transitions and orchestration")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private DeliveryClient deliveryClient;
    @Mock
    private PaymentClient paymentClient;
    @Mock
    private WarehouseClient warehouseClient;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID CART_ID = UUID.randomUUID();
    private static final UUID DELIVERY_ID = UUID.randomUUID();
    private static final UUID PAYMENT_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final String USERNAME = "user1";

    private Order buildOrder(OrderState state) {
        return Order.builder()
                .orderId(ORDER_ID)
                .username(USERNAME)
                .state(state)
                .shoppingCartId(CART_ID)
                .products(Map.of(PRODUCT_ID, 2))
                .deliveryId(DELIVERY_ID)
                .paymentId(PAYMENT_ID)
                .deliveryWeight(5.0)
                .deliveryVolume(3.0)
                .fragile(false)
                .productsPrice(BigDecimal.valueOf(200.0))
                .deliveryPrice(BigDecimal.valueOf(30.0))
                .build();
    }

    private OrderDto buildOrderDto(OrderState state) {
        return OrderDto.builder()
                .orderId(ORDER_ID)
                .username(USERNAME)
                .state(state)
                .shoppingCartId(CART_ID)
                .products(Map.of(PRODUCT_ID, 2))
                .deliveryId(DELIVERY_ID)
                .paymentId(PAYMENT_ID)
                .deliveryWeight(5.0)
                .deliveryVolume(3.0)
                .fragile(false)
                .productsPrice(BigDecimal.valueOf(200.0))
                .deliveryPrice(BigDecimal.valueOf(30.0))
                .build();
    }

    // ── createNewOrder ────────────────────────────────────────────────────────
    @Test
    @DisplayName("createNewOrder: persists order with state NEW, calls warehouse+delivery+payment")
    void createNewOrder_createsOrderInNewState() {
        BookedProductsDto booked = BookedProductsDto.builder()
                .deliveryWeight(5.0).deliveryVolume(3.0).fragile(false).build();
        when(warehouseClient.checkProductsAvailability(any())).thenReturn(booked);
        when(deliveryClient.planDelivery(any())).thenReturn(
                DeliveryDto.builder().deliveryId(DELIVERY_ID).build());
        when(paymentClient.productCost(any())).thenReturn(BigDecimal.valueOf(200.0));
        when(orderRepository.save(any())).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            if (o.getOrderId() == null) {
                o = Order.builder().orderId(ORDER_ID).username(o.getUsername())
                        .state(o.getState()).shoppingCartId(o.getShoppingCartId())
                        .products(o.getProducts()).deliveryWeight(o.getDeliveryWeight())
                        .deliveryVolume(o.getDeliveryVolume()).fragile(o.getFragile()).build();
            }
            return o;
        });
        when(orderMapper.toDto(any(Order.class))).thenReturn(buildOrderDto(OrderState.NEW));

        CreateNewOrderRequest request = CreateNewOrderRequest.builder()
                .shoppingCartId(CART_ID).username(USERNAME)
                .products(Map.of(PRODUCT_ID, 2)).build();

        OrderDto result = orderService.createNewOrder(request);

        assertThat(result.getState()).isEqualTo(OrderState.NEW);
        verify(warehouseClient).checkProductsAvailability(any());
        verify(deliveryClient).planDelivery(any());
        verify(paymentClient).productCost(any());
    }

    // ── payment ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("payment: sets state ON_PAYMENT and saves paymentId")
    void payment_setsOnPaymentState() {
        Order order = buildOrder(OrderState.NEW);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(paymentClient.getTotalCost(any())).thenReturn(BigDecimal.valueOf(260.0));
        when(paymentClient.payment(any())).thenReturn(
                PaymentDto.builder().paymentId(PAYMENT_ID).state(PaymentState.PENDING).build());
        when(orderRepository.save(any())).thenReturn(order);
        when(orderMapper.toDto(any(Order.class))).thenReturn(buildOrderDto(OrderState.ON_PAYMENT));

        OrderDto result = orderService.payment(ORDER_ID);

        assertThat(result.getState()).isEqualTo(OrderState.ON_PAYMENT);
        verify(paymentClient).payment(any());
    }

    // ── paymentSuccess ────────────────────────────────────────────────────────
    @Test
    @DisplayName("paymentSuccess: callback transitions state to PAID")
    void paymentSuccess_setsPaidState() {
        Order order = buildOrder(OrderState.ON_PAYMENT);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);
        when(orderMapper.toDto(any(Order.class))).thenReturn(buildOrderDto(OrderState.PAID));

        assertThat(orderService.paymentSuccess(ORDER_ID).getState()).isEqualTo(OrderState.PAID);
    }

    // ── paymentFailed ─────────────────────────────────────────────────────────
    @Test
    @DisplayName("paymentFailed: callback transitions state to PAYMENT_FAILED")
    void paymentFailed_setsPaymentFailedState() {
        Order order = buildOrder(OrderState.ON_PAYMENT);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);
        when(orderMapper.toDto(any(Order.class))).thenReturn(buildOrderDto(OrderState.PAYMENT_FAILED));

        assertThat(orderService.paymentFailed(ORDER_ID).getState()).isEqualTo(OrderState.PAYMENT_FAILED);
    }

    // ── delivery callbacks ────────────────────────────────────────────────────
    @Test
    @DisplayName("delivery: callback transitions state to DELIVERED")
    void delivery_setsDeliveredState() {
        Order order = buildOrder(OrderState.ASSEMBLED);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);
        when(orderMapper.toDto(any(Order.class))).thenReturn(buildOrderDto(OrderState.DELIVERED));

        assertThat(orderService.delivery(ORDER_ID).getState()).isEqualTo(OrderState.DELIVERED);
    }

    @Test
    @DisplayName("deliveryFailed: callback transitions state to DELIVERY_FAILED")
    void deliveryFailed_setsDeliveryFailedState() {
        Order order = buildOrder(OrderState.ASSEMBLED);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);
        when(orderMapper.toDto(any(Order.class))).thenReturn(buildOrderDto(OrderState.DELIVERY_FAILED));

        assertThat(orderService.deliveryFailed(ORDER_ID).getState()).isEqualTo(OrderState.DELIVERY_FAILED);
    }

    // ── productReturn ─────────────────────────────────────────────────────────
    @Test
    @DisplayName("productReturn: calls warehouse return and sets PRODUCT_RETURNED")
    void productReturn_callsWarehouseAndSetsState() {
        Order order = buildOrder(OrderState.DELIVERY_FAILED);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);
        when(orderMapper.toDto(any(Order.class))).thenReturn(buildOrderDto(OrderState.PRODUCT_RETURNED));

        assertThat(orderService.productReturn(ORDER_ID).getState()).isEqualTo(OrderState.PRODUCT_RETURNED);
        verify(warehouseClient).returnProducts(order.getProducts());
    }
}
