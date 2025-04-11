package ru.yandex.practicum.s5_1_online_store.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.s5_1_online_store.dto.ItemDto;
import ru.yandex.practicum.s5_1_online_store.services.CartService;
import ru.yandex.practicum.s5_1_online_store.services.ItemService;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final CartService cartService;

    @GetMapping("/{id}")
    public String itemPage(@PathVariable("id") Integer id, Model model, HttpServletRequest request) {
        ItemDto item = itemService.getItem(id, request);
        model.addAttribute("item", item);
        return "item";
    }

    @PostMapping("/{id}")
    public String handleItemAction(@RequestParam String action,
                                   @PathVariable("id") Integer id,
                                   Model model,
                                   HttpServletRequest request) {
        cartService.handleItemAction(action, id, request);
        ItemDto item = itemService.getItem(id, request);
        model.addAttribute("item", item);
        return "item";
    }
}
