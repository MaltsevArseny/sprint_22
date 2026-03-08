package ru.yandex.practicum.payment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.api.dto.PaymentDto;
import ru.yandex.practicum.commerce.interaction.api.enums.PaymentState;
import ru.yandex.practicum.commerce.interaction.api.feign.OrderClient;
import ru.yandex.practicum.commerce.interaction.api.feign.ShoppingStoreClient;
import ru.yandex.practicum.payment.model.Payment;
import ru.yandex.practicum.payment.repository.PaymentRepository;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderClient orderClient;

    @Value("${payment.vat-rate:0.10}")
    private double vatRate;

    public PaymentService(PaymentRepository paymentRepository,
                          ShoppingStoreClient shoppingStoreClient,
                          OrderClient orderClient) {
        this.paymentRepository = paymentRepository;
        this.shoppingStoreClient = shoppingStoreClient;
        this.orderClient = orderClient;
    }

    @Transactional(readOnly = true)
    public Double productCost(OrderDto order) {
        return order.getProducts().entrySet().stream()
                .mapToDouble(entry -> {
                    double price = shoppingStoreClient.getProduct(entry.getKey()).getPrice();
                    return price * entry.getValue();
                })
                .sum();
    }

    @Transactional(readOnly = true)
    public Double getTotalCost(OrderDto order) {
        double productsSubtotal = productCost(order);
        double vat = productsSubtotal * vatRate;
        double deliveryCost = java.util.Objects.requireNonNullElse(order.getDeliveryPrice(), 0.0);
        return productsSubtotal + vat + deliveryCost;
    }

    @Transactional
    @SuppressWarnings("null")
    public PaymentDto payment(OrderDto order) {
        double productsSubtotal = productCost(order);
        double deliveryCost = java.util.Objects.requireNonNullElse(order.getDeliveryPrice(), 0.0);
        double total = productsSubtotal + (productsSubtotal * vatRate) + deliveryCost;

        Payment payment = Payment.builder()
                .orderId(order.getOrderId())
                .productsTotal(productsSubtotal)
                .deliveryTotal(deliveryCost)
                .totalPayment(total)
                .state(PaymentState.PENDING)
                .build();
        payment = paymentRepository.save(payment);
        return toDto(payment);
    }

    @Transactional
    @SuppressWarnings("null")
    public void paymentSuccess(UUID paymentId) {
        Payment payment = findPayment(paymentId);
        payment.setState(PaymentState.SUCCESS);
        paymentRepository.save(payment);
        orderClient.paymentSuccess(payment.getOrderId());
    }

    @Transactional
    @SuppressWarnings("null")
    public void paymentFailed(UUID paymentId) {
        Payment payment = findPayment(paymentId);
        payment.setState(PaymentState.FAILED);
        paymentRepository.save(payment);
        orderClient.paymentFailed(payment.getOrderId());
    }

    private Payment findPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoSuchElementException("Payment not found: " + paymentId));
    }

    private PaymentDto toDto(Payment p) {
        return PaymentDto.builder()
                .paymentId(p.getPaymentId())
                .orderId(p.getOrderId())
                .productsTotal(p.getProductsTotal())
                .deliveryTotal(p.getDeliveryTotal())
                .totalPayment(p.getTotalPayment())
                .state(p.getState())
                .build();
    }
}
