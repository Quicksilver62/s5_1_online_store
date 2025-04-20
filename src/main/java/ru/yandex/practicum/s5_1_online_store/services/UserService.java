package ru.yandex.practicum.s5_1_online_store.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.s5_1_online_store.model.User;
import ru.yandex.practicum.s5_1_online_store.repository.UserRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Mono<User> getUser(UUID uuid) {
        return userRepository.findById(uuid);
    }
}
