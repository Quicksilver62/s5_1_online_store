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
//import ru.yandex.practicum.showcase_service.controllers.CartController;
//import ru.yandex.practicum.showcase_service.dto.ItemDto;
//import ru.yandex.practicum.showcase_service.facades.CartFacade;
//import ru.yandex.practicum.showcase_service.model.CartModel;
//import ru.yandex.practicum.showcase_service.services.CartService;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.when;
//
//@WebFluxTest(CartController.class)
//public class CartPageTest {
//
//    @Autowired
//    private WebTestClient webTestClient;
//
//    @MockitoBean
//    private CartService cartService;
//
//    @MockitoBean
//    private CartFacade cartFacade;
//
//    @Test
//    void cartPage_ShouldReturnCartViewWithItems() {
//        List<ItemDto> mockItems = List.of(
//                new ItemDto(1, "Item 1", "Desc 1", "/img1.jpg", 100.0, 2),
//                new ItemDto(2, "Item 2", "Desc 2", "/img2.jpg", 200.0, 1)
//        );
//        Mono<CartModel> mockModel = Mono.just(new CartModel(mockItems, 300.0, true));
//
//                when(cartFacade.getCartModel(any(ServerHttpRequest.class)))
//                .thenReturn(mockModel);
//
//        webTestClient.get()
//                .uri("/cart/items")
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.TEXT_HTML)
//                .expectBody(String.class).consumeWith(response -> {
//                    String body = response.getResponseBody();
//                    assertNotNull(body);
//                    assertTrue(body.contains("<b>Item 1</b>"));
//                    assertTrue(body.contains("<b>Item 2</b>"));
//                    assertTrue(body.contains("Итого: 300.0 руб."));
//                });
//    }
//
//    @Test
//    void handleItemAction_WithPlusAction_ShouldUpdateCart() {
//        List<ItemDto> updatedItems = List.of(
//                new ItemDto(1, "Item 1", "Desc 1", "/img1.jpg", 100.0, 3)
//        );
//        Mono<CartModel> mockModel = Mono.just(new CartModel(updatedItems, 100.0, true));
//
//        when(cartFacade.handleItemActionAndGetModel(anyString(), anyInt(), any(ServerHttpRequest.class)))
//                .thenReturn(mockModel);
//
//        webTestClient.post()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/cart/items/1")
//                        .queryParam("action", "plus")
//                        .build())
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.TEXT_HTML)
//                .expectBody(String.class).consumeWith(response -> {
//                    String body = response.getResponseBody();
//                    assertNotNull(body);
//                    assertTrue(body.contains("<b>Item 1</b>"));
//                });
//    }
//
//    @Test
//    void buy_ShouldClearCart() {
//        when(cartService.buy(any(ServerHttpRequest.class)))
//                .thenReturn(Mono.empty());
//
//        webTestClient.post()
//                .uri("/cart/items/buy")
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.TEXT_HTML)
//                .expectBody(String.class).consumeWith(response -> {
//                    String body = response.getResponseBody();
//                    assertNotNull(body);
//                    assertTrue(body.contains("Итого: 0.0 руб."));
//                });
//    }
//}
