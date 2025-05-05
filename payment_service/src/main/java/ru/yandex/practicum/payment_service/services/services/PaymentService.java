package ru.yandex.practicum.payment_service.services.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final AmountService amountService;

    public Mono<Void> purchase(Double price) {
        if (price == null || price <= 0) {
            return Mono.error(new IllegalArgumentException("Price must be positive"));
        }
        return amountService.getAmount()
                .flatMap(amount -> {
                    if (amount < price) {
                        return Mono.error(new IllegalArgumentException("Not enough funds"));
                    }
                    return amountService.decreaseAmount(price);
                })
                .onErrorMap(e -> e);
    }}
