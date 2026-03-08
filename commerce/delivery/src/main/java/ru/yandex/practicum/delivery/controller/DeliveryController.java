package ru.yandex.practicum.delivery.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interaction.api.dto.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;
import ru.yandex.practicum.delivery.service.DeliveryService;

@RestController
@RequestMapping("/api/v1/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    /** Create a new delivery plan, returning the delivery record with its ID */
    @PostMapping
    public DeliveryDto planDelivery(@RequestBody OrderDto order) {
        return deliveryService.planDelivery(order);
    }

    /** Calculate delivery cost based on weight, volume, fragility, and addresses */
    @PostMapping("/cost")
    public Double deliveryCost(@RequestBody OrderDto order) {
        return deliveryService.deliveryCost(order);
    }

    /** Accept order into delivery — sets IN_PROGRESS, notifies order+warehouse */
    @PostMapping("/pickup")
    public void pickUp(@RequestBody OrderDto order) {
        deliveryService.pickUp(order);
    }

    /** Mark delivery as DELIVERED and notify order service */
    @PutMapping("/success")
    public void successDelivery(@RequestBody OrderDto order) {
        deliveryService.successDelivery(order);
    }

    /** Mark delivery as FAILED and notify order service */
    @PutMapping("/failed")
    public void failedDelivery(@RequestBody OrderDto order) {
        deliveryService.failedDelivery(order);
    }
}
