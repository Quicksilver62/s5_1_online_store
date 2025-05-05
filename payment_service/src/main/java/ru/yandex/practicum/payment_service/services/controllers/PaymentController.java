package ru.yandex.practicum.payment_service.services.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment_service.services.services.PaymentService;
import ru.yandex.practicum.server.api.DefaultApi;
import ru.yandex.practicum.server.domain.PurchaseRequest;

@RestController
@RequiredArgsConstructor
public class PaymentController implements DefaultApi {

    private final PaymentService paymentService;

    @Override
    public Mono<ResponseEntity<Object>> purchase(@Valid @RequestBody Mono<PurchaseRequest> purchaseRequest,
                                                 ServerWebExchange exchange) {
        return purchaseRequest
                .flatMap(request -> paymentService.purchase(request.getPrice()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).build())
                .onErrorResume(e -> {
                    HttpStatus status = e instanceof IllegalArgumentException
                            ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR;
                    return Mono.just(ResponseEntity.status(status).build());
                });
    }
}
