package ru.yandex.practicum.showcase_service.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.showcase_service.repository.RoleRepository;
import ru.yandex.practicum.showcase_service.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .flatMap(user ->
                    roleRepository.findRolesByUser(user.getId())
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                            .collectList()
                            .map(authorities -> User.builder()
                                    .username(user.getId().toString())
                                    .password(user.getPassword())
                                    .authorities(authorities)
                                    .build())
                );
    }
}
