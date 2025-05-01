package ru.yandex.practicum.showcase_service.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.showcase_service.model.OrderItem;
import ru.yandex.practicum.showcase_service.model.OrderItemWithItem;

public interface OrderItemsRepository extends ReactiveCrudRepository<OrderItem, Integer> {

    @Query("""
        SELECT
            order_items.order_id,
            order_items.item_id,
            order_items.count,
            items.title as item_title,
            items.description as item_description,
            items.img_path as item_img_path,
            items.price as item_price,
        FROM store.order_items
        JOIN store.items ON order_items.item_id = items.id 
        WHERE order_items.order_id = :orderId
        """)
    Flux<OrderItemWithItem> findByOrderIdWithItem(Integer orderId);
}
