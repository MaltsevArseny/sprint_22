package ru.yandex.practicum.commerce.interaction.api.dto;

import ru.yandex.practicum.commerce.interaction.api.enums.PaymentState;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentDto {

    private UUID paymentId;
    private UUID orderId;
    private BigDecimal productsTotal;
    private BigDecimal deliveryTotal;
    private BigDecimal totalPayment;
    private PaymentState state;

    public PaymentDto() {}

    public PaymentDto(UUID paymentId, UUID orderId, BigDecimal productsTotal,
                      BigDecimal deliveryTotal, BigDecimal totalPayment, PaymentState state) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.productsTotal = productsTotal;
        this.deliveryTotal = deliveryTotal;
        this.totalPayment = totalPayment;
        this.state = state;
    }

    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public BigDecimal getProductsTotal() { return productsTotal; }
    public void setProductsTotal(BigDecimal productsTotal) { this.productsTotal = productsTotal; }

    public BigDecimal getDeliveryTotal() { return deliveryTotal; }
    public void setDeliveryTotal(BigDecimal deliveryTotal) { this.deliveryTotal = deliveryTotal; }

    public BigDecimal getTotalPayment() { return totalPayment; }
    public void setTotalPayment(BigDecimal totalPayment) { this.totalPayment = totalPayment; }

    public PaymentState getState() { return state; }
    public void setState(PaymentState state) { this.state = state; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID paymentId;
        private UUID orderId;
        private BigDecimal productsTotal;
        private BigDecimal deliveryTotal;
        private BigDecimal totalPayment;
        private PaymentState state;

        public Builder paymentId(UUID paymentId) { this.paymentId = paymentId; return this; }
        public Builder orderId(UUID orderId) { this.orderId = orderId; return this; }
        public Builder productsTotal(BigDecimal productsTotal) { this.productsTotal = productsTotal; return this; }
        public Builder deliveryTotal(BigDecimal deliveryTotal) { this.deliveryTotal = deliveryTotal; return this; }
        public Builder totalPayment(BigDecimal totalPayment) { this.totalPayment = totalPayment; return this; }
        public Builder state(PaymentState state) { this.state = state; return this; }

        public PaymentDto build() {
            return new PaymentDto(paymentId, orderId, productsTotal, deliveryTotal, totalPayment, state);
        }
    }
}
