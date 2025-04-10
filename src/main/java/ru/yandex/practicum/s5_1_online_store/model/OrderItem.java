package ru.yandex.practicum.s5_1_online_store.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "store", name = "order_items")
public class OrderItem {

    @EmbeddedId
    private OrderItemId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("itemId")
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(name = "count")
    private Integer count;
}
