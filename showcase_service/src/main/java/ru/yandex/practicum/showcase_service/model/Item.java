package ru.yandex.practicum.showcase_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("store.items")
public class Item {

    @Id
    private Integer id;

    @Column
    private String title;

    @Column
    private String description;

    @Column("img_path")
    private String imgPath;

    @Column
    private Double price;
}
