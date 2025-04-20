package ru.yandex.practicum.s5_1_online_store.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("store.cart_items")
public class CartItem {

    @Id
    private CartItemId id;

    private Integer count;

    @Transient
    private Item item;
}
