package ru.yandex.practicum.warehouse.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.commerce.interaction.api.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.interaction.api.dto.ShoppingCartDto;
import ru.yandex.practicum.warehouse.config.WarehouseAddressProperties;
import ru.yandex.practicum.warehouse.model.WarehouseProduct;
import ru.yandex.practicum.warehouse.repository.OrderBookingRepository;
import ru.yandex.practicum.warehouse.repository.WarehouseProductRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"null", "unused"})
@ExtendWith(MockitoExtension.class)
@DisplayName("WarehouseService — inventory management")
class WarehouseServiceTest {

    @Mock private WarehouseProductRepository productRepository;
    @Mock private OrderBookingRepository bookingRepository;
    @Mock private WarehouseAddressProperties addressProperties;

    @InjectMocks private WarehouseService warehouseService;

    private static final UUID PRODUCT_1 = UUID.randomUUID();
    private static final UUID PRODUCT_2 = UUID.randomUUID();
    private static final UUID ORDER_ID  = UUID.randomUUID();

    private WarehouseProduct product(UUID id, int qty, double weight, double volume, boolean fragile) {
        return new WarehouseProduct(id, qty, weight, volume, fragile);
    }

    private ShoppingCartDto cart(Map<UUID, Integer> products) {
        ShoppingCartDto dto = new ShoppingCartDto();
        dto.setProducts(products);
        return dto;
    }

    // ── checkProductsAvailability ─────────────────────────────────────────────

    @Test
    @DisplayName("checkProducts: returns correct weight/volume/fragile aggregation")
    void checkProductsAvailability_returnsCorrectMetrics() {
        WarehouseProduct p1 = product(PRODUCT_1, 10, 2.0, 1.0, false);
        WarehouseProduct p2 = product(PRODUCT_2, 10, 3.0, 2.0, true);
        when(productRepository.findByProductIdIn(anyCollection()))
                .thenReturn(List.of(p1, p2));

        // qty: p1=2, p2=3
        ShoppingCartDto cart = cart(Map.of(PRODUCT_1, 2, PRODUCT_2, 3));
        BookedProductsDto result = warehouseService.checkProductsAvailability(cart);

        // totalWeight = 2*2.0 + 3*3.0 = 4 + 9 = 13
        assertThat(result.getDeliveryWeight()).isCloseTo(13.0, within(0.001));
        // totalVolume = 2*1.0 + 3*2.0 = 2 + 6 = 8
        assertThat(result.getDeliveryVolume()).isCloseTo(8.0, within(0.001));
        // fragile = true (p2 is fragile)
        assertThat(result.getFragile()).isTrue();
    }

    @Test
    @DisplayName("checkProducts: non-fragile when all products are non-fragile")
    void checkProductsAvailability_notFragileWhenNoneAreFragile() {
        when(productRepository.findByProductIdIn(anyCollection()))
                .thenReturn(List.of(product(PRODUCT_1, 10, 1.0, 1.0, false)));

        BookedProductsDto result = warehouseService.checkProductsAvailability(
                cart(Map.of(PRODUCT_1, 1)));

        assertThat(result.getFragile()).isFalse();
    }

    // ── assemblyProductForOrderFromShoppingCart ───────────────────────────────

    @Test
    @DisplayName("assembly: deducts stock and creates booking record")
    void assembly_deductsStockAndCreatesBooking() {
        WarehouseProduct p1 = product(PRODUCT_1, 10, 2.0, 1.0, false);
        when(productRepository.findByProductIdIn(anyCollection())).thenReturn(List.of(p1));
        when(productRepository.save(any())).thenReturn(p1);
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        warehouseService.assemblyProductForOrderFromShoppingCart(
                cart(Map.of(PRODUCT_1, 3)), ORDER_ID);

        // Stock should have been deducted: 10 - 3 = 7
        assertThat(p1.getQuantity()).isEqualTo(7);
        verify(bookingRepository).save(any());
    }

    @Test
    @DisplayName("assembly: throws when insufficient stock")
    void assembly_throwsWhenInsufficientStock() {
        WarehouseProduct p1 = product(PRODUCT_1, 2, 1.0, 1.0, false);
        when(productRepository.findByProductIdIn(anyCollection())).thenReturn(List.of(p1));

        assertThatThrownBy(() ->
                warehouseService.assemblyProductForOrderFromShoppingCart(
                        cart(Map.of(PRODUCT_1, 5)), ORDER_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient stock");
    }

    // ── returnProducts ────────────────────────────────────────────────────────

    @Test
    @DisplayName("returnProducts: increases stock by returned quantity")
    void returnProducts_increasesStock() {
        WarehouseProduct p1 = product(PRODUCT_1, 5, 1.0, 1.0, false);
        when(productRepository.findById(PRODUCT_1)).thenReturn(java.util.Optional.of(p1));
        when(productRepository.save(any())).thenReturn(p1);

        warehouseService.returnProducts(Map.of(PRODUCT_1, 3));

        assertThat(p1.getQuantity()).isEqualTo(8);
    }
}
