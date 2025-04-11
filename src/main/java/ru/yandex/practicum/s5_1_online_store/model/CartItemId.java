package ru.yandex.practicum.s5_1_online_store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class CartItemId implements Serializable {
    @Column(name = "item_id")
    private Integer itemId;
    @Column(name = "cart_id")
    private Integer cartId;
}
