package ru.yandex.practicum.s5_1_online_store.model;

import lombok.*;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("store.cart_items")
public class CartItem {

    @Column("item_id")
    private Integer itemId;

    @Column("cart_id")
    private Integer cartId;

    private Integer count;

    @Transient
    private Item item;
}
