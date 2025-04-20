package ru.yandex.practicum.s5_1_online_store.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.s5_1_online_store.model.Cart;

import java.util.UUID;

@Repository
public interface CartRepository extends ReactiveCrudRepository<Cart, Integer> {

    Mono<Cart> findByUserId(UUID userId);
}
