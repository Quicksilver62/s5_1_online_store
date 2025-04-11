package ru.yandex.practicum.s5_1_online_store.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.s5_1_online_store.controllers.MainController;
import ru.yandex.practicum.s5_1_online_store.dto.ItemDto;
import ru.yandex.practicum.s5_1_online_store.services.ItemService;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MainController.class)
public class MainPageTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @Test
    void mainPage_ShouldReturnMainView() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Slice<ItemDto> mockSlice = new SliceImpl<>(List.of(
                new ItemDto(1, "Item 1", "Desc 1", "/img1.jpg", 100.0, 5),
                new ItemDto(2, "Item 2", "Desc 2", "/img2.jpg", 200.0, 3)
        ), pageable, false);

        when(itemService.getItems(ArgumentMatchers.any(HttpServletRequest.class),
                ArgumentMatchers.any(HttpServletResponse.class), ArgumentMatchers.any(Pageable.class)))
                .thenReturn(mockSlice);

        mockMvc.perform(get("/main/items")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("paging"))
                .andExpect(model().attribute("items", hasSize(2)));
    }

    @Test
    void handleItemAction_WithPlusAction_ShouldAddItem() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Slice<ItemDto> mockSlice = new SliceImpl<>(List.of(
                new ItemDto(1, "Item 1", "Desc 1", "/img1.jpg", 100.0, 6) // count increased
        ), pageable, false);

        when(itemService.handleItemAction(eq("plus"), eq(1), ArgumentMatchers.any(HttpServletRequest.class),
                ArgumentMatchers.any(Pageable.class)))
                .thenReturn(mockSlice);

        mockMvc.perform(post("/main/items/1")
                        .param("action", "plus"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attribute("items", hasItem(
                        hasProperty("count", equalTo(6))
                )));
    }

    @Test
    void handleItemAction_WithInvalidAction_ShouldReturnMainView() throws Exception {
        when(itemService.handleItemAction(eq("invalid"), eq(1),
                ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Pageable.class)))
                .thenThrow(IllegalArgumentException.class);

        mockMvc.perform(post("/main/items/1")
                        .param("action", "invalid"))
                .andExpect(status().is4xxClientError())
                .andExpect(view().name("invalid-arguments.html"));
    }
}
