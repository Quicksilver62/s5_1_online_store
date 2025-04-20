package ru.yandex.practicum.s5_1_online_store.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.yandex.practicum.s5_1_online_store.model.OrderItem;

public interface OrderItemsRepository extends ReactiveCrudRepository<OrderItem, Integer> {
}
