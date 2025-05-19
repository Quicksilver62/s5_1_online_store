package ru.yandex.practicum.showcase_service.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.showcase_service.model.Role;

import java.util.UUID;

@Repository
public interface RoleRepository extends ReactiveCrudRepository<Role, Integer> {

    @Query("""
        SELECT r.*
        FROM store.roles r
            JOIN store.user_roles ur ON r.id = ur.role_id 
        WHERE ur.user_id = :userId
        """)
    Flux<Role> findRolesByUser(UUID userId);
}
