package ru.yandex.practicum.showcase_service.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.showcase_service.model.Cart;

import java.util.UUID;

@Repository
public interface CartRepository extends ReactiveCrudRepository<Cart, Integer> {

    Mono<Cart> findByUserId(UUID userId);
}
