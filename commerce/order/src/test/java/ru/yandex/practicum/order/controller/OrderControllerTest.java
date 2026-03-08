package ru.yandex.practicum.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.commerce.interaction.api.dto.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.api.enums.OrderState;
import ru.yandex.practicum.order.service.OrderService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@WebMvcTest(controllers = OrderController.class)
@DisplayName("OrderController — MVC slice tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID CART_ID  = UUID.randomUUID();
    private static final UUID PROD_ID  = UUID.randomUUID();

    @Test
    @DisplayName("POST /api/v1/order → 200 with OrderDto (state=NEW)")
    void createNewOrder_returns200WithOrderDto() throws Exception {
        CreateNewOrderRequest request = new CreateNewOrderRequest();
        request.setShoppingCartId(CART_ID);
        request.setUsername("testUser");
        request.setProducts(Map.of(PROD_ID, 1));

        OrderDto response = new OrderDto();
        response.setOrderId(ORDER_ID);
        response.setUsername("testUser");
        response.setState(OrderState.NEW);
        when(orderService.createNewOrder(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(ORDER_ID.toString()))
                .andExpect(jsonPath("$.state").value("NEW"));
    }

    @Test
    @DisplayName("GET /api/v1/order?username=user → 200 with list of orders")
    void getClientOrders_returns200WithList() throws Exception {
        OrderDto order1 = new OrderDto();
        order1.setOrderId(ORDER_ID);
        order1.setUsername("user");
        order1.setState(OrderState.PAID);
        List<OrderDto> orders = List.of(order1);
        when(orderService.getClientOrders("user")).thenReturn(orders);

        mockMvc.perform(get("/api/v1/order").param("username", "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].state").value("PAID"));
    }

    @Test
    @DisplayName("PUT /api/v1/order/{id}/payment/success → 200")
    void paymentSuccess_returns200() throws Exception {
        OrderDto updated = new OrderDto();
        updated.setOrderId(ORDER_ID);
        updated.setState(OrderState.PAID);
        when(orderService.paymentSuccess(ORDER_ID)).thenReturn(updated);

        mockMvc.perform(put("/api/v1/order/{id}/payment/success", ORDER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("PAID"));
    }

    @Test
    @DisplayName("PUT /api/v1/order/{id}/delivery/failed → 200 with DELIVERY_FAILED state")
    void deliveryFailed_returns200() throws Exception {
        OrderDto updated = new OrderDto();
        updated.setOrderId(ORDER_ID);
        updated.setState(OrderState.DELIVERY_FAILED);
        when(orderService.deliveryFailed(ORDER_ID)).thenReturn(updated);

        mockMvc.perform(put("/api/v1/order/{id}/delivery/failed", ORDER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("DELIVERY_FAILED"));
    }
}
