package ru.yandex.practicum.showcase_service.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.client.api.DefaultApi;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final DefaultApi paymentApiClient;

    public Mono<Boolean> isPurchaseAvailable(Double totalAmount) {
        return paymentApiClient.apiAmountGet()
                .map(balance -> balance >= totalAmount)
                .onErrorReturn(false);
    }
}
