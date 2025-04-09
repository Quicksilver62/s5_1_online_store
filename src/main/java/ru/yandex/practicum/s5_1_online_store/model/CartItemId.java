package ru.yandex.practicum.s5_1_online_store.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class CartItemId {
    private Integer itemId;
    private Integer cartId;
}
