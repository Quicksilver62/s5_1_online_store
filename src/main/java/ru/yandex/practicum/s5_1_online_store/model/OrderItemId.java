package ru.yandex.practicum.s5_1_online_store.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class OrderItemId implements Serializable {
    @Column("item_id")
    private Integer itemId;
    @Column("order_id")
    private Integer orderId;
}
