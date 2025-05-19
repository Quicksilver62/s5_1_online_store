package ru.yandex.practicum.showcase_service.helpers;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Mono;

import java.util.UUID;

@UtilityClass
public class Helper {

    public Mono<UUID> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .switchIfEmpty(Mono.error(new IllegalStateException("Security context not available")))
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .switchIfEmpty(Mono.error(new IllegalStateException("User not authenticated")))
                .handle((auth, sink) -> {
                    try {
                        sink.next(UUID.fromString(auth.getName()));
                    } catch (IllegalArgumentException e) {
                        sink.error(new IllegalStateException("Invalid user ID format", e));
                    }
                });
    }
}
