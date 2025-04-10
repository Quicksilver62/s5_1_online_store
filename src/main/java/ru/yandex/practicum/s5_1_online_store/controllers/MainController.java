package ru.yandex.practicum.s5_1_online_store.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.s5_1_online_store.dto.ItemDto;
import ru.yandex.practicum.s5_1_online_store.helpers.PageableWrapper;
import ru.yandex.practicum.s5_1_online_store.services.ItemService;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ItemService itemService;

    @GetMapping
    public String mainPage(Model model,
                           @RequestParam(required = false) String search,
                           @RequestParam(required = false) String sort,
                           @PageableDefault(size = 10) Pageable pageable,
                           HttpServletRequest request,
                           HttpServletResponse response) {

        Slice<ItemDto> items = itemService.getItems(request, response, pageable);

        model.addAttribute("items", items.getContent());
        model.addAttribute("paging", new PageableWrapper(pageable, items.hasNext()));
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);

        return "main";
    }

    @PostMapping("/main/items/{itemId}")
    public String handleItemAction(@RequestParam String action,
                                   @PathVariable("itemId") Integer itemId,
                                   Model model,
                                   @RequestParam(required = false) String search,
                                   @RequestParam(required = false) String sort,
                                   @PageableDefault(size = 10) Pageable pageable,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        Slice<ItemDto> items = itemService.handleItemAction(action, itemId, request, response, pageable);

        model.addAttribute("items", items.getContent());
        model.addAttribute("paging", new PageableWrapper(pageable, items.hasNext()));
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        return "main";
    }
}
