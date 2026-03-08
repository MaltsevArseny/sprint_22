package ru.yandex.practicum.warehouse.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "warehouse_products")
public class WarehouseProduct {

    @Id
    private UUID productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private Double volume;

    @Column(nullable = false)
    private Boolean fragile;

    public WarehouseProduct() {}

    public WarehouseProduct(UUID productId, Integer quantity, Double weight, Double volume, Boolean fragile) {
        this.productId = productId;
        this.quantity = quantity;
        this.weight = weight;
        this.volume = volume;
        this.fragile = fragile;
    }

    public WarehouseProduct(UUID productId, int quantity, double weight, double volume, boolean fragile) {
        this.productId = productId;
        this.quantity = quantity;
        this.weight = weight;
        this.volume = volume;
        this.fragile = fragile;
    }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Double getVolume() { return volume; }
    public void setVolume(Double volume) { this.volume = volume; }

    public Boolean getFragile() { return fragile; }
    public void setFragile(Boolean fragile) { this.fragile = fragile; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID productId;
        private Integer quantity;
        private Double weight;
        private Double volume;
        private Boolean fragile;

        public Builder productId(UUID productId) { this.productId = productId; return this; }
        public Builder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public Builder weight(Double weight) { this.weight = weight; return this; }
        public Builder volume(Double volume) { this.volume = volume; return this; }
        public Builder fragile(Boolean fragile) { this.fragile = fragile; return this; }

        public WarehouseProduct build() {
            return new WarehouseProduct(productId, quantity, weight, volume, fragile);
        }
    }
}
