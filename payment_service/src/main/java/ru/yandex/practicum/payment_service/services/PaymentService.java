package ru.yandex.practicum.payment_service.services;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Random;

@Service
public class PaymentService {

    private final Random random = new Random();

    public Mono<Double> getAmount() {
        return Mono.fromCallable(() -> {
            double amount = random.nextDouble() * 100_000;
            return Math.round(amount * 100) / 100.0;
        });
    }

    public Mono<Void> purchase(Double price) {
        if (price == null || price <= 0) {
            return Mono.error(new IllegalArgumentException("Price must be positive"));
        }
        return getAmount()
                .flatMap(amount -> {
                    if (amount < price) {
                        return Mono.error(new IllegalArgumentException("Not enough funds"));
                    }
                    return decreaseAmount(price);
                })
                .onErrorMap(e -> e);
    }

    private Mono<Void> decreaseAmount(Double price) {
        return getAmount()
                .map(amount -> amount - price)
                .then();
    }
}
