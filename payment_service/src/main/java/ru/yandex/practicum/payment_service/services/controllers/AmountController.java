package ru.yandex.practicum.payment_service.services.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment_service.services.services.AmountService;
import ru.yandex.practicum.server.api.DefaultApi;

@RestController
@RequiredArgsConstructor
public class AmountController implements DefaultApi {

    private final AmountService amountService;

    @Override
//    @GetMapping("/api/amount")
    public Mono<ResponseEntity<Double>> getAmount(ServerWebExchange exchange) {
        return amountService.getAmount()
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.error(new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to get amount",
                        e
                )));
    }
}
