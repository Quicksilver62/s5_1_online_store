package ru.yandex.practicum.s5_1_online_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.s5_1_online_store.model.CartItem;

@Repository
public interface CartItemsRepository extends JpaRepository<CartItem, Integer> {
}
