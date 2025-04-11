package ru.yandex.practicum.s5_1_online_store.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Integer id;
    private String title;
    private String description;
    private String imgPath;
    private Double price;
    private Integer count;
}
