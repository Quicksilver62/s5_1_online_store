package ru.yandex.practicum.showcase_service.helpers;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.showcase_service.model.Cart;

import java.security.Principal;
import java.util.UUID;

@UtilityClass
public class Helper {

    private final String CART_ID = "cart_id";

    public Mono<Integer> getCartIdFromCookie(ServerHttpRequest request) {
        return getCookieValue(request, CART_ID)
                .map(Integer::parseInt);
    }

    private Mono<String> getCookieValue(ServerHttpRequest request, String name) {
        return Mono.justOrEmpty(request.getCookies()
                        .getFirst(name))
                .map(HttpCookie::getValue)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Cookie '" + name + "' not found")));
    }

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
