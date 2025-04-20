package ru.yandex.practicum.s5_1_online_store.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.s5_1_online_store.model.Order;
import ru.yandex.practicum.s5_1_online_store.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Integer> {

    List<Order> findAllByUser(User user);

    Optional<Order> findByUser(User user);
}
