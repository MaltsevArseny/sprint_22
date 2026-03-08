package ru.yandex.practicum.commerce.interaction.api.feign;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interaction.api.dto.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;

@FeignClient(name = "order", path = "/api/v1/order")
public interface OrderClient {

    @PostMapping
    OrderDto createNewOrder(@RequestBody @Valid CreateNewOrderRequest request);

    @PutMapping("/{orderId}/payment")
    OrderDto payment(@PathVariable UUID orderId);

    @PutMapping("/{orderId}/delivery/cost")
    OrderDto calculateDeliveryCost(@PathVariable UUID orderId);

    @PutMapping("/{orderId}/total/cost")
    OrderDto calculateTotalCost(@PathVariable UUID orderId);

    @GetMapping
    List<OrderDto> getClientOrders(@RequestParam String username);

    @PutMapping("/{orderId}/assembly")
    OrderDto assembly(@PathVariable UUID orderId);

    @PutMapping("/{orderId}/shipped")
    OrderDto shippedToDelivery(@PathVariable UUID orderId);

    @PutMapping("/{orderId}/payment/failed")
    OrderDto paymentFailed(@PathVariable UUID orderId);

    @PutMapping("/{orderId}/assembly/failed")
    OrderDto assemblyFailed(@PathVariable UUID orderId);

    @PutMapping("/{orderId}/delivery/failed")
    OrderDto deliveryFailed(@PathVariable UUID orderId);

    @PutMapping("/{orderId}/return")
    OrderDto productReturn(@PathVariable UUID orderId);

    @PutMapping("/{orderId}/delivery")
    OrderDto delivery(@PathVariable UUID orderId);

    @PutMapping("/{orderId}/payment/success")
    OrderDto paymentSuccess(@PathVariable UUID orderId);

    @PutMapping("/{orderId}/completed")
    OrderDto completed(@PathVariable UUID orderId);
}
