package ru.yandex.practicum.s5_1_online_store.model;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class OrderItemId implements Serializable {
    private Integer itemId;
    private Integer orderId;
}
