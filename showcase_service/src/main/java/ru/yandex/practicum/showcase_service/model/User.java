package ru.yandex.practicum.showcase_service.model;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Getter
@Table("store.users")
public class User {

    @Id
    private UUID id;

    private String email;
}
