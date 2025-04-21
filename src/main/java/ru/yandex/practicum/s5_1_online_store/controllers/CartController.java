package ru.yandex.practicum.s5_1_online_store.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.s5_1_online_store.dto.ItemDto;
import ru.yandex.practicum.s5_1_online_store.services.CartService;

import java.util.Collections;

@Controller
@RequestMapping("/cart/items")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public Mono<String> cartPage(Model model, ServerHttpRequest request) {
        return cartService.getCartItems(request)
                .collectList()
                .doOnNext(items -> {
                    model.addAttribute("items", items);
                    model.addAttribute("total", items.stream()
                            .mapToDouble(ItemDto::getPrice)
                            .sum());
                })
                .thenReturn("cart");
    }

    @PostMapping("/{itemId}")
    public Mono<String> handleItemAction(@RequestParam String action,
                                   @PathVariable("itemId") Integer itemId,
                                   Model model,
                                   ServerHttpRequest request) {
        return cartService.handleItemAction(action, itemId, request)
                .then(cartService.getCartItems(request)
                        .collectList()
                        .doOnSuccess(items -> {
                            model.addAttribute("items", items);
                            model.addAttribute("total", items.stream()
                                    .mapToDouble(ItemDto::getPrice)
                                    .sum());
                        })
                )
                .thenReturn("cart");
    }

    @PostMapping("/buy")
    public Mono<String> buy(Model model, ServerHttpRequest request) {
        return cartService.buy(request)
                .then(Mono.fromCallable(() -> {
                    model.addAttribute("items", Collections.EMPTY_LIST);
                    model.addAttribute("total", 0.0);
                    return "cart";
                }));
    }
}
