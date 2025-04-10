package ru.yandex.practicum.s5_1_online_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.s5_1_online_store.model.CartItem;

import java.util.Optional;

@Repository
public interface CartItemsRepository extends JpaRepository<CartItem, Integer> {

    Optional<CartItem> findCartItemByItemIdAndCartId(Integer itemId, Integer cartId);
}
