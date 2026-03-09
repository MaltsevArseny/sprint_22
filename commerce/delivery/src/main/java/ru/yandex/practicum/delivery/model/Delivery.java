package ru.yandex.practicum.delivery.model;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import ru.yandex.practicum.commerce.interaction.api.enums.DeliveryState;

@Entity
@Table(name = "deliveries")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID deliveryId;

    @Column(nullable = false)
    private UUID orderId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "country", column = @Column(name = "from_country")),
            @AttributeOverride(name = "city",    column = @Column(name = "from_city")),
            @AttributeOverride(name = "street",  column = @Column(name = "from_street")),
            @AttributeOverride(name = "house",   column = @Column(name = "from_house")),
            @AttributeOverride(name = "flat",    column = @Column(name = "from_flat"))
    })
    private Address fromAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "country", column = @Column(name = "to_country")),
            @AttributeOverride(name = "city",    column = @Column(name = "to_city")),
            @AttributeOverride(name = "street",  column = @Column(name = "to_street")),
            @AttributeOverride(name = "house",   column = @Column(name = "to_house")),
            @AttributeOverride(name = "flat",    column = @Column(name = "to_flat"))
    })
    private Address toAddress;

    private Double deliveryWeight;
    private Double deliveryVolume;
    private Boolean fragile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryState state;

    @Column(precision = 19, scale = 2)
    private BigDecimal deliveryPrice;

    public Delivery() {}

    public Delivery(UUID deliveryId, UUID orderId, Address fromAddress, Address toAddress,
                    Double deliveryWeight, Double deliveryVolume, Boolean fragile,
                    DeliveryState state, BigDecimal deliveryPrice) {
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.deliveryWeight = deliveryWeight;
        this.deliveryVolume = deliveryVolume;
        this.fragile = fragile;
        this.state = state;
        this.deliveryPrice = deliveryPrice;
    }

    public UUID getDeliveryId() { return deliveryId; }
    public void setDeliveryId(UUID deliveryId) { this.deliveryId = deliveryId; }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public Address getFromAddress() { return fromAddress; }
    public void setFromAddress(Address fromAddress) { this.fromAddress = fromAddress; }

    public Address getToAddress() { return toAddress; }
    public void setToAddress(Address toAddress) { this.toAddress = toAddress; }

    public Double getDeliveryWeight() { return deliveryWeight; }
    public void setDeliveryWeight(Double deliveryWeight) { this.deliveryWeight = deliveryWeight; }

    public Double getDeliveryVolume() { return deliveryVolume; }
    public void setDeliveryVolume(Double deliveryVolume) { this.deliveryVolume = deliveryVolume; }

    public Boolean getFragile() { return fragile; }
    public void setFragile(Boolean fragile) { this.fragile = fragile; }

    public DeliveryState getState() { return state; }
    public void setState(DeliveryState state) { this.state = state; }

    public BigDecimal getDeliveryPrice() { return deliveryPrice; }
    public void setDeliveryPrice(BigDecimal deliveryPrice) { this.deliveryPrice = deliveryPrice; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID deliveryId;
        private UUID orderId;
        private Address fromAddress;
        private Address toAddress;
        private Double deliveryWeight;
        private Double deliveryVolume;
        private Boolean fragile;
        private DeliveryState state;
        private BigDecimal deliveryPrice;

        public Builder deliveryId(UUID deliveryId) { this.deliveryId = deliveryId; return this; }
        public Builder orderId(UUID orderId) { this.orderId = orderId; return this; }
        public Builder fromAddress(Address fromAddress) { this.fromAddress = fromAddress; return this; }
        public Builder toAddress(Address toAddress) { this.toAddress = toAddress; return this; }
        public Builder deliveryWeight(Double deliveryWeight) { this.deliveryWeight = deliveryWeight; return this; }
        public Builder deliveryVolume(Double deliveryVolume) { this.deliveryVolume = deliveryVolume; return this; }
        public Builder fragile(Boolean fragile) { this.fragile = fragile; return this; }
        public Builder state(DeliveryState state) { this.state = state; return this; }
        public Builder deliveryPrice(BigDecimal deliveryPrice) { this.deliveryPrice = deliveryPrice; return this; }

        public Delivery build() {
            return new Delivery(deliveryId, orderId, fromAddress, toAddress,
                    deliveryWeight, deliveryVolume, fragile, state, deliveryPrice);
        }
    }
}
