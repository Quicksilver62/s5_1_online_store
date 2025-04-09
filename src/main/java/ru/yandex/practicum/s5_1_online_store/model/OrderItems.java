package ru.yandex.practicum.s5_1_online_store.model;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(schema = "store", name = "order_items")
public class OrderItems {

    @EmbeddedId
    private OrderItemId id;

    @ManyToOne
    @MapsId("orderId")
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @MapsId("itemId")
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(name = "count")
    private Integer count;
}
