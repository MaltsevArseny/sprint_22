package ru.yandex.practicum.commerce.interaction.api.dto;

import java.util.Map;
import java.util.UUID;

public class ShoppingCartDto {

    private UUID shoppingCartId;
    private String username;
    private Map<UUID, Integer> products;

    public ShoppingCartDto() {}

    public ShoppingCartDto(UUID shoppingCartId, String username, Map<UUID, Integer> products) {
        this.shoppingCartId = shoppingCartId;
        this.username = username;
        this.products = products;
    }

    public UUID getShoppingCartId() { return shoppingCartId; }
    public void setShoppingCartId(UUID shoppingCartId) { this.shoppingCartId = shoppingCartId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Map<UUID, Integer> getProducts() { return products; }
    public void setProducts(Map<UUID, Integer> products) { this.products = products; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID shoppingCartId;
        private String username;
        private Map<UUID, Integer> products;

        public Builder shoppingCartId(UUID shoppingCartId) { this.shoppingCartId = shoppingCartId; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder products(Map<UUID, Integer> products) { this.products = products; return this; }

        public ShoppingCartDto build() {
            return new ShoppingCartDto(shoppingCartId, username, products);
        }
    }
}
