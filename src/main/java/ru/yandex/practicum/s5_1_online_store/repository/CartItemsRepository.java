package ru.yandex.practicum.s5_1_online_store.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.s5_1_online_store.model.CartItem;
import ru.yandex.practicum.s5_1_online_store.model.CartItemId;

@Repository
public interface CartItemsRepository extends ReactiveCrudRepository<CartItem, CartItemId> {

    Mono<CartItem> findById_ItemIdAndId_CartId(Integer itemId, Integer cartId);

    @Query("""
        SELECT cart_items.*, items.* FROM store.cart_items 
        JOIN store.items ON cart_items.item_id = items.id 
        WHERE cart_items.cart_id = :cartId
        """)
    Flux<CartItem> findByCartIdWithItem(Integer cartId);

    Mono<Void> deleteAllById_CartId(Integer cartId);
}
