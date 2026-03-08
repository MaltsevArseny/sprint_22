package ru.yandex.practicum.warehouse.model;

import jakarta.persistence.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "order_bookings")
public class OrderBooking {

    @Id
    private UUID orderId;

    private UUID deliveryId;

    @ElementCollection
    @CollectionTable(name = "order_booking_items", joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<UUID, Integer> products = new HashMap<>();

    public OrderBooking() {}

    public OrderBooking(UUID orderId, UUID deliveryId, Map<UUID, Integer> products) {
        this.orderId = orderId;
        this.deliveryId = deliveryId;
        this.products = products != null ? products : new HashMap<>();
    }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public UUID getDeliveryId() { return deliveryId; }
    public void setDeliveryId(UUID deliveryId) { this.deliveryId = deliveryId; }

    public Map<UUID, Integer> getProducts() { return products; }
    public void setProducts(Map<UUID, Integer> products) { this.products = products; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID orderId;
        private UUID deliveryId;
        private Map<UUID, Integer> products = new HashMap<>();

        public Builder orderId(UUID orderId) { this.orderId = orderId; return this; }
        public Builder deliveryId(UUID deliveryId) { this.deliveryId = deliveryId; return this; }
        public Builder products(Map<UUID, Integer> products) { this.products = products; return this; }

        public OrderBooking build() {
            return new OrderBooking(orderId, deliveryId, products);
        }
    }
}
