package ru.yandex.practicum.s5_1_online_store.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("store.order_items")
public class OrderItem {

    @Id
    private OrderItemId id;

    private Integer count;

    @Transient
    private Item item;
}
