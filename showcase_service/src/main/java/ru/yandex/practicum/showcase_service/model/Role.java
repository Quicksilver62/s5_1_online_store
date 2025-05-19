package ru.yandex.practicum.showcase_service.model;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("store.roles")
public class Role {

    @Id
    private Integer id;

    @Column("role_name")
    private String roleName;
}
