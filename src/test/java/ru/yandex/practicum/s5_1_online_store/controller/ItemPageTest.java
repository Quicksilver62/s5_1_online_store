package ru.yandex.practicum.s5_1_online_store.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.s5_1_online_store.controllers.ItemController;
import ru.yandex.practicum.s5_1_online_store.dto.ItemDto;
import ru.yandex.practicum.s5_1_online_store.services.CartService;
import ru.yandex.practicum.s5_1_online_store.services.ItemService;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
public class ItemPageTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private CartService cartService;

    @Test
    void itemPage_ShouldReturnItemView() throws Exception {
        ItemDto mockItem = new ItemDto(1, "Test Item", "Description", "/img.jpg", 100.0, 2);
        when(itemService.getItem(eq(1), any(HttpServletRequest.class))).thenReturn(mockItem);

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attributeExists("item"))
                .andExpect(model().attribute("item", hasProperty("id", is(1))));
    }

    @Test
    void handleItemAction_WithPlusAction_ShouldUpdateItem() throws Exception {
        ItemDto updatedItem = new ItemDto(1, "Test Item", "Description", "/img.jpg", 100.0, 3);
        when(itemService.getItem(eq(1), any(HttpServletRequest.class))).thenReturn(updatedItem);
        doNothing().when(cartService).handleItemAction(eq("plus"), eq(1), any(HttpServletRequest.class));

        mockMvc.perform(post("/items/1")
                        .param("action", "plus"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attribute("item", hasProperty("count", is(3))));
    }

    @Test
    void handleItemAction_WithInvalidAction_ShouldStillReturnItem() throws Exception {
        ItemDto updatedItem = new ItemDto(1, "Test Item", "Description", "/img.jpg", 100.0, 3);
        when(itemService.getItem(eq(1), any(HttpServletRequest.class))).thenReturn(updatedItem);
        doNothing().when(cartService).handleItemAction(eq("plus"), eq(1), any(HttpServletRequest.class));

        mockMvc.perform(post("/items/1")
                        .param("action", "invalid"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"));
    }
}
