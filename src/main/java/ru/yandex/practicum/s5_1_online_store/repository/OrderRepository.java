package ru.yandex.practicum.s5_1_online_store.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.s5_1_online_store.model.Order;

import java.util.UUID;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Integer> {

    Flux<Order> findAllByUserId(UUID userId);

    Mono<Order> findByIdAndUserId(Integer id, UUID userId);
}
