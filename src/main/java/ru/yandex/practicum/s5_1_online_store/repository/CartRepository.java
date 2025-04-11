package ru.yandex.practicum.s5_1_online_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.s5_1_online_store.model.Cart;
import ru.yandex.practicum.s5_1_online_store.model.User;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {

    Optional<Cart> findByUser(User user);
}
