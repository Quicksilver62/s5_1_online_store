package ru.yandex.practicum.payment_service.services.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class AmountService {

    private final Random random = new Random();

    public Mono<Double> getAmount() {
        return Mono.fromCallable(() -> {
            double amount = random.nextDouble() * 100_000;
            return Math.round(amount * 100) / 100.0;
        });
    }

    public Mono<Void> decreaseAmount(Double price) {
        return getAmount()
                .map(amount -> amount - price)
                .then();
    }
}
