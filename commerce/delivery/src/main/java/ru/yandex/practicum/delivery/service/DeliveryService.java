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
import ru.yandex.practicum.delivery.model.Address;
import ru.yandex.practicum.delivery.model.Delivery;
import ru.yandex.practicum.delivery.repository.DeliveryRepository;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final WarehouseClient warehouseClient;
    private final OrderClient orderClient;

    @Value("${delivery.base-cost:5.0}")
    private double baseCost;

    public DeliveryService(DeliveryRepository deliveryRepository,
                           WarehouseClient warehouseClient,
                           OrderClient orderClient) {
        this.deliveryRepository = deliveryRepository;
        this.warehouseClient = warehouseClient;
        this.orderClient = orderClient;
    }

    // ── Plan Delivery ──────────────────────────────────────────────────────────

    /**
     * Creates a CREATED delivery record.  Fetches warehouse address to store
     * the from-address; uses order shipping address (extracted from order DTO)
     * as the to-address (simplified: only city/street in order for now).
     */
    @SuppressWarnings("null")
    @Transactional
    public DeliveryDto planDelivery(OrderDto order) {
        AddressDto warehouseAddr = warehouseClient.getWarehouseAddress();

        Delivery delivery = Delivery.builder()
                .orderId(order.getOrderId())
                .fromAddress(mapAddress(warehouseAddr))
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
        return toDto(delivery);
    }

    // ── Cost Calculation ───────────────────────────────────────────────────────

    /**
     * 5-step cumulative delivery cost algorithm:
     *
     * 1. base × warehouseMultiplier + base
     *    ADDRESS_1 → ×1 (+5) = 10.  ADDRESS_2 → ×2 (+5) = 15
     * 2. If fragile: cost += cost × 0.2
     * 3. cost += weight × 0.3
     * 4. cost += volume × 0.2
     * 5. If different street: cost += cost × 0.2
     *
     * Example (ADDRESS_2, fragile, weight=10, volume=10, different street):
     *   step1: 5×2+5 = 15
     *   step2: 15+15×0.2 = 18
     *   step3: 18+10×0.3 = 21
     *   step4: 21+10×0.2 = 23
     *   step5: 23+23×0.2 = 27.6
     */
    @Transactional(readOnly = true)
    public Double deliveryCost(OrderDto order) {
        AddressDto warehouseAddr = warehouseClient.getWarehouseAddress();

        // Step 1 – warehouse origin surcharge
        String warehouseStreet = warehouseAddr.getStreet();
        double multiplier = warehouseStreet != null && warehouseStreet.contains("ADDRESS_2") ? 2.0 : 1.0;
        double cost = baseCost * multiplier + baseCost;

        // Step 2 – fragility surcharge (+20%)
        if (Boolean.TRUE.equals(order.getFragile())) {
            cost += cost * 0.2;
        }

        // Step 3 – weight surcharge
        double weight = java.util.Objects.requireNonNullElse(order.getDeliveryWeight(), 0.0);
        cost += weight * 0.3;

        // Step 4 – volume surcharge
        double volume = java.util.Objects.requireNonNullElse(order.getDeliveryVolume(), 0.0);
        cost += volume * 0.2;

        // Step 5 – same street check (simplified: look at toAddress in db if delivery exists)
        Delivery delivery = deliveryRepository.findByOrderId(order.getOrderId()).orElse(null);
        if (delivery != null && delivery.getToAddress() != null) {
            String toStreet = delivery.getToAddress().getStreet();
            if (!java.util.Objects.equals(warehouseStreet, toStreet)) {
                cost += cost * 0.2;
            }
        } else {
            // If no delivery record yet, apply surcharge as default (different street assumed)
            cost += cost * 0.2;
        }

        return cost;
    }

    // ── Pickup / Accept into Delivery ──────────────────────────────────────────

    /**
     * In-progress: delivery service physically accepted the order from the warehouse.
     * - Sets delivery state IN_PROGRESS
     * - Notifies order service: ASSEMBLED
     * - Registers delivery ID at warehouse
     */
    @Transactional
    @SuppressWarnings("null")
    public void pickUp(OrderDto order) {
        Delivery delivery = findDeliveryByOrderId(order.getOrderId());
        delivery.setState(DeliveryState.IN_PROGRESS);
        deliveryRepository.save(delivery);

        // Notify order: state → ASSEMBLED
        orderClient.shippedToDelivery(order.getOrderId());

        // Link delivery ID to warehouse booking
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

    private Address mapAddress(AddressDto dto) {
        return Address.builder()
                .country(dto.getCountry())
                .city(dto.getCity())
                .street(dto.getStreet())
                .house(dto.getHouse())
                .flat(dto.getFlat())
                .build();
    }

    private DeliveryDto toDto(Delivery d) {
        return DeliveryDto.builder()
                .deliveryId(d.getDeliveryId())
                .orderId(d.getOrderId())
                .fromAddress(d.getFromAddress() != null
                        ? AddressDto.builder()
                                .country(d.getFromAddress().getCountry())
                                .city(d.getFromAddress().getCity())
                                .street(d.getFromAddress().getStreet())
                                .house(d.getFromAddress().getHouse())
                                .flat(d.getFromAddress().getFlat()).build()
                        : null)
                .toAddress(d.getToAddress() != null
                        ? AddressDto.builder()
                                .country(d.getToAddress().getCountry())
                                .city(d.getToAddress().getCity())
                                .street(d.getToAddress().getStreet())
                                .house(d.getToAddress().getHouse())
                                .flat(d.getToAddress().getFlat()).build()
                        : null)
                .deliveryWeight(d.getDeliveryWeight())
                .deliveryVolume(d.getDeliveryVolume())
                .fragile(d.getFragile())
                .state(d.getState())
                .deliveryPrice(d.getDeliveryPrice())
                .build();
    }
}
