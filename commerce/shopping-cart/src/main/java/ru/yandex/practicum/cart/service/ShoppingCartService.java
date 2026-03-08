package ru.yandex.practicum.cart.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.cart.model.ShoppingCart;
import ru.yandex.practicum.cart.repository.ShoppingCartRepository;
import ru.yandex.practicum.commerce.interaction.api.dto.ShoppingCartDto;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ShoppingCartService {

    private final ShoppingCartRepository cartRepository;

    public ShoppingCartService(ShoppingCartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Transactional(readOnly = true)
    public ShoppingCartDto getShoppingCart(String username) {
        ShoppingCart cart = getActiveCart(username);
        return toDto(cart);
    }

    @Transactional
    public ShoppingCartDto addItem(String username, UUID productId, int quantity) {
        ShoppingCart cart = cartRepository.findByUsernameAndActiveTrue(username).orElseGet(() ->
                cartRepository.save(ShoppingCart.builder().username(username).active(true).build()));
        cart.getProducts().merge(productId, quantity, (a, b) -> (a == null ? 0 : a) + (b == null ? 0 : b));
        return toDto(cartRepository.save(cart));
    }

    @Transactional
    public ShoppingCartDto removeItem(String username, UUID productId) {
        ShoppingCart cart = getActiveCart(username);
        cart.getProducts().remove(productId);
        return toDto(cartRepository.save(cart));
    }

    @Transactional
    public ShoppingCartDto changeItemQuantity(String username, UUID productId, int quantity) {
        ShoppingCart cart = getActiveCart(username);
        if (quantity <= 0) {
            cart.getProducts().remove(productId);
        } else {
            cart.getProducts().put(productId, quantity);
        }
        return toDto(cartRepository.save(cart));
    }

    @Transactional
    public void deactivate(String username) {
        ShoppingCart cart = getActiveCart(username);
        cart.setActive(false);
        cartRepository.save(cart);
    }

    private ShoppingCart getActiveCart(String username) {
        return cartRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new NoSuchElementException("No active cart for user: " + username));
    }

    private ShoppingCartDto toDto(ShoppingCart cart) {
        return ShoppingCartDto.builder()
                .shoppingCartId(cart.getShoppingCartId())
                .username(cart.getUsername())
                .products(cart.getProducts())
                .build();
    }
}
