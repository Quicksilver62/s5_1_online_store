package ru.yandex.practicum.showcase_service.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.showcase_service.services.OrderService;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public Mono<String> ordersPage(Model model) {
        return orderService.getOrders()
                .collectList()
                .doOnNext(orders -> model.addAttribute("orders", orders))
                .thenReturn("orders");
    }

    @GetMapping("/{id}")
    public Mono<String> itemPage(@PathVariable("id") Integer id, Model model) {
        return orderService.getOrder(id)
                .doOnNext(order -> model.addAttribute("order", order))
                .thenReturn("order");
    }
}
