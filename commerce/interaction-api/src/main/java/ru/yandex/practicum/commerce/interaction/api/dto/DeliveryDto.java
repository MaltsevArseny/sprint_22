package ru.yandex.practicum.commerce.interaction.api.dto;

import ru.yandex.practicum.commerce.interaction.api.enums.DeliveryState;

import java.math.BigDecimal;
import java.util.UUID;

public class DeliveryDto {

    private UUID deliveryId;
    private UUID orderId;
    private AddressDto fromAddress;
    private AddressDto toAddress;
    private Double deliveryWeight;
    private Double deliveryVolume;
    private Boolean fragile;
    private DeliveryState state;
    private BigDecimal deliveryPrice;

    public DeliveryDto() {}

    public DeliveryDto(UUID deliveryId, UUID orderId, AddressDto fromAddress, AddressDto toAddress,
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

    public AddressDto getFromAddress() { return fromAddress; }
    public void setFromAddress(AddressDto fromAddress) { this.fromAddress = fromAddress; }

    public AddressDto getToAddress() { return toAddress; }
    public void setToAddress(AddressDto toAddress) { this.toAddress = toAddress; }

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
        private AddressDto fromAddress;
        private AddressDto toAddress;
        private Double deliveryWeight;
        private Double deliveryVolume;
        private Boolean fragile;
        private DeliveryState state;
        private BigDecimal deliveryPrice;

        public Builder deliveryId(UUID deliveryId) { this.deliveryId = deliveryId; return this; }
        public Builder orderId(UUID orderId) { this.orderId = orderId; return this; }
        public Builder fromAddress(AddressDto fromAddress) { this.fromAddress = fromAddress; return this; }
        public Builder toAddress(AddressDto toAddress) { this.toAddress = toAddress; return this; }
        public Builder deliveryWeight(Double deliveryWeight) { this.deliveryWeight = deliveryWeight; return this; }
        public Builder deliveryVolume(Double deliveryVolume) { this.deliveryVolume = deliveryVolume; return this; }
        public Builder fragile(Boolean fragile) { this.fragile = fragile; return this; }
        public Builder state(DeliveryState state) { this.state = state; return this; }
        public Builder deliveryPrice(BigDecimal deliveryPrice) { this.deliveryPrice = deliveryPrice; return this; }

        public DeliveryDto build() {
            return new DeliveryDto(deliveryId, orderId, fromAddress, toAddress,
                    deliveryWeight, deliveryVolume, fragile, state, deliveryPrice);
        }
    }
}
