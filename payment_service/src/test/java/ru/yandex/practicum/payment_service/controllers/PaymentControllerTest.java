package ru.yandex.practicum.payment_service.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment_service.services.PaymentService;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@WebFluxTest(PaymentController.class)
public class PaymentControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private PaymentService paymentService;

    private final String AMOUNT_ENDPOINT = "/api/amount";
    private final String PURCHASE_ENDPOINT = "/api/purchase";

    @Test
    void getAmount_ShouldReturnAmount_WhenServiceReturnsValue() {
        double expectedAmount = 1234.56;
        when(paymentService.getAmount())
                .thenReturn(Mono.just(expectedAmount));

        webTestClient.get()
                .uri(AMOUNT_ENDPOINT)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Double.class)
                .isEqualTo(expectedAmount);

        verify(paymentService, times(1)).getAmount();
    }

    @Test
    void getAmount_ShouldReturn500_WhenServiceFails() {
        String errorMessage = "Connection failed";
        when(paymentService.getAmount())
                .thenReturn(Mono.error(new RuntimeException(errorMessage)));

        webTestClient.get()
                .uri(AMOUNT_ENDPOINT)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.status").isEqualTo(500);

        verify(paymentService, times(1)).getAmount();
    }

    @Test
    void purchase_ShouldReturn201_WhenSuccessful() {
        when(paymentService.purchase(anyDouble()))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri(PURCHASE_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"price\": 100.0}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody().isEmpty();

        verify(paymentService, times(1)).purchase(100.0);
    }

    @Test
    void purchase_ShouldReturn400_WhenServiceThrowsIllegalArgument() {
        when(paymentService.purchase(anyDouble()))
                .thenReturn(Mono.error(new IllegalArgumentException("Invalid amount")));

        webTestClient.post()
                .uri(PURCHASE_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"price\": 100.0}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void purchase_ShouldReturn500_WhenUnexpectedError() {
        when(paymentService.purchase(anyDouble()))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        webTestClient.post()
                .uri(PURCHASE_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"price\": 100.0}")
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
