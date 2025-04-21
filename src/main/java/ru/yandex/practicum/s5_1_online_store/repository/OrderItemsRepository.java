package ru.yandex.practicum.s5_1_online_store.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.s5_1_online_store.model.OrderItem;

public interface OrderItemsRepository extends ReactiveCrudRepository<OrderItem, Integer> {

    @Query("""
        SELECT order_items.*, items.* FROM store.order_items 
        JOIN store.items ON order_items.item_id = items.id 
        WHERE order_items.order_id = :orderId
        """)
    Flux<OrderItem> findByOrderIdWithItem(Integer orderId);
}
