package ru.yandex.practicum.delivery.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.api.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction.api.dto.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.api.enums.DeliveryState;
import ru.yandex.practicum.commerce.interaction.api.feign.OrderClient;
import ru.yandex.practicum.commerce.interaction.api.feign.WarehouseClient;
import ru.yandex.practicum.delivery.mapper.DeliveryMapper;
import ru.yandex.practicum.delivery.model.Address;
import ru.yandex.practicum.delivery.model.Delivery;
import ru.yandex.practicum.delivery.repository.DeliveryRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final WarehouseClient warehouseClient;
    private final OrderClient orderClient;
    private final DeliveryMapper deliveryMapper;

    @Value("${delivery.base-cost:5.0}")
    private double baseCost;

    public DeliveryService(DeliveryRepository deliveryRepository,
            WarehouseClient warehouseClient,
            OrderClient orderClient,
            DeliveryMapper deliveryMapper) {
        this.deliveryRepository = deliveryRepository;
        this.warehouseClient = warehouseClient;
        this.orderClient = orderClient;
        this.deliveryMapper = deliveryMapper;
    }

    // ── Plan Delivery ──────────────────────────────────────────────────────────
    @SuppressWarnings("null")
    @Transactional
    public DeliveryDto planDelivery(OrderDto order) {
        AddressDto warehouseAddr = warehouseClient.getWarehouseAddress();

        Delivery delivery = Delivery.builder()
                .orderId(order.getOrderId())
                .fromAddress(deliveryMapper.toAddress(warehouseAddr))
                .toAddress(Address.builder()
                        .country("Russia")
                        .city("Moscow")
                        .street("Unknown")
                        .house("1")
                        .flat("1")
                        .build())
                .deliveryWeight(order.getDeliveryWeight())
                .deliveryVolume(order.getDeliveryVolume())
                .fragile(order.getFragile())
                .state(DeliveryState.CREATED)
                .build();

        delivery = java.util.Objects.requireNonNull(deliveryRepository.save(delivery), "save() returned null");
        return deliveryMapper.toDto(delivery);
    }

    // ── Cost Calculation ───────────────────────────────────────────────────────
    /**
     * 5-step cumulative delivery cost algorithm using BigDecimal:
     *
     * 1. base × warehouseMultiplier + base ADDRESS_1 → ×1 (+5) = 10. ADDRESS_2
     * → ×2 (+5) = 15 2. If fragile: cost += cost × 0.2 3. cost += weight × 0.3
     * 4. cost += volume × 0.2 5. If different street: cost += cost × 0.2
     */
    @Transactional(readOnly = true)
    public BigDecimal deliveryCost(OrderDto order) {
        AddressDto warehouseAddr = warehouseClient.getWarehouseAddress();

        // Step 1 – warehouse origin surcharge
        String warehouseStreet = warehouseAddr.getStreet();
        double multiplier = warehouseStreet != null && warehouseStreet.contains("ADDRESS_2") ? 2.0 : 1.0;
        BigDecimal cost = BigDecimal.valueOf(baseCost * multiplier + baseCost);

        // Step 2 – fragility surcharge (+20%)
        if (Boolean.TRUE.equals(order.getFragile())) {
            cost = cost.add(cost.multiply(BigDecimal.valueOf(0.2)));
        }

        // Step 3 – weight surcharge
        double weight = order.getDeliveryWeight() != null ? order.getDeliveryWeight() : 0.0;
        cost = cost.add(BigDecimal.valueOf(weight).multiply(BigDecimal.valueOf(0.3)));

        // Step 4 – volume surcharge
        double volume = order.getDeliveryVolume() != null ? order.getDeliveryVolume() : 0.0;
        cost = cost.add(BigDecimal.valueOf(volume).multiply(BigDecimal.valueOf(0.2)));

        // Step 5 – same street check
        Delivery delivery = deliveryRepository.findByOrderId(order.getOrderId()).orElse(null);
        if (delivery != null && delivery.getToAddress() != null) {
            String toStreet = delivery.getToAddress().getStreet();
            if (!java.util.Objects.equals(warehouseStreet, toStreet)) {
                cost = cost.add(cost.multiply(BigDecimal.valueOf(0.2)));
            }
        } else {
            cost = cost.add(cost.multiply(BigDecimal.valueOf(0.2)));
        }

        return cost.setScale(2, RoundingMode.HALF_UP);
    }

    // ── Pickup / Accept into Delivery ──────────────────────────────────────────
    @Transactional
    @SuppressWarnings("null")
    public void pickUp(OrderDto order) {
        Delivery delivery = findDeliveryByOrderId(order.getOrderId());
        delivery.setState(DeliveryState.IN_PROGRESS);
        deliveryRepository.save(delivery);

        orderClient.shippedToDelivery(order.getOrderId());
        warehouseClient.shippedToDelivery(delivery.getDeliveryId(), order.getOrderId());
    }

    // ── Callbacks ─────────────────────────────────────────────────────────────
    @Transactional
    @SuppressWarnings("null")
    public void successDelivery(OrderDto order) {
        Delivery delivery = findDeliveryByOrderId(order.getOrderId());
        delivery.setState(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);
        orderClient.delivery(order.getOrderId());
    }

    @Transactional
    @SuppressWarnings("null")
    public void failedDelivery(OrderDto order) {
        Delivery delivery = findDeliveryByOrderId(order.getOrderId());
        delivery.setState(DeliveryState.FAILED);
        deliveryRepository.save(delivery);
        orderClient.deliveryFailed(order.getOrderId());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private Delivery findDeliveryByOrderId(UUID orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoSuchElementException("Delivery not found for order: " + orderId));
    }
}
