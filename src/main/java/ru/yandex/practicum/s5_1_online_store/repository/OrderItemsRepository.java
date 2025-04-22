package ru.yandex.practicum.s5_1_online_store.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.s5_1_online_store.model.OrderItem;
import ru.yandex.practicum.s5_1_online_store.model.OrderItemWithItem;

public interface OrderItemsRepository extends ReactiveCrudRepository<OrderItem, Integer> {

    @Query("""
        SELECT
            order_items.order_id as order_id,
            order_items.item_id as item_id,
            order_items.count,
            items.title,
            items.description,
            items.img_path,
            items.price
        FROM store.order_items
        JOIN store.items ON order_items.item_id = items.id 
        WHERE order_items.order_id = :orderId
        """)
    Flux<OrderItemWithItem> findByOrderIdWithItem(Integer orderId);
}
