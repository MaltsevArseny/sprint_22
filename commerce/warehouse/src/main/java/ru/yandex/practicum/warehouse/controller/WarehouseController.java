package ru.yandex.practicum.warehouse.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interaction.api.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction.api.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.interaction.api.dto.ShoppingCartDto;
import ru.yandex.practicum.warehouse.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/warehouse")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping("/address")
    public AddressDto getWarehouseAddress() {
        return warehouseService.getWarehouseAddress();
    }

    @PostMapping("/check")
    public BookedProductsDto checkProductsAvailability(@RequestBody ShoppingCartDto cart) {
        return warehouseService.checkProductsAvailability(cart);
    }

    @PostMapping("/assembly-for-order")
    public BookedProductsDto assemblyProductForOrderFromShoppingCart(
            @RequestBody ShoppingCartDto cart,
            @RequestParam UUID orderId) {
        return warehouseService.assemblyProductForOrderFromShoppingCart(cart, orderId);
    }

    @PutMapping("/shipped")
    public void shippedToDelivery(
            @RequestParam UUID deliveryId,
            @RequestParam UUID orderId) {
        warehouseService.shippedToDelivery(deliveryId, orderId);
    }

    @PostMapping("/return")
    public void returnProducts(@RequestBody Map<UUID, Integer> products) {
        warehouseService.returnProducts(products);
    }

    /** Manager endpoints: add new product to warehouse catalog */
    @PostMapping("/add")
    public void addProduct(
            @RequestParam UUID productId,
            @RequestParam Integer quantity,
            @RequestParam Double weight,
            @RequestParam Double volume,
            @RequestParam Boolean fragile) {
        warehouseService.addProduct(productId, quantity, weight, volume, fragile);
    }

    /** Accept additional stock for an existing product */
    @PutMapping("/accept")
    public void acceptProduct(@RequestParam UUID productId, @RequestParam Integer quantity) {
        warehouseService.acceptProduct(productId, quantity);
    }
}
