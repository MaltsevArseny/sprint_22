package ru.yandex.practicum.order.model;

import jakarta.persistence.*;
import ru.yandex.practicum.commerce.interaction.api.enums.OrderState;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orderId;

    @Column(nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderState state;

    private UUID shoppingCartId;
    private UUID paymentId;
    private UUID deliveryId;

    private Double deliveryWeight;
    private Double deliveryVolume;
    private Boolean fragile;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalPrice;

    @Column(precision = 19, scale = 2)
    private BigDecimal productsPrice;

    @Column(precision = 19, scale = 2)
    private BigDecimal deliveryPrice;

    @ElementCollection
    @CollectionTable(name = "order_products", joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<UUID, Integer> products = new HashMap<>();

    public Order() {}

    public Order(UUID orderId, String username, OrderState state, UUID shoppingCartId,
                 UUID paymentId, UUID deliveryId, Double deliveryWeight, Double deliveryVolume,
                 Boolean fragile, BigDecimal totalPrice, BigDecimal productsPrice, BigDecimal deliveryPrice,
                 Map<UUID, Integer> products) {
        this.orderId = orderId;
        this.username = username;
        this.state = state;
        this.shoppingCartId = shoppingCartId;
        this.paymentId = paymentId;
        this.deliveryId = deliveryId;
        this.deliveryWeight = deliveryWeight;
        this.deliveryVolume = deliveryVolume;
        this.fragile = fragile;
        this.totalPrice = totalPrice;
        this.productsPrice = productsPrice;
        this.deliveryPrice = deliveryPrice;
        this.products = products != null ? products : new HashMap<>();
    }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public OrderState getState() { return state; }
    public void setState(OrderState state) { this.state = state; }

    public UUID getShoppingCartId() { return shoppingCartId; }
    public void setShoppingCartId(UUID shoppingCartId) { this.shoppingCartId = shoppingCartId; }

    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }

    public UUID getDeliveryId() { return deliveryId; }
    public void setDeliveryId(UUID deliveryId) { this.deliveryId = deliveryId; }

    public Double getDeliveryWeight() { return deliveryWeight; }
    public void setDeliveryWeight(Double deliveryWeight) { this.deliveryWeight = deliveryWeight; }

    public Double getDeliveryVolume() { return deliveryVolume; }
    public void setDeliveryVolume(Double deliveryVolume) { this.deliveryVolume = deliveryVolume; }

    public Boolean getFragile() { return fragile; }
    public void setFragile(Boolean fragile) { this.fragile = fragile; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public BigDecimal getProductsPrice() { return productsPrice; }
    public void setProductsPrice(BigDecimal productsPrice) { this.productsPrice = productsPrice; }

    public BigDecimal getDeliveryPrice() { return deliveryPrice; }
    public void setDeliveryPrice(BigDecimal deliveryPrice) { this.deliveryPrice = deliveryPrice; }

    public Map<UUID, Integer> getProducts() { return products; }
    public void setProducts(Map<UUID, Integer> products) { this.products = products; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID orderId;
        private String username;
        private OrderState state;
        private UUID shoppingCartId;
        private UUID paymentId;
        private UUID deliveryId;
        private Double deliveryWeight;
        private Double deliveryVolume;
        private Boolean fragile;
        private BigDecimal totalPrice;
        private BigDecimal productsPrice;
        private BigDecimal deliveryPrice;
        private Map<UUID, Integer> products = new HashMap<>();

        public Builder orderId(UUID orderId) { this.orderId = orderId; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder state(OrderState state) { this.state = state; return this; }
        public Builder shoppingCartId(UUID shoppingCartId) { this.shoppingCartId = shoppingCartId; return this; }
        public Builder paymentId(UUID paymentId) { this.paymentId = paymentId; return this; }
        public Builder deliveryId(UUID deliveryId) { this.deliveryId = deliveryId; return this; }
        public Builder deliveryWeight(Double deliveryWeight) { this.deliveryWeight = deliveryWeight; return this; }
        public Builder deliveryVolume(Double deliveryVolume) { this.deliveryVolume = deliveryVolume; return this; }
        public Builder fragile(Boolean fragile) { this.fragile = fragile; return this; }
        public Builder totalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; return this; }
        public Builder productsPrice(BigDecimal productsPrice) { this.productsPrice = productsPrice; return this; }
        public Builder deliveryPrice(BigDecimal deliveryPrice) { this.deliveryPrice = deliveryPrice; return this; }
        public Builder products(Map<UUID, Integer> products) { this.products = products; return this; }

        public Order build() {
            return new Order(orderId, username, state, shoppingCartId, paymentId, deliveryId,
                    deliveryWeight, deliveryVolume, fragile, totalPrice, productsPrice, deliveryPrice, products);
        }
    }
}
