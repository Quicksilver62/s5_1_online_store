package ru.yandex.practicum.s5_1_online_store.model;

import org.springframework.data.relational.core.mapping.Column;

public interface OrderItemWithItem {
    @Column("order_id")
    Integer getOrderId();
    @Column("item_id")
    Integer getItemId();
    @Column("count")
    Integer getCount();
    @Column("title")
    String getItemTitle();
    @Column("description")
    String getItemDescription();
    @Column("img_path")
    String getItemImgPath();
    @Column("price")
    Double getItemPrice();
}
