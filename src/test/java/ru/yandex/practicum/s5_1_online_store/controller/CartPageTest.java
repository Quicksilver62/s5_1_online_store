package ru.yandex.practicum.s5_1_online_store.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.s5_1_online_store.controllers.CartController;
import ru.yandex.practicum.s5_1_online_store.dto.ItemDto;
import ru.yandex.practicum.s5_1_online_store.services.CartService;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
public class CartPageTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @Test
    void cartPage_ShouldReturnCartViewWithItems() throws Exception {
        List<ItemDto> mockItems = List.of(
                new ItemDto(1, "Item 1", "Desc 1", "/img1.jpg", 100.0, 2),
                new ItemDto(2, "Item 2", "Desc 2", "/img2.jpg", 200.0, 1)
        );

        when(cartService.getCartItems(any(HttpServletRequest.class)))
                .thenReturn(mockItems);

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attribute("total", 300.0));
    }

    @Test
    void handleItemAction_WithPlusAction_ShouldUpdateCart() throws Exception {
        List<ItemDto> updatedItems = List.of(
                new ItemDto(1, "Item 1", "Desc 1", "/img1.jpg", 100.0, 3)
        );

        when(cartService.getCartItems(any(HttpServletRequest.class)))
                .thenReturn(updatedItems);

        mockMvc.perform(post("/cart/items/1")
                        .param("action", "plus"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", hasSize(1)));
    }

    @Test
    void buy_ShouldClearCart() throws Exception {
        doNothing().when(cartService).buy(any(HttpServletRequest.class));

        mockMvc.perform(post("/cart/items/buy"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", Collections.EMPTY_LIST))
                .andExpect(model().attribute("total", 0.0));
    }
}
