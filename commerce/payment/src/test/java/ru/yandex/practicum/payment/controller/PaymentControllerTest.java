package ru.yandex.practicum.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.api.dto.PaymentDto;
import ru.yandex.practicum.commerce.interaction.api.enums.PaymentState;
import ru.yandex.practicum.payment.service.PaymentService;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@WebMvcTest(controllers = PaymentController.class)
@DisplayName("PaymentController — MVC slice tests")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID PAYMENT_ID = UUID.randomUUID();
    private static final UUID PROD_ID = UUID.randomUUID();

    private OrderDto sampleOrder() {
        OrderDto dto = new OrderDto();
        dto.setOrderId(ORDER_ID);
        dto.setProducts(Map.of(PROD_ID, 2));
        dto.setDeliveryPrice(BigDecimal.valueOf(50.0));
        return dto;
    }

    @Test
    @DisplayName("POST /api/v1/payment/productCost → 200 with BigDecimal")
    void productCost_returns200WithBigDecimal() throws Exception {
        when(paymentService.productCost(any())).thenReturn(new BigDecimal("190.00"));

        mockMvc.perform(post("/api/v1/payment/productCost")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleOrder())))
                .andExpect(status().isOk())
                .andExpect(content().string("190.00"));
    }

    @Test
    @DisplayName("POST /api/v1/payment/totalCost → 200 with total BigDecimal")
    void totalCost_returns200WithBigDecimal() throws Exception {
        when(paymentService.getTotalCost(any())).thenReturn(new BigDecimal("260.00"));

        mockMvc.perform(post("/api/v1/payment/totalCost")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleOrder())))
                .andExpect(status().isOk())
                .andExpect(content().string("260.00"));
    }

    @Test
    @DisplayName("POST /api/v1/payment → 200 with PaymentDto (state=PENDING)")
    void payment_returns200WithPaymentDto() throws Exception {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setPaymentId(PAYMENT_ID);
        paymentDto.setOrderId(ORDER_ID);
        paymentDto.setTotalPayment(new BigDecimal("260.00"));
        paymentDto.setState(PaymentState.PENDING);
        when(paymentService.payment(any())).thenReturn(paymentDto);

        mockMvc.perform(post("/api/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleOrder())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("PENDING"))
                .andExpect(jsonPath("$.totalPayment").value(260.00));
    }
}
