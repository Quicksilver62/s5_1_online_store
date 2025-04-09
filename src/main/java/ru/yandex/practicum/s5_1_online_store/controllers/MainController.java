package ru.yandex.practicum.s5_1_online_store.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.yandex.practicum.s5_1_online_store.dto.ItemDto;
import ru.yandex.practicum.s5_1_online_store.services.ItemService;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ItemService itemService;

    @GetMapping
    public String mainPage(Model model, HttpServletRequest request, HttpServletResponse response, Pageable pageable) {
        Slice<ItemDto> items = itemService.getItems(request, response, pageable);
        model.addAttribute("items", items.getContent());
        model.addAttribute("paging", pageable);
        return "main";
    }
}
