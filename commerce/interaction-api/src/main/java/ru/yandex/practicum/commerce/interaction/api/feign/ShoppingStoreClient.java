package ru.yandex.practicum.commerce.interaction.api.feign;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interaction.api.dto.ProductDto;

@FeignClient(name = "shopping-store", path = "/api/v1/shopping-store")
public interface ShoppingStoreClient {

    @GetMapping("/{productId}")
    ProductDto getProduct(@PathVariable UUID productId);
}
