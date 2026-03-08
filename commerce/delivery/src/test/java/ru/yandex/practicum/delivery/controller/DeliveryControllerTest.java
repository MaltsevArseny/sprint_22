package ru.yandex.practicum.delivery.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.commerce.interaction.api.dto.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.api.enums.DeliveryState;
import ru.yandex.practicum.delivery.service.DeliveryService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@WebMvcTest(controllers = DeliveryController.class)
@DisplayName("DeliveryController — MVC slice tests")
class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeliveryService deliveryService;

    private static final UUID ORDER_ID    = UUID.randomUUID();
    private static final UUID DELIVERY_ID = UUID.randomUUID();

    private OrderDto sampleOrder() {
        return OrderDto.builder()
                .orderId(ORDER_ID)
                .deliveryWeight(10.0)
                .deliveryVolume(10.0)
                .fragile(true)
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/delivery → 200 with DeliveryDto (state=CREATED)")
    void planDelivery_returns200WithDeliveryDto() throws Exception {
        DeliveryDto response = DeliveryDto.builder()
                .deliveryId(DELIVERY_ID)
                .orderId(ORDER_ID)
                .state(DeliveryState.CREATED)
                .build();
        when(deliveryService.planDelivery(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/delivery")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleOrder())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryId").value(DELIVERY_ID.toString()))
                .andExpect(jsonPath("$.state").value("CREATED"));
    }

    @Test
    @DisplayName("POST /api/v1/delivery/cost → 200 with computed cost Double")
    void deliveryCost_returns200WithDouble() throws Exception {
        when(deliveryService.deliveryCost(any())).thenReturn(27.6);

        mockMvc.perform(post("/api/v1/delivery/cost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleOrder())))
                .andExpect(status().isOk())
                .andExpect(content().string("27.6"));
    }
}
