package ru.yandex.practicum.s5_1_online_store.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CartItemId implements Serializable {
    @Column("item_id")
    private Integer itemId;
    @Column("cart_id")
    private Integer cartId;
}
