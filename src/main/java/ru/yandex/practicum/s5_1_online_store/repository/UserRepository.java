package ru.yandex.practicum.s5_1_online_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.s5_1_online_store.model.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
