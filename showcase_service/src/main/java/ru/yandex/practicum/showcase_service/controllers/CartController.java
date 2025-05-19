package ru.yandex.practicum.showcase_service.controllers;

import lombok.RequiredArgsConstructor;
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
    public Mono<String> cartPage(Model model) {
        return cartFacade.getCartModel()
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
                                   Model model) {
        return cartFacade.handleItemActionAndGetModel(action, itemId)
                .doOnNext(cartModel -> {
                    model.addAttribute("items", cartModel.items());
                    model.addAttribute("total", cartModel.total());
                    model.addAttribute("isAvailable", cartModel.isAvailable());
                })
                .thenReturn("cart");
    }

    @PostMapping("/buy")
    public Mono<String> buy(Model model) {
        return cartService.buy()
                .then(Mono.fromCallable(() -> {
                    model.addAttribute("items", Collections.EMPTY_LIST);
                    model.addAttribute("total", 0.0);
                    model.addAttribute("isAvailable", false);
                    return "cart";
                }));
    }
}
