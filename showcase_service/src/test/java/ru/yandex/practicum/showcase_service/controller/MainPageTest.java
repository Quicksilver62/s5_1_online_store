package ru.yandex.practicum.showcase_service.controller;


import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.showcase_service.config.TestSecurityConfig;
import ru.yandex.practicum.showcase_service.configuration.SecurityConfig;
import ru.yandex.practicum.showcase_service.controllers.MainController;
import ru.yandex.practicum.showcase_service.dto.ItemDto;
import ru.yandex.practicum.showcase_service.services.ItemService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@Import({SecurityConfig.class, TestSecurityConfig.class})
@WebFluxTest(MainController.class)
public class MainPageTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ItemService itemService;

    @Test
    @WithMockUser
    void mainPage_ShouldReturnMainView() {
        Pageable pageable = PageRequest.of(0, 10);
        Slice<ItemDto> mockSlice = new SliceImpl<>(List.of(
                new ItemDto(1, "Item 1", "Desc 1", "/img1.jpg", 100.0, 5),
                new ItemDto(2, "Item 2", "Desc 2", "/img2.jpg", 200.0, 3)
        ), pageable, false);

        when(itemService.getItems(ArgumentMatchers.any(Pageable.class)))
                .thenReturn(Mono.just(mockSlice));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/main/items")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class).consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("<b>Item 1</b>"));
                    assertTrue(body.contains("<b>Item 2</b>"));
                });
    }

    @Test
    void mainPage_ShouldReturnMainView_NotAuthenticated() {
        Pageable pageable = PageRequest.of(0, 10);
        Slice<ItemDto> mockSlice = new SliceImpl<>(List.of(
                new ItemDto(1, "Item 1", "Desc 1", "/img1.jpg", 100.0, 0),
                new ItemDto(2, "Item 2", "Desc 2", "/img2.jpg", 200.0, 0)
        ), pageable, false);

        when(itemService.getItems(ArgumentMatchers.any(Pageable.class)))
                .thenReturn(Mono.just(mockSlice));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/main/items")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class).consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("<b>Item 1</b>"));
                    assertTrue(body.contains("<b>Item 2</b>"));
                });
    }

    @Test
    @WithMockUser
    void handleItemAction_WithPlusAction_ShouldAddItem() {
        Pageable pageable = PageRequest.of(0, 10);
        Slice<ItemDto> mockSlice = new SliceImpl<>(List.of(
                new ItemDto(1, "Item 1", "Desc 1", "/img1.jpg", 100.0, 6) // count increased
        ), pageable, false);

        when(itemService.handleItemAction(eq("plus"), eq(1), ArgumentMatchers.any(Pageable.class)))
                .thenReturn(Mono.just(mockSlice));

        webTestClient.mutateWith(csrf())
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/main/items/1")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .queryParam("action", "plus")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class).consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("<b>Item 1</b>"));
                    assertTrue(body.contains("<span>6</span>"));
                });
    }

    @Test
    @WithMockUser
    void handleItemAction_WithInvalidAction_ShouldReturnMainView() {
        when(itemService.handleItemAction(eq("invalid"), eq(1), ArgumentMatchers.any(Pageable.class)))
                .thenThrow(IllegalArgumentException.class);

        webTestClient.mutateWith(csrf())
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/main/items/1")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .queryParam("action", "invalid")
                        .build())
                .exchange()
                .expectStatus().is4xxClientError()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class).consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("<title>Некорректный запрос - 400</title>"));
                });
    }

    @Test
    @WithMockUser
    void ordersPage_WhenServiceThrowsException_ShouldReturnError() {
        when(itemService.getItems(ArgumentMatchers.any(Pageable.class)))
                .thenThrow(new RuntimeException("Service error"));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/main/items")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .build())
                .exchange()
                .expectStatus().is5xxServerError()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class).consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("<title>Ошибка сервера - 500</title>"));
                });
    }
}
