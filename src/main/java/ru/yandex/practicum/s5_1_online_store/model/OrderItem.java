package ru.yandex.practicum.s5_1_online_store.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("store.order_items")
public class OrderItem {

    @Column("item_id")
    private Integer itemId;

    @Column("order_id")
    private Integer orderId;

    private Integer count;

    @Transient
    private Item item;
}
