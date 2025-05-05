package ru.yandex.practicum.payment_service.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment_service.services.controllers.AmountController;
import ru.yandex.practicum.payment_service.services.services.AmountService;

import static org.mockito.Mockito.*;

@WebFluxTest(AmountController.class)
public class AmountControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AmountService amountService;

    private final String AMOUNT_ENDPOINT = "/api/amount";

    @Test
    void getAmount_ShouldReturnAmount_WhenServiceReturnsValue() {
        double expectedAmount = 1234.56;
        when(amountService.getAmount())
                .thenReturn(Mono.just(expectedAmount));

        webTestClient.get()
                .uri(AMOUNT_ENDPOINT)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Double.class)
                .isEqualTo(expectedAmount);

        verify(amountService, times(1)).getAmount();
    }

    @Test
    void getAmount_ShouldReturn500_WhenServiceFails() {
        String errorMessage = "Connection failed";
        when(amountService.getAmount())
                .thenReturn(Mono.error(new RuntimeException(errorMessage)));

        webTestClient.get()
                .uri(AMOUNT_ENDPOINT)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.status").isEqualTo(500);

        verify(amountService, times(1)).getAmount();
    }
}
