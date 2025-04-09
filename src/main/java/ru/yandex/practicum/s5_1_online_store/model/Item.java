package ru.yandex.practicum.s5_1_online_store.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(schema = "store", name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String title;

    @Column
    private String description;

    @Column(name = "img_path")
    private String imgPath;

    @Column
    private Integer price;

    @OneToMany(mappedBy = "item")
    private Set<Cart> carts = new HashSet<>();

    @OneToMany(mappedBy = "item")
    private Set<Order> orders = new HashSet<>();
}
