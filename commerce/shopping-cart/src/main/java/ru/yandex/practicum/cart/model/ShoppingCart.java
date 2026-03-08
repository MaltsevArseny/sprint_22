package ru.yandex.practicum.cart.model;

import jakarta.persistence.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "shopping_carts")
public class ShoppingCart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID shoppingCartId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Boolean active;

    @ElementCollection
    @CollectionTable(name = "cart_items", joinColumns = @JoinColumn(name = "cart_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<UUID, Integer> products = new HashMap<>();

    public ShoppingCart() {}

    public ShoppingCart(UUID shoppingCartId, String username, Boolean active, Map<UUID, Integer> products) {
        this.shoppingCartId = shoppingCartId;
        this.username = username;
        this.active = active;
        this.products = products != null ? products : new HashMap<>();
    }

    public UUID getShoppingCartId() { return shoppingCartId; }
    public void setShoppingCartId(UUID shoppingCartId) { this.shoppingCartId = shoppingCartId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Map<UUID, Integer> getProducts() { return products; }
    public void setProducts(Map<UUID, Integer> products) { this.products = products; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID shoppingCartId;
        private String username;
        private Boolean active;
        private Map<UUID, Integer> products = new HashMap<>();

        public Builder shoppingCartId(UUID shoppingCartId) { this.shoppingCartId = shoppingCartId; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder active(Boolean active) { this.active = active; return this; }
        public Builder products(Map<UUID, Integer> products) { this.products = products; return this; }

        public ShoppingCart build() {
            return new ShoppingCart(shoppingCartId, username, active, products);
        }
    }
}
