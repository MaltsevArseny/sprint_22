package ru.yandex.practicum.order.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.api.dto.*;
import ru.yandex.practicum.commerce.interaction.api.enums.OrderState;
import ru.yandex.practicum.commerce.interaction.api.feign.DeliveryClient;
import ru.yandex.practicum.commerce.interaction.api.feign.PaymentClient;
import ru.yandex.practicum.commerce.interaction.api.feign.WarehouseClient;
import ru.yandex.practicum.order.model.Order;
import ru.yandex.practicum.order.repository.OrderRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final DeliveryClient deliveryClient;
    private final PaymentClient paymentClient;
    private final WarehouseClient warehouseClient;

    public OrderService(OrderRepository orderRepository,
                        DeliveryClient deliveryClient,
                        PaymentClient paymentClient,
                        WarehouseClient warehouseClient) {
        this.orderRepository = orderRepository;
        this.deliveryClient = deliveryClient;
        this.paymentClient = paymentClient;
        this.warehouseClient = warehouseClient;
    }

    @Transactional
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        ShoppingCartDto cart = ShoppingCartDto.builder()
                .shoppingCartId(request.getShoppingCartId())
                .username(request.getUsername())
                .products(request.getProducts())
                .build();

        BookedProductsDto booked = warehouseClient.checkProductsAvailability(cart);

        Order order = Order.builder()
                .username(request.getUsername())
                .shoppingCartId(request.getShoppingCartId())
                .products(request.getProducts())
                .state(OrderState.NEW)
                .deliveryWeight(booked.getDeliveryWeight())
                .deliveryVolume(booked.getDeliveryVolume())
                .fragile(booked.getFragile())
                .build();
        order = orderRepository.save(order);

        DeliveryDto delivery = deliveryClient.planDelivery(toDto(order));
        order.setDeliveryId(delivery.getDeliveryId());

        double productsPrice = paymentClient.productCost(toDto(order));
        order.setProductsPrice(productsPrice);

        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto payment(UUID orderId) {
        Order order = findOrder(orderId);
        double totalPrice = paymentClient.getTotalCost(toDto(order));
        order.setTotalPrice(totalPrice);

        PaymentDto payment = paymentClient.payment(toDto(order));
        order.setPaymentId(payment.getPaymentId());
        order.setState(OrderState.ON_PAYMENT);
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto calculateDeliveryCost(UUID orderId) {
        Order order = findOrder(orderId);
        double deliveryPrice = deliveryClient.deliveryCost(toDto(order));
        order.setDeliveryPrice(deliveryPrice);
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto calculateTotalCost(UUID orderId) {
        Order order = findOrder(orderId);
        double total = paymentClient.getTotalCost(toDto(order));
        order.setTotalPrice(total);
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto assembly(UUID orderId) {
        Order order = findOrder(orderId);
        ShoppingCartDto cart = ShoppingCartDto.builder()
                .shoppingCartId(order.getShoppingCartId())
                .username(order.getUsername())
                .products(order.getProducts())
                .build();
        warehouseClient.assemblyProductForOrderFromShoppingCart(cart, orderId);
        order.setState(OrderState.ON_DELIVERY);
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto shippedToDelivery(UUID orderId) {
        Order order = findOrder(orderId);
        order.setState(OrderState.ASSEMBLED);
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto paymentSuccess(UUID orderId) {
        Order order = findOrder(orderId);
        order.setState(OrderState.PAID);
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto paymentFailed(UUID orderId) {
        Order order = findOrder(orderId);
        order.setState(OrderState.PAYMENT_FAILED);
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto delivery(UUID orderId) {
        Order order = findOrder(orderId);
        order.setState(OrderState.DELIVERED);
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto deliveryFailed(UUID orderId) {
        Order order = findOrder(orderId);
        order.setState(OrderState.DELIVERY_FAILED);
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto assemblyFailed(UUID orderId) {
        Order order = findOrder(orderId);
        order.setState(OrderState.ASSEMBLY_FAILED);
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto productReturn(UUID orderId) {
        Order order = findOrder(orderId);
        warehouseClient.returnProducts(order.getProducts());
        order.setState(OrderState.PRODUCT_RETURNED);
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto completed(UUID orderId) {
        Order order = findOrder(orderId);
        order.setState(OrderState.COMPLETED);
        return toDto(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getClientOrders(String username) {
        return orderRepository.findByUsername(username)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private Order findOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));
    }

    public OrderDto toDto(Order order) {
        return OrderDto.builder()
                .orderId(order.getOrderId())
                .username(order.getUsername())
                .state(order.getState())
                .shoppingCartId(order.getShoppingCartId())
                .products(order.getProducts())
                .paymentId(order.getPaymentId())
                .deliveryId(order.getDeliveryId())
                .deliveryWeight(order.getDeliveryWeight())
                .deliveryVolume(order.getDeliveryVolume())
                .fragile(order.getFragile())
                .totalPrice(order.getTotalPrice())
                .productsPrice(order.getProductsPrice())
                .deliveryPrice(order.getDeliveryPrice())
                .build();
    }
}
