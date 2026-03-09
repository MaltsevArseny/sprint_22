package ru.yandex.practicum.commerce.interaction.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interaction.api.dto.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;

import java.math.BigDecimal;

@FeignClient(name = "delivery", path = "/api/v1/delivery")
public interface DeliveryClient {

    @PostMapping
    DeliveryDto planDelivery(@RequestBody OrderDto order);

    @PostMapping("/cost")
    BigDecimal deliveryCost(@RequestBody OrderDto order);

    @PostMapping("/pickup")
    void pickUp(@RequestBody OrderDto order);

    @PutMapping("/success")
    void successDelivery(@RequestBody OrderDto order);

    @PutMapping("/failed")
    void failedDelivery(@RequestBody OrderDto order);
}
