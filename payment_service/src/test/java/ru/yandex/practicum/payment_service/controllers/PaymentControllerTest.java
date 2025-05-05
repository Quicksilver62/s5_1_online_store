package ru.yandex.practicum.payment_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.practicum.payment_service.services.controllers.PaymentController;
import ru.yandex.practicum.payment_service.services.services.PaymentService;

@WebFluxTest(PaymentController.class)
public class PaymentControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private PaymentService paymentService;
}
