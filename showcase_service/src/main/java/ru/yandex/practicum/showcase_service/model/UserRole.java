package ru.yandex.practicum.showcase_service.model;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Getter
@Table("store.user_roles")
public class UserRole {

    @Id
    private Integer id;

    @Column("user_id")
    private UUID userId;

    @Column("role_id")
    private Integer roleId;
}
