package ru.yandex.practicum.showcase_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("store.orders")
public class Order {

    @Id
    private Integer id;

    @Column("user_id")
    private UUID userId;

    @Column("total_sum")
    private Double totalSum;

    @Column("created_at")
    private LocalDateTime createdAt;
}
