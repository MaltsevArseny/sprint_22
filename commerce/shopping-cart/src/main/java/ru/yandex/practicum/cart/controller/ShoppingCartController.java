package ru.yandex.practicum.cart.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.cart.service.ShoppingCartService;
import ru.yandex.practicum.commerce.interaction.api.dto.ShoppingCartDto;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shopping-cart")
public class ShoppingCartController {

    private final ShoppingCartService cartService;

    public ShoppingCartController(ShoppingCartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ShoppingCartDto getShoppingCart(@RequestParam String username) {
        return cartService.getShoppingCart(username);
    }

    @PostMapping("/add")
    public ShoppingCartDto addItem(
            @RequestParam String username,
            @RequestParam UUID productId,
            @RequestParam(defaultValue = "1") int quantity) {
        return cartService.addItem(username, productId, quantity);
    }

    @DeleteMapping("/remove")
    public ShoppingCartDto removeItem(@RequestParam String username, @RequestParam UUID productId) {
        return cartService.removeItem(username, productId);
    }

    @PatchMapping("/change-quantity")
    public ShoppingCartDto changeItemQuantity(
            @RequestParam String username,
            @RequestParam UUID productId,
            @RequestParam int quantity) {
        return cartService.changeItemQuantity(username, productId, quantity);
    }

    @DeleteMapping("/deactivate")
    public void deactivate(@RequestParam String username) {
        cartService.deactivate(username);
    }
}
