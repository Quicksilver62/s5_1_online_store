package ru.yandex.practicum.s5_1_online_store.helpers;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

@UtilityClass
public class Helper {

    private final String USER_ID = "user_id";
    private final String CART_ID = "cart_id";

    public Mono<String> getUserIdFromCookie(ServerHttpRequest request) {
        return getCookieValue(request, USER_ID);
    }

    public Mono<Integer> getCartIdFromCookie(ServerHttpRequest request) {
        return getCookieValue(request, CART_ID)
                .map(Integer::parseInt);
    }

    public Mono<Void> setCartIdCookie(ServerHttpResponse response, Integer cartId) {
        return Mono.fromRunnable(() -> {
            ResponseCookie cookie = ResponseCookie.from(CART_ID, cartId.toString())
                    .path("/")
                    .httpOnly(true)
                    .build();
            response.addCookie(cookie);
        });
    }

    private Mono<String> getCookieValue(ServerHttpRequest request, String name) {
        return Mono.justOrEmpty(request.getCookies()
                        .getFirst(name))
                .map(HttpCookie::getValue)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Cookie '" + name + "' not found")));
    }

}
