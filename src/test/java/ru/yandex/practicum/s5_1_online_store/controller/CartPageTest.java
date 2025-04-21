package ru.yandex.practicum.s5_1_online_store.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.s5_1_online_store.controllers.CartController;
import ru.yandex.practicum.s5_1_online_store.dto.ItemDto;
import ru.yandex.practicum.s5_1_online_store.services.CartService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest(CartController.class)
public class CartPageTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CartService cartService;

    @Test
    void cartPage_ShouldReturnCartViewWithItems() {
        Flux<ItemDto> mockItems = Flux.just(
                new ItemDto(1, "Item 1", "Desc 1", "/img1.jpg", 100.0, 2),
                new ItemDto(2, "Item 2", "Desc 2", "/img2.jpg", 200.0, 1)
        );

        when(cartService.getCartItems(any(ServerHttpRequest.class)))
                .thenReturn(mockItems);

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class).consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("<b>Item 1</b>"));
                    assertTrue(body.contains("<b>Item 2</b>"));
                    assertTrue(body.contains("Итого: 300.0 руб."));
                });
    }

    @Test
    void handleItemAction_WithPlusAction_ShouldUpdateCart() {
        Flux<ItemDto> updatedItems = Flux.just(
                new ItemDto(1, "Item 1", "Desc 1", "/img1.jpg", 100.0, 3)
        );

        when(cartService.handleItemAction(anyString(), anyInt(), any(ServerHttpRequest.class)))
                .thenReturn(Mono.empty());
        when(cartService.getCartItems(any(ServerHttpRequest.class)))
                .thenReturn(updatedItems);

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/cart/items/1")
                        .queryParam("action", "plus")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class).consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("<b>Item 1</b>"));
                });
    }

    @Test
    void buy_ShouldClearCart() {
        when(cartService.buy(any(ServerHttpRequest.class)))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/cart/items/buy")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class).consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("Итого: 0.0 руб."));
                });
    }
}
