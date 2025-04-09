package ru.yandex.practicum.s5_1_online_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.s5_1_online_store.model.Item;

public interface ItemRepository extends JpaRepository<Item, Integer> {
}
