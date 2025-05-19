package ru.yandex.practicum.showcase_service.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.showcase_service.helpers.PageableWrapper;
import ru.yandex.practicum.showcase_service.services.ItemService;

@Controller
@RequestMapping("/main/items")
@RequiredArgsConstructor
public class MainController {

    private final ItemService itemService;

    @GetMapping
    public Mono<String> mainPage(Model model,
                                @RequestParam(required = false) String search,
                                @RequestParam(required = false) String sort,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return itemService.getItems(pageable)
                .doOnSuccess(items -> {
                    model.addAttribute("items", items.getContent());
                    model.addAttribute("paging", new PageableWrapper(pageable, items.hasNext()));
                    model.addAttribute("search", search);
                    model.addAttribute("sort", sort);
                })
                .thenReturn("main");
    }

    @PostMapping("/{itemId}")
    @PreAuthorize("isAuthenticated()")
    public Mono<String> handleItemAction(@RequestParam String action,
                                   @PathVariable("itemId") Integer itemId,
                                   Model model,
                                   @RequestParam(required = false) String search,
                                   @RequestParam(required = false) String sort,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return itemService.handleItemAction(action, itemId, pageable)
                .doOnSuccess(items -> {
                    model.addAttribute("items", items.getContent());
                    model.addAttribute("paging", new PageableWrapper(pageable, items.hasNext()));
                    model.addAttribute("search", search);
                    model.addAttribute("sort", sort);
                })
                .thenReturn("main");
    }
}
