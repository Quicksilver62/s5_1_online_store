package ru.yandex.practicum.s5_1_online_store.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.s5_1_online_store.controllers.OrderController;
import ru.yandex.practicum.s5_1_online_store.dto.ItemDto;
import ru.yandex.practicum.s5_1_online_store.dto.OrderDto;
import ru.yandex.practicum.s5_1_online_store.services.OrderService;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
public class OrderPageTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    private ItemDto createTestItem(Integer id) {
        return new ItemDto(id, "Item " + id, "Description " + id,
                "/img" + id + ".jpg", 100.0 * id, 1);
    }

    private OrderDto createTestOrder(Integer id, int itemCount) {
        Set<ItemDto> items = IntStream.range(1, itemCount + 1)
                .mapToObj(this::createTestItem)
                .collect(Collectors.toSet());

        double totalSum = items.stream()
                .mapToDouble(item -> item.getPrice() * item.getCount())
                .sum();

        return new OrderDto(id, items, totalSum);
    }

    @Test
    void ordersPage_ShouldReturnOrdersViewWithItems() throws Exception {
        OrderDto order1 = createTestOrder(1, 2);
        OrderDto order2 = createTestOrder(2, 1);
        List<OrderDto> mockOrders = List.of(order1, order2);

        when(orderService.getOrders(any(HttpServletRequest.class))).thenReturn(mockOrders);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().attribute("orders", hasSize(2)))
                .andExpect(model().attribute("orders", hasItem(
                        allOf(
                                hasProperty("id", is(1)),
                                hasProperty("totalSum", is(300.0)),
                                hasProperty("items", hasSize(2))
                        ))));
    }

    @Test
    void orderPage_ShouldReturnOrderWithItems() throws Exception {
        OrderDto mockOrder = createTestOrder(1, 3);
        when(orderService.getOrder(eq(1), any(HttpServletRequest.class))).thenReturn(mockOrder);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attribute("order",
                        allOf(
                                hasProperty("id", is(1)),
                                hasProperty("items", hasSize(3)),
                                hasProperty("totalSum", is(600.0))
                        )
                ));
    }

    @Test
    void orderPage_WhenEmptyOrder_ShouldReturnView() throws Exception {
        OrderDto emptyOrder = new OrderDto(1, new HashSet<>(), 0.0);
        when(orderService.getOrder(eq(1), any(HttpServletRequest.class))).thenReturn(emptyOrder);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attribute("order",
                        allOf(
                                hasProperty("id", is(1)),
                                hasProperty("items", empty()),
                                hasProperty("totalSum", is(0.0))
                        )
                ));
    }

    @Test
    void orderPage_WhenOrderNotFound_ShouldReturnNotFound() throws Exception {
        when(orderService.getOrder(eq(999), any(HttpServletRequest.class)))
                .thenThrow(new NoSuchElementException("Order not found"));

        mockMvc.perform(get("/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("not-found.html"));
    }
}
