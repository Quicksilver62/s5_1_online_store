package ru.yandex.practicum.showcase_service.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.showcase_service.controllers.OrderController;
import ru.yandex.practicum.showcase_service.dto.ItemDto;
import ru.yandex.practicum.showcase_service.dto.OrderDto;
import ru.yandex.practicum.showcase_service.services.OrderService;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(OrderController.class)
public class OrderPageTest {

    @Autowired
    private WebTestClient webTestClient;

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
    void ordersPage_ShouldReturnOrdersViewWithItems() {
        OrderDto order1 = createTestOrder(1, 2);
        OrderDto order2 = createTestOrder(2, 1);
        Flux<OrderDto> mockOrders = Flux.just(order1, order2);

        when(orderService.getOrders(any(ServerHttpRequest.class))).thenReturn(mockOrders);

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class).consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("<td>Item 1 (1 шт.) 100.0 руб.</td>"));
                    assertTrue(body.contains("<td>Item 2 (1 шт.) 200.0 руб.</td>"));
                    assertTrue(body.contains("<b>Сумма: 300.0 руб.</b>"));
                });
    }

    @Test
    void orderPage_ShouldReturnOrderWithItems() {
        OrderDto mockOrder = createTestOrder(1, 3);
        when(orderService.getOrder(eq(1), any(ServerHttpRequest.class))).thenReturn(Mono.just(mockOrder));

        webTestClient.get()
                .uri("/orders/1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class).consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("<b>Item 1</b>"));
                    assertTrue(body.contains("<b>Item 2</b>"));
                    assertTrue(body.contains("<b>Item 3</b>"));
                    assertTrue(body.contains("<h3>Сумма: 600.0 руб.</h3>"));
                });
    }

    @Test
    void orderPage_WhenEmptyOrder_ShouldReturnView() {
        OrderDto emptyOrder = new OrderDto(1, new HashSet<>(), 0.0);
        when(orderService.getOrder(eq(1), any(ServerHttpRequest.class))).thenReturn(Mono.just(emptyOrder));

        webTestClient.get()
                .uri("/orders/1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class).consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("<h3>Сумма: 0.0 руб.</h3>"));
                });
    }

    @Test
    void orderPage_WhenOrderNotFound_ShouldReturnNotFound() throws Exception {
        when(orderService.getOrder(eq(999), any(ServerHttpRequest.class)))
                .thenThrow(new NoSuchElementException("Order not found"));

        webTestClient.get()
                .uri("/orders/999")
                .exchange()
                .expectStatus().is4xxClientError()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class).consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("<title>Страница не найдена - 404</title>"));
                });
    }
}
