package ru.yandex.practicum.s5_1_online_store.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.s5_1_online_store.model.Item;

@Repository
public interface ItemRepository extends ReactiveCrudRepository<Item, Integer> {

    Flux<Item> findAllBy(Pageable pageable);
}
