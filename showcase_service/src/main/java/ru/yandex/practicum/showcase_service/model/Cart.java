package ru.yandex.practicum.showcase_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("store.carts")
public class Cart {

    @Id
    private Integer id;

    @Column("user_id")
    private UUID userId;
}
