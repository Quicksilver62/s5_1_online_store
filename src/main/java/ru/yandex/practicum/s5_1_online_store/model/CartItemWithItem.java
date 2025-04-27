package ru.yandex.practicum.s5_1_online_store.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CartItemWithItem {
    private Integer cartId;
    private Integer itemId;
    private Integer count;
    private String itemTitle;
    private String itemDescription;
    private String itemImgPath;
    private Double itemPrice;
}
