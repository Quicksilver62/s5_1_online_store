package ru.yandex.practicum.showcase_service.dto;

import lombok.*;

@Getter
@Setter
@Builder
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
