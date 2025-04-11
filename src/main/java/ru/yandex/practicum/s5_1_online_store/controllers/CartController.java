package ru.yandex.practicum.s5_1_online_store.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.s5_1_online_store.dto.ItemDto;
import ru.yandex.practicum.s5_1_online_store.services.CartService;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/cart/items")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public String cartPage(Model model, HttpServletRequest request) {
        List<ItemDto> items = cartService.getCartItems(request);
        model.addAttribute("items", items);
        model.addAttribute("total", items.stream()
                .mapToDouble(ItemDto::getPrice)
                .sum());
        return "cart";
    }

    @PostMapping("/{itemId}")
    public String handleItemAction(@RequestParam String action,
                                   @PathVariable("itemId") Integer itemId,
                                   Model model,
                                   HttpServletRequest request) {
        cartService.handleItemAction(action, itemId, request);
        List<ItemDto> items = cartService.getCartItems(request);
        model.addAttribute("items", items);
        model.addAttribute("total", items.stream()
                .mapToDouble(ItemDto::getPrice)
                .sum());
        return "cart";
    }

    @PostMapping("/{buy}")
    public String buy(Model model, HttpServletRequest request) {
        cartService.buy(request);
        model.addAttribute("items", Collections.EMPTY_LIST);
        model.addAttribute("total", 0);
        return "cart";
    }
}
