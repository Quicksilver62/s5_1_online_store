package ru.yandex.practicum.s5_1_online_store.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.s5_1_online_store.services.OrderService;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public Mono<String> ordersPage(Model model, ServerHttpRequest request) {
        return orderService.getOrders(request)
                .collectList()
                .doOnNext(orders -> model.addAttribute("orders", orders))
                .thenReturn("orders");
    }

    @GetMapping("/{id}")
    public Mono<String> itemPage(@PathVariable("id") Integer id, Model model, ServerHttpRequest request) {
        return orderService.getOrder(id, request)
                .doOnNext(order -> model.addAttribute("order", order))
                .thenReturn("order");
    }
}
