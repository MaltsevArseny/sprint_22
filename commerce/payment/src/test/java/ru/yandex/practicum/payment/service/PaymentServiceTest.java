package ru.yandex.practicum.payment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.api.dto.PaymentDto;
import ru.yandex.practicum.commerce.interaction.api.dto.ProductDto;
import ru.yandex.practicum.commerce.interaction.api.enums.PaymentState;
import ru.yandex.practicum.commerce.interaction.api.feign.OrderClient;
import ru.yandex.practicum.commerce.interaction.api.feign.ShoppingStoreClient;
import ru.yandex.practicum.payment.mapper.PaymentMapper;
import ru.yandex.practicum.payment.model.Payment;
import ru.yandex.practicum.payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService — VAT cost calculations")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ShoppingStoreClient shoppingStoreClient;
    @Mock
    private OrderClient orderClient;
    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentService paymentService;

    private static final UUID PRODUCT_1 = UUID.randomUUID();
    private static final UUID PRODUCT_2 = UUID.randomUUID();
    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID PAYMENT_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "vatRate", 0.10);
    }

    private ProductDto product(double price) {
        return ProductDto.builder().price(BigDecimal.valueOf(price)).build();
    }

    private OrderDto order(double deliveryPrice) {
        return OrderDto.builder()
                .orderId(ORDER_ID)
                .products(Map.of(PRODUCT_1, 2, PRODUCT_2, 3))
                .deliveryPrice(BigDecimal.valueOf(deliveryPrice))
                .build();
    }

    // ── productCost ──────────────────────────────────────────────────────────
    @Test
    @DisplayName("productCost: 2×50 + 3×30 = 190")
    void productCost_calculatesCorrectly() {
        when(shoppingStoreClient.getProduct(PRODUCT_1)).thenReturn(product(50.0));
        when(shoppingStoreClient.getProduct(PRODUCT_2)).thenReturn(product(30.0));

        BigDecimal cost = paymentService.productCost(order(0.0));

        // 2*50 + 3*30 = 100 + 90 = 190
        assertThat(cost).isCloseTo(new BigDecimal("190.0"), within(new BigDecimal("0.001")));
    }

    @Test
    @DisplayName("productCost: single product, quantity=1")
    void productCost_singleProduct() {
        OrderDto singleOrder = OrderDto.builder()
                .orderId(ORDER_ID)
                .products(Map.of(PRODUCT_1, 1))
                .deliveryPrice(BigDecimal.ZERO)
                .build();
        when(shoppingStoreClient.getProduct(PRODUCT_1)).thenReturn(product(100.0));

        BigDecimal cost = paymentService.productCost(singleOrder);

        assertThat(cost).isCloseTo(new BigDecimal("100.0"), within(new BigDecimal("0.001")));
    }

    // ── getTotalCost ──────────────────────────────────────────────────────────
    @Test
    @DisplayName("getTotalCost: products=100, delivery=50 → 100+10+50=160")
    void getTotalCost_tz_example() {
        OrderDto singleOrder = OrderDto.builder()
                .orderId(ORDER_ID)
                .products(Map.of(PRODUCT_1, 2))
                .deliveryPrice(BigDecimal.valueOf(50.0))
                .build();
        when(shoppingStoreClient.getProduct(PRODUCT_1)).thenReturn(product(50.0));

        BigDecimal total = paymentService.getTotalCost(singleOrder);

        // products=100, VAT=10, delivery=50 → 160
        assertThat(total).isCloseTo(new BigDecimal("160.0"), within(new BigDecimal("0.001")));
    }

    @Test
    @DisplayName("getTotalCost: no delivery cost → adds only VAT")
    void getTotalCost_noDelivery() {
        when(shoppingStoreClient.getProduct(PRODUCT_1)).thenReturn(product(50.0));
        when(shoppingStoreClient.getProduct(PRODUCT_2)).thenReturn(product(30.0));

        BigDecimal total = paymentService.getTotalCost(order(0.0));

        // products=190, VAT=19, delivery=0 → 209
        assertThat(total).isCloseTo(new BigDecimal("209.0"), within(new BigDecimal("0.001")));
    }

    // ── payment() ────────────────────────────────────────────────────────────
    @Test
    @DisplayName("payment: saves Payment with PENDING state and correct amounts")
    void payment_savesWithPendingState() {
        when(shoppingStoreClient.getProduct(PRODUCT_1)).thenReturn(product(50.0));
        when(shoppingStoreClient.getProduct(PRODUCT_2)).thenReturn(product(30.0));
        when(paymentRepository.save(any())).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p = Payment.builder()
                    .paymentId(PAYMENT_ID)
                    .orderId(p.getOrderId())
                    .productsTotal(p.getProductsTotal())
                    .deliveryTotal(p.getDeliveryTotal())
                    .totalPayment(p.getTotalPayment())
                    .state(p.getState())
                    .build();
            return p;
        });
        when(paymentMapper.toDto(any())).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            return PaymentDto.builder()
                    .paymentId(p.getPaymentId())
                    .orderId(p.getOrderId())
                    .productsTotal(p.getProductsTotal())
                    .deliveryTotal(p.getDeliveryTotal())
                    .totalPayment(p.getTotalPayment())
                    .state(p.getState())
                    .build();
        });

        PaymentDto dto = paymentService.payment(order(20.0));

        assertThat(dto.getState()).isEqualTo(PaymentState.PENDING);
        assertThat(dto.getProductsTotal()).isCloseTo(new BigDecimal("190.0"), within(new BigDecimal("0.001")));
        assertThat(dto.getDeliveryTotal()).isCloseTo(new BigDecimal("20.0"), within(new BigDecimal("0.001")));
        // 190 + 19 + 20 = 229
        assertThat(dto.getTotalPayment()).isCloseTo(new BigDecimal("229.0"), within(new BigDecimal("0.001")));
    }

    // ── success / failed callbacks ────────────────────────────────────────────
    @Test
    @DisplayName("paymentSuccess: sets SUCCESS and notifies order service")
    void paymentSuccess_updatesStateAndNotifiesOrder() {
        Payment payment = Payment.builder()
                .paymentId(PAYMENT_ID).orderId(ORDER_ID)
                .state(PaymentState.PENDING)
                .productsTotal(BigDecimal.valueOf(100.0))
                .deliveryTotal(BigDecimal.valueOf(50.0))
                .totalPayment(BigDecimal.valueOf(160.0))
                .build();
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenReturn(payment);

        paymentService.paymentSuccess(PAYMENT_ID);

        assertThat(payment.getState()).isEqualTo(PaymentState.SUCCESS);
        verify(orderClient).paymentSuccess(ORDER_ID);
    }

    @Test
    @DisplayName("paymentFailed: sets FAILED and notifies order service")
    void paymentFailed_updatesStateAndNotifiesOrder() {
        Payment payment = Payment.builder()
                .paymentId(PAYMENT_ID).orderId(ORDER_ID)
                .state(PaymentState.PENDING)
                .productsTotal(BigDecimal.valueOf(100.0))
                .deliveryTotal(BigDecimal.valueOf(50.0))
                .totalPayment(BigDecimal.valueOf(160.0))
                .build();
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenReturn(payment);

        paymentService.paymentFailed(PAYMENT_ID);

        assertThat(payment.getState()).isEqualTo(PaymentState.FAILED);
        verify(orderClient).paymentFailed(ORDER_ID);
    }
}
