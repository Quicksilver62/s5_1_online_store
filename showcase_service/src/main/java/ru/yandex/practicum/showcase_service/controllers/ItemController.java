package ru.yandex.practicum.showcase_service.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.showcase_service.services.CartService;
import ru.yandex.practicum.showcase_service.services.ItemService;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final CartService cartService;

    @GetMapping("/{id}")
    public Mono<String> itemPage(@PathVariable("id") Integer id, Model model, ServerHttpRequest request) {
        return itemService.getItem(id, request)
                .doOnNext(item -> model.addAttribute("item", item))
                .map(item -> "item")
                .defaultIfEmpty("not-found");
    }

    @PostMapping("/{id}")
    public Mono<String> handleItemAction(@RequestParam String action,
                                         @PathVariable("id") Integer id,
                                         Model model,
                                         ServerHttpRequest request) {
        return cartService.handleItemAction(action, id, request)
                .then(itemService.getItem(id, request))
                .doOnNext(item -> model.addAttribute("item", item))
                .map(item -> "item")
                .defaultIfEmpty("not-found");
    }
}
