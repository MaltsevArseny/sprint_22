package ru.yandex.practicum.payment.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.api.dto.PaymentDto;
import ru.yandex.practicum.payment.service.PaymentService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /** Calculate product subtotal — sum(qty × price) for each item in order */
    @PostMapping("/productCost")
    public Double productCost(@RequestBody OrderDto order) {
        return paymentService.productCost(order);
    }

    /** Calculate final invoice: productSubtotal + VAT(10%) + deliveryCost */
    @PostMapping("/totalCost")
    public Double getTotalCost(@RequestBody OrderDto order) {
        return paymentService.getTotalCost(order);
    }

    /** Create a PENDING payment record for the order */
    @PostMapping
    public PaymentDto payment(@RequestBody OrderDto order) {
        return paymentService.payment(order);
    }

    /** Mark payment as SUCCESS and notify order service */
    @PutMapping("/{paymentId}/success")
    public void paymentSuccess(@PathVariable UUID paymentId) {
        paymentService.paymentSuccess(paymentId);
    }

    /** Mark payment as FAILED and notify order service */
    @PutMapping("/{paymentId}/failed")
    public void paymentFailed(@PathVariable UUID paymentId) {
        paymentService.paymentFailed(paymentId);
    }
}
