package ru.yandex.practicum.delivery.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import ru.yandex.practicum.commerce.interaction.api.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.api.feign.OrderClient;
import ru.yandex.practicum.commerce.interaction.api.feign.WarehouseClient;
import ru.yandex.practicum.delivery.mapper.DeliveryMapper;
import ru.yandex.practicum.delivery.model.Address;
import ru.yandex.practicum.delivery.model.Delivery;
import ru.yandex.practicum.delivery.repository.DeliveryRepository;

@SuppressWarnings({"unused", "NullableProblems"})
@ExtendWith(MockitoExtension.class)
@DisplayName("DeliveryService — cost calculation algorithm")
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;
    @Mock
    private WarehouseClient warehouseClient;
    @Mock
    private OrderClient orderClient;
    @Mock
    private DeliveryMapper deliveryMapper;

    @InjectMocks
    private DeliveryService deliveryService;

    private static final UUID ORDER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(deliveryService, "baseCost", 5.0);
    }

    // ── Helper builders ──────────────────────────────────────────────────────
    private AddressDto warehouseAddress(String street) {
        return AddressDto.builder()
                .country("Russia").city("Moscow").street(street).house("1").flat("1")
                .build();
    }

    private OrderDto orderWith(double weight, double volume, boolean fragile, UUID orderId) {
        return OrderDto.builder()
                .orderId(orderId)
                .deliveryWeight(weight)
                .deliveryVolume(volume)
                .fragile(fragile)
                .build();
    }

    private Delivery deliveryWith(String toStreet) {
        return Delivery.builder()
                .orderId(ORDER_ID)
                .toAddress(Address.builder().street(toStreet).build())
                .build();
    }

    // ── Tests: ADDRESS_1 ────────────────────────────────────────────────────
    @Test
    @DisplayName("ADDRESS_1, not fragile, same street → cost = 10 + 0 + w*0.3 + v*0.2")
    void deliveryCost_address1_notFragile_sameStreet() {
        when(warehouseClient.getWarehouseAddress()).thenReturn(warehouseAddress("ADDRESS_1 street"));
        when(deliveryRepository.findByOrderId(ORDER_ID))
                .thenReturn(Optional.of(deliveryWith("ADDRESS_1 street")));

        OrderDto order = orderWith(4.0, 5.0, false, ORDER_ID);
        BigDecimal cost = deliveryService.deliveryCost(order);

        // step1: 5*1+5=10, step2: no fragile, step3: 4*0.3=1.2→11.2, step4: 5*0.2=1→12.2, step5: same street
        assertThat(cost).isCloseTo(new BigDecimal("12.2"), within(new BigDecimal("0.001")));
    }

    @Test
    @DisplayName("ADDRESS_2, fragile, weight=10, volume=10, different street → 27.6")
    void deliveryCost_address2_fragile_differentStreet() {
        when(warehouseClient.getWarehouseAddress()).thenReturn(warehouseAddress("ADDRESS_2 street"));
        when(deliveryRepository.findByOrderId(ORDER_ID))
                .thenReturn(Optional.of(deliveryWith("Пролетарская")));

        OrderDto order = orderWith(10.0, 10.0, true, ORDER_ID);
        BigDecimal cost = deliveryService.deliveryCost(order);

        // step1: 5*2+5=15, step2: 15+15*0.2=18, step3: 18+3=21, step4: 21+2=23, step5: 23+4.6=27.6
        assertThat(cost).isCloseTo(new BigDecimal("27.6"), within(new BigDecimal("0.001")));
    }

    @Test
    @DisplayName("ADDRESS_1, fragile, different street")
    void deliveryCost_address1_fragile_differentStreet() {
        when(warehouseClient.getWarehouseAddress()).thenReturn(warehouseAddress("ADDRESS_1 st"));
        when(deliveryRepository.findByOrderId(ORDER_ID))
                .thenReturn(Optional.of(deliveryWith("Other st")));

        // base=5, multiplier=1 → 5*1+5=10, fragile: 10*0.2=2→12, weight=0, volume=0, diff street: 12*0.2=2.4→14.4
        OrderDto order = orderWith(0.0, 0.0, true, ORDER_ID);
        BigDecimal cost = deliveryService.deliveryCost(order);

        assertThat(cost).isCloseTo(new BigDecimal("14.4"), within(new BigDecimal("0.001")));
    }

    @Test
    @DisplayName("ADDRESS_2, not fragile, same street, weight=5, volume=5")
    void deliveryCost_address2_notFragile_sameStreet() {
        when(warehouseClient.getWarehouseAddress()).thenReturn(warehouseAddress("ADDRESS_2 av"));
        when(deliveryRepository.findByOrderId(ORDER_ID))
                .thenReturn(Optional.of(deliveryWith("ADDRESS_2 av")));

        // step1: 5*2+5=15, step2: no fragile, step3: 5*0.3=1.5→16.5, step4: 5*0.2=1→17.5, step5: same street
        OrderDto order = orderWith(5.0, 5.0, false, ORDER_ID);
        BigDecimal cost = deliveryService.deliveryCost(order);

        assertThat(cost).isCloseTo(new BigDecimal("17.5"), within(new BigDecimal("0.001")));
    }

    @Test
    @DisplayName("No delivery record → default different-street surcharge applied")
    void deliveryCost_noDeliveryRecord_appliesDifferentStreetSurcharge() {
        when(warehouseClient.getWarehouseAddress()).thenReturn(warehouseAddress("ADDRESS_1 st"));
        when(deliveryRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

        // step1: 10, step2: no fragile, step3: 0, step4: 0, step5: 10*0.2=2→12
        OrderDto order = orderWith(0.0, 0.0, false, ORDER_ID);
        BigDecimal cost = deliveryService.deliveryCost(order);

        assertThat(cost).isCloseTo(new BigDecimal("12.0"), within(new BigDecimal("0.001")));
    }
}
