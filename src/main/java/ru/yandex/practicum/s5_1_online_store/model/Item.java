package ru.yandex.practicum.s5_1_online_store.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
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
