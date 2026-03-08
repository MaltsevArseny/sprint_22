package ru.yandex.practicum.commerce.interaction.api.feign;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.api.dto.PaymentDto;

@FeignClient(name = "payment", path = "/api/v1/payment")
public interface PaymentClient {

    @PostMapping("/productCost")
    Double productCost(@RequestBody OrderDto order);

    @PostMapping("/totalCost")
    Double getTotalCost(@RequestBody OrderDto order);

    @PostMapping
    PaymentDto payment(@RequestBody OrderDto order);

    @PutMapping("/{paymentId}/success")
    void paymentSuccess(@PathVariable UUID paymentId);

    @PutMapping("/{paymentId}/failed")
    void paymentFailed(@PathVariable UUID paymentId);
}
