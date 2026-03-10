package ru.yandex.practicum.payment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.api.dto.PaymentDto;
import ru.yandex.practicum.commerce.interaction.api.enums.PaymentState;
import ru.yandex.practicum.commerce.interaction.api.feign.OrderClient;
import ru.yandex.practicum.commerce.interaction.api.feign.ShoppingStoreClient;
import ru.yandex.practicum.payment.mapper.PaymentMapper;
import ru.yandex.practicum.payment.model.Payment;
import ru.yandex.practicum.payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderClient orderClient;
    private final PaymentMapper paymentMapper;

    @Value("${payment.vat-rate:0.10}")
    private double vatRate;

    public PaymentService(PaymentRepository paymentRepository,
            ShoppingStoreClient shoppingStoreClient,
            OrderClient orderClient,
            PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.shoppingStoreClient = shoppingStoreClient;
        this.orderClient = orderClient;
        this.paymentMapper = paymentMapper;
    }

    @Transactional(readOnly = true)
    public BigDecimal productCost(OrderDto order) {
        return order.getProducts().entrySet().stream()
                .map(entry -> {
                    BigDecimal price = shoppingStoreClient.getProduct(entry.getKey()).getPrice();
                    return price.multiply(BigDecimal.valueOf(entry.getValue()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalCost(OrderDto order) {
        BigDecimal productsSubtotal = productCost(order);
        BigDecimal vat = productsSubtotal.multiply(BigDecimal.valueOf(vatRate));
        BigDecimal deliveryCost = order.getDeliveryPrice() != null ? order.getDeliveryPrice() : BigDecimal.ZERO;
        return productsSubtotal.add(vat).add(deliveryCost).setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    @SuppressWarnings("null")
    public PaymentDto payment(OrderDto order) {
        BigDecimal productsSubtotal = productCost(order);
        BigDecimal deliveryCost = order.getDeliveryPrice() != null ? order.getDeliveryPrice() : BigDecimal.ZERO;
        BigDecimal vat = productsSubtotal.multiply(BigDecimal.valueOf(vatRate));
        BigDecimal total = productsSubtotal.add(vat).add(deliveryCost).setScale(2, RoundingMode.HALF_UP);

        Payment payment = Payment.builder()
                .orderId(order.getOrderId())
                .productsTotal(productsSubtotal.setScale(2, RoundingMode.HALF_UP))
                .deliveryTotal(deliveryCost.setScale(2, RoundingMode.HALF_UP))
                .totalPayment(total)
                .state(PaymentState.PENDING)
                .build();
        payment = paymentRepository.save(payment);
        return paymentMapper.toDto(payment);
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
}
