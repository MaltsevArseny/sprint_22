package ru.yandex.practicum.order.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interaction.api.dto.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;
import ru.yandex.practicum.order.service.OrderService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderDto createNewOrder(@RequestBody @Valid CreateNewOrderRequest request) {
        return orderService.createNewOrder(request);
    }

    @PutMapping("/{orderId}/payment")
    public OrderDto payment(@PathVariable UUID orderId) {
        return orderService.payment(orderId);
    }

    @PutMapping("/{orderId}/delivery/cost")
    public OrderDto calculateDeliveryCost(@PathVariable UUID orderId) {
        return orderService.calculateDeliveryCost(orderId);
    }

    @PutMapping("/{orderId}/total/cost")
    public OrderDto calculateTotalCost(@PathVariable UUID orderId) {
        return orderService.calculateTotalCost(orderId);
    }

    @GetMapping
    public List<OrderDto> getClientOrders(@RequestParam String username) {
        return orderService.getClientOrders(username);
    }

    @PutMapping("/{orderId}/assembly")
    public OrderDto assembly(@PathVariable UUID orderId) {
        return orderService.assembly(orderId);
    }

    @PutMapping("/{orderId}/shipped")
    public OrderDto shippedToDelivery(@PathVariable UUID orderId) {
        return orderService.shippedToDelivery(orderId);
    }

    @PutMapping("/{orderId}/payment/failed")
    public OrderDto paymentFailed(@PathVariable UUID orderId) {
        return orderService.paymentFailed(orderId);
    }

    @PutMapping("/{orderId}/assembly/failed")
    public OrderDto assemblyFailed(@PathVariable UUID orderId) {
        return orderService.assemblyFailed(orderId);
    }

    @PutMapping("/{orderId}/delivery/failed")
    public OrderDto deliveryFailed(@PathVariable UUID orderId) {
        return orderService.deliveryFailed(orderId);
    }

    @PutMapping("/{orderId}/return")
    public OrderDto productReturn(@PathVariable UUID orderId) {
        return orderService.productReturn(orderId);
    }

    @PutMapping("/{orderId}/delivery")
    public OrderDto delivery(@PathVariable UUID orderId) {
        return orderService.delivery(orderId);
    }

    @PutMapping("/{orderId}/payment/success")
    public OrderDto paymentSuccess(@PathVariable UUID orderId) {
        return orderService.paymentSuccess(orderId);
    }

    @PutMapping("/{orderId}/completed")
    public OrderDto completed(@PathVariable UUID orderId) {
        return orderService.completed(orderId);
    }
}
