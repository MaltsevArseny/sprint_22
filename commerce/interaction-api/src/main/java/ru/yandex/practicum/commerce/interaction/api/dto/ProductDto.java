package ru.yandex.practicum.commerce.interaction.api.dto;

import ru.yandex.practicum.commerce.interaction.api.enums.ProductQuantityState;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductDto {

    private UUID productId;
    private String productName;
    private String description;
    private String imageSrc;
    private ProductQuantityState quantityState;
    private String productCategory;
    private Double rating;
    private BigDecimal price;
    private Boolean fragile;

    public ProductDto() {}

    public ProductDto(UUID productId, String productName, String description, String imageSrc,
                      ProductQuantityState quantityState, String productCategory,
                      Double rating, BigDecimal price, Boolean fragile) {
        this.productId = productId;
        this.productName = productName;
        this.description = description;
        this.imageSrc = imageSrc;
        this.quantityState = quantityState;
        this.productCategory = productCategory;
        this.rating = rating;
        this.price = price;
        this.fragile = fragile;
    }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageSrc() { return imageSrc; }
    public void setImageSrc(String imageSrc) { this.imageSrc = imageSrc; }

    public ProductQuantityState getQuantityState() { return quantityState; }
    public void setQuantityState(ProductQuantityState quantityState) { this.quantityState = quantityState; }

    public String getProductCategory() { return productCategory; }
    public void setProductCategory(String productCategory) { this.productCategory = productCategory; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Boolean getFragile() { return fragile; }
    public void setFragile(Boolean fragile) { this.fragile = fragile; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID productId;
        private String productName;
        private String description;
        private String imageSrc;
        private ProductQuantityState quantityState;
        private String productCategory;
        private Double rating;
        private BigDecimal price;
        private Boolean fragile;

        public Builder productId(UUID productId) { this.productId = productId; return this; }
        public Builder productName(String productName) { this.productName = productName; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder imageSrc(String imageSrc) { this.imageSrc = imageSrc; return this; }
        public Builder quantityState(ProductQuantityState quantityState) { this.quantityState = quantityState; return this; }
        public Builder productCategory(String productCategory) { this.productCategory = productCategory; return this; }
        public Builder rating(Double rating) { this.rating = rating; return this; }
        public Builder price(BigDecimal price) { this.price = price; return this; }
        public Builder fragile(Boolean fragile) { this.fragile = fragile; return this; }

        public ProductDto build() {
            return new ProductDto(productId, productName, description, imageSrc,
                    quantityState, productCategory, rating, price, fragile);
        }
    }
}
