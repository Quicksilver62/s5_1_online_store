package ru.yandex.practicum.showcase_service.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.showcase_service.facades.CartFacade;
import ru.yandex.practicum.showcase_service.services.CartService;

import java.util.Collections;

@Controller
@RequestMapping("/cart/items")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CartFacade cartFacade;

    @GetMapping
    public Mono<String> cartPage(Model model, ServerHttpRequest request) {
        return cartFacade.getCartModel(request)
                .doOnNext(cartModel -> {
                    model.addAttribute("items", cartModel.items());
                    model.addAttribute("total", cartModel.total());
                    model.addAttribute("isAvailable", cartModel.isAvailable());
                })
                .thenReturn("cart");
    }

    @PostMapping("/{itemId}")
    public Mono<String> handleItemAction(@RequestParam String action,
                                   @PathVariable("itemId") Integer itemId,
                                   Model model,
                                   ServerHttpRequest request) {
        return cartFacade.handleItemActionAndGetModel(action, itemId, request)
                .doOnNext(cartModel -> {
                    model.addAttribute("items", cartModel.items());
                    model.addAttribute("total", cartModel.total());
                    model.addAttribute("isAvailable", cartModel.isAvailable());
                })
                .thenReturn("cart");
    }

    @PostMapping("/buy")
    public Mono<String> buy(Model model, ServerHttpRequest request) {
        return cartService.buy(request)
                .then(Mono.fromCallable(() -> {
                    model.addAttribute("items", Collections.EMPTY_LIST);
                    model.addAttribute("total", 0.0);
                    model.addAttribute("isAvailable", false);
                    return "cart";
                }));
    }
}
