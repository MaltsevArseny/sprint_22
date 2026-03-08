package ru.yandex.practicum.commerce.interaction.api.feign;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interaction.api.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction.api.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.interaction.api.dto.ShoppingCartDto;

@FeignClient(name = "warehouse", path = "/api/v1/warehouse")
public interface WarehouseClient {

    @PostMapping("/check")
    BookedProductsDto checkProductsAvailability(@RequestBody ShoppingCartDto cart);

    @GetMapping("/address")
    AddressDto getWarehouseAddress();

    @PostMapping("/assembly-for-order")
    BookedProductsDto assemblyProductForOrderFromShoppingCart(
            @RequestBody ShoppingCartDto cart,
            @RequestParam UUID orderId);

    @PutMapping("/shipped")
    void shippedToDelivery(
            @RequestParam UUID deliveryId,
            @RequestParam UUID orderId);

    @PostMapping("/return")
    void returnProducts(@RequestBody Map<UUID, Integer> products);
}
