package ru.yandex.practicum.s5_1_online_store.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.s5_1_online_store.model.CartItem;
import ru.yandex.practicum.s5_1_online_store.model.CartItemWithItem;

@Repository
public interface CartItemsRepository extends ReactiveCrudRepository<CartItem, Integer> {

    Mono<CartItem> findByItemIdAndCartId(Integer itemId, Integer cartId);

    @Query("""
        SELECT
            cart_items.cart_id,
            cart_items.item_id,
            cart_items.count,
            items.title as item_title,
            items.description as item_description,
            items.img_path as item_img_path,
            items.price as item_price
        FROM store.cart_items
        JOIN store.items ON cart_items.item_id = items.id 
        WHERE cart_items.cart_id = :cartId
        """)
    Flux<CartItemWithItem> findByCartIdWithItem(Integer cartId);

    Mono<Void> deleteAllByCartId(Integer cartId);
}
