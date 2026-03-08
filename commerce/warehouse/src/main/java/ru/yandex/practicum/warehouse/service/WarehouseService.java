package ru.yandex.practicum.warehouse.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.api.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction.api.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.interaction.api.dto.ShoppingCartDto;
import ru.yandex.practicum.warehouse.config.WarehouseAddressProperties;
import ru.yandex.practicum.warehouse.model.OrderBooking;
import ru.yandex.practicum.warehouse.model.WarehouseProduct;
import ru.yandex.practicum.warehouse.repository.OrderBookingRepository;
import ru.yandex.practicum.warehouse.repository.WarehouseProductRepository;

import java.util.*;

@Service
public class WarehouseService {

    private final WarehouseProductRepository productRepository;
    private final OrderBookingRepository bookingRepository;
    private final WarehouseAddressProperties addressProperties;

    public WarehouseService(WarehouseProductRepository productRepository,
                            OrderBookingRepository bookingRepository,
                            WarehouseAddressProperties addressProperties) {
        this.productRepository = productRepository;
        this.bookingRepository = bookingRepository;
        this.addressProperties = addressProperties;
    }

    @Transactional(readOnly = true)
    public AddressDto getWarehouseAddress() {
        return AddressDto.builder()
                .country(addressProperties.getCountry())
                .city(addressProperties.getCity())
                .street(addressProperties.getStreet())
                .house(addressProperties.getHouse())
                .flat(addressProperties.getFlat())
                .build();
    }

    @Transactional
    @SuppressWarnings("null")
    public WarehouseProduct addProduct(UUID productId, Integer quantity, Double weight,
                                       Double volume, Boolean fragile) {
        WarehouseProduct p = WarehouseProduct.builder()
                .productId(productId)
                .quantity(quantity)
                .weight(weight)
                .volume(volume)
                .fragile(fragile)
                .build();
        return productRepository.save(p);
    }

    @Transactional
    @SuppressWarnings("null")
    public void acceptProduct(UUID productId, Integer quantity) {
        WarehouseProduct p = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not in warehouse: " + productId));
        p.setQuantity(p.getQuantity() + quantity);
        productRepository.save(p);
    }

    @Transactional(readOnly = true)
    public BookedProductsDto checkProductsAvailability(ShoppingCartDto cart) {
        return calculateBooking(cart.getProducts());
    }

    @Transactional
    @SuppressWarnings("null")
    public BookedProductsDto assemblyProductForOrderFromShoppingCart(ShoppingCartDto cart, UUID orderId) {
        Map<UUID, Integer> products = cart.getProducts();
        List<WarehouseProduct> warehouseItems = productRepository.findByProductIdIn(products.keySet());
        validateAndDeduct(warehouseItems, products);

        OrderBooking booking = OrderBooking.builder()
                .orderId(orderId)
                .products(new HashMap<>(products))
                .build();
        bookingRepository.save(booking);

        return calculateBookingFromItems(warehouseItems, products);
    }

    @Transactional
    @SuppressWarnings("null")
    public void shippedToDelivery(UUID deliveryId, UUID orderId) {
        OrderBooking booking = bookingRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order booking not found: " + orderId));
        booking.setDeliveryId(deliveryId);
        bookingRepository.save(booking);
    }

    @Transactional
    @SuppressWarnings("null")
    public void returnProducts(Map<UUID, Integer> products) {
        products.forEach((productId, quantity) ->
                productRepository.findById(productId).ifPresent(p -> {
                    p.setQuantity(p.getQuantity() + quantity);
                    productRepository.save(p);
                }));
    }

    // ----- private helpers -----

    private BookedProductsDto calculateBooking(Map<UUID, Integer> products) {
        List<WarehouseProduct> items = productRepository.findByProductIdIn(products.keySet());
        if (items.size() < products.size()) {
            throw new NoSuchElementException("Some products not found in warehouse");
        }
        return calculateBookingFromItems(items, products);
    }

    private BookedProductsDto calculateBookingFromItems(List<WarehouseProduct> items,
                                                        Map<UUID, Integer> products) {
        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean anyFragile = false;
        for (WarehouseProduct item : items) {
            int qty = products.getOrDefault(item.getProductId(), 0);
            totalWeight += item.getWeight() * qty;
            totalVolume += item.getVolume() * qty;
            if (Boolean.TRUE.equals(item.getFragile())) anyFragile = true;
        }
        return BookedProductsDto.builder()
                .deliveryWeight(totalWeight)
                .deliveryVolume(totalVolume)
                .fragile(anyFragile)
                .build();
    }

    private void validateAndDeduct(List<WarehouseProduct> items, Map<UUID, Integer> products) {
        for (WarehouseProduct item : items) {
            int required = products.getOrDefault(item.getProductId(), 0);
            if (item.getQuantity() < required) {
                throw new IllegalStateException(
                        "Insufficient stock for product " + item.getProductId()
                                + ": required " + required + " but only " + item.getQuantity() + " available");
            }
            item.setQuantity(item.getQuantity() - required);
            productRepository.save(item);
        }
    }
}
