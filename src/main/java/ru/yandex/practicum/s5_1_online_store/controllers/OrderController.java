package ru.yandex.practicum.s5_1_online_store.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.yandex.practicum.s5_1_online_store.dto.OrderDto;
import ru.yandex.practicum.s5_1_online_store.services.OrderService;

import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public String ordersPage(Model model, HttpServletRequest request) {
        List<OrderDto> orders = orderService.getOrders(request);
        model.addAttribute("orders", orders);
        return "orders";
    }

    @GetMapping("/{id}")
    public String itemPage(@PathVariable("id") Integer id, Model model, HttpServletRequest request) {
        OrderDto order = orderService.getOrder(id, request);
        model.addAttribute("order", order);
        return "order";
    }
}
