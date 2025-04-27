package ru.yandex.practicum.s5_1_online_store.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderItemWithItem {
    private Integer orderId;
    private Integer itemId;
    private Integer count;
    private String itemTitle;
    private String itemDescription;
    private String itemImgPath;
    private Double itemPrice;
}
