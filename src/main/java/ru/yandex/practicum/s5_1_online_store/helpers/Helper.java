package ru.yandex.practicum.s5_1_online_store.helpers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;

import java.util.Objects;

@UtilityClass
public class Helper {

    private final String USER_ID = "user_id";
    private final String CART_ID = "cart_id";

    public String getUserIdFromCookie(HttpServletRequest request) {
        return getCookie(request, USER_ID);
    }

    public Integer getCartIdFromCookie(HttpServletRequest request) {
        return Integer.parseInt(Objects.requireNonNull(getCookie(request, CART_ID)));
    }

    public void setCartIdCookie(HttpServletResponse response, Integer cartId) {
        Cookie cookie = new Cookie(CART_ID, cartId.toString());
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private String getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}
