//package ru.yandex.practicum.showcase_service.controller;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
//import org.springframework.http.MediaType;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.reactive.server.WebTestClient;
//import reactor.core.publisher.Mono;
//import ru.yandex.practicum.showcase_service.controllers.ItemController;
//import ru.yandex.practicum.showcase_service.dto.ItemDto;
//import ru.yandex.practicum.showcase_service.services.CartService;
//import ru.yandex.practicum.showcase_service.services.ItemService;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@WebFluxTest(ItemController.class)
//public class ItemPageTest {
//
//    @Autowired
//    private WebTestClient webTestClient;
//
//    @MockitoBean
//    private ItemService itemService;
//
//    @MockitoBean
//    private CartService cartService;
//
//    @Test
//    void itemPage_ShouldReturnItemView() {
//        ItemDto mockItem = new ItemDto(1, "Test Item", "Description", "/img.jpg", 100.0, 2);
//        when(itemService.getItem(eq(1), any(ServerHttpRequest.class))).thenReturn(Mono.just(mockItem));
//
//        webTestClient.get()
//                .uri("/items/1")
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.TEXT_HTML)
//                .expectBody(String.class).consumeWith(response -> {
//                    String body = response.getResponseBody();
//                    assertNotNull(body);
//                    assertTrue(body.contains("<b>Test Item</b>"));
//                });
//    }
//
//    @Test
//    void handleItemAction_WithPlusAction_ShouldUpdateItem() {
//        ItemDto updatedItem = new ItemDto(1, "Test Item", "Description", "/img.jpg", 100.0, 3);
//        when(itemService.getItem(eq(1), any(ServerHttpRequest.class))).thenReturn(Mono.just(updatedItem));
//        when(cartService.handleItemAction(eq("plus"), eq(1), any(ServerHttpRequest.class))).thenReturn(Mono.empty());
//
//        webTestClient.post()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/items/1")
//                        .queryParam("action", "plus")
//                        .build())
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.TEXT_HTML)
//                .expectBody(String.class).consumeWith(response -> {
//                    String body = response.getResponseBody();
//                    assertNotNull(body);
//                    assertTrue(body.contains("<b>Test Item</b>"));
//                });
//    }
//
//    @Test
//    void handleItemAction_WithInvalidAction_ShouldStillReturnItem() {
//        ItemDto updatedItem = new ItemDto(1, "Test Item", "Description", "/img.jpg", 100.0, 3);
//        when(itemService.getItem(eq(1), any(ServerHttpRequest.class))).thenReturn(Mono.just(updatedItem));
//        when(cartService.handleItemAction(eq("plus"), eq(1), any(ServerHttpRequest.class))).thenReturn(Mono.empty());
//
//        webTestClient.post()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/items/1")
//                        .queryParam("action", "invalid")
//                        .build())
//                .exchange()
//                .expectStatus().is5xxServerError()
//                .expectHeader().contentType(MediaType.TEXT_HTML)
//                .expectBody(String.class).consumeWith(response -> {
//                    String body = response.getResponseBody();
//                    assertNotNull(body);
//                    assertTrue(body.contains("<title>Ошибка сервера - 500</title>"));
//                });
//    }
//}
