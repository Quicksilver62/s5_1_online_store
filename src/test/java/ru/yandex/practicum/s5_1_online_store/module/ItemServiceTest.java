package ru.yandex.practicum.s5_1_online_store.module;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ru.yandex.practicum.s5_1_online_store.dto.ItemDto;
import ru.yandex.practicum.s5_1_online_store.mappers.ItemMapper;
import ru.yandex.practicum.s5_1_online_store.model.Cart;
import ru.yandex.practicum.s5_1_online_store.model.CartItem;
import ru.yandex.practicum.s5_1_online_store.model.Item;
import ru.yandex.practicum.s5_1_online_store.model.User;
import ru.yandex.practicum.s5_1_online_store.repository.ItemRepository;
import ru.yandex.practicum.s5_1_online_store.services.CartService;
import ru.yandex.practicum.s5_1_online_store.services.ItemService;
import ru.yandex.practicum.s5_1_online_store.services.UserService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserService userService;

    @Mock
    private CartService cartService;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private ItemService itemService;

    @Test
    void getItems_ShouldReturnItemsWithCartCounts() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        Cart cart = Cart.builder()
                .id(1)
                .user(user)
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("user_id", userId.toString())});
        when(userService.getUser(userId)).thenReturn(user);
        when(cartService.getUserCart(user)).thenReturn(cart);

        Item item1 = new Item(1, "Item 1", "Desc 1", "/img1.jpg", 100.0, Set.of(cart), new HashSet<>());
        Item item2 = new Item(2, "Item 2", "Desc 2", "/img2.jpg", 200.0, Set.of(cart), new HashSet<>());
        cart.addItem(item1, 1);
        cart.addItem(item2, 2);
        Page<Item> itemPage = new PageImpl<>(List.of(item1, item2), pageable, 2);

        when(itemRepository.findAll(pageable)).thenReturn(itemPage);

        ItemDto itemDto1 = new ItemDto(1, "Item 1", "Desc 1", "/img1.jpg", 100.0,  3);
        ItemDto itemDto2 = new ItemDto(2, "Item 2", "Desc 2", "/img2.jpg", 200.0, 0);
        when(itemMapper.toDto(item1)).thenReturn(itemDto1);
        when(itemMapper.toDto(item2)).thenReturn(itemDto2);

        Slice<ItemDto> result = itemService.getItems(request, response, pageable);

        assertEquals(2, result.getContent().size());
        assertEquals(1, result.getContent().get(0).getCount());
        assertEquals(2, result.getContent().get(1).getCount());
        verify(response).addCookie(any());
    }

    @Test
    void handleItemAction_ShouldUpdateCartAndReturnItems() {
        Integer cartId = 1;
        Integer itemId = 1;
        Pageable pageable = PageRequest.of(0, 10);
        Cart cart = new Cart();

        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("cart_id", cartId.toString())});
        when(cartService.getCartById(cartId)).thenReturn(cart);

        Item item = new Item(1, "Item 1", "Desc 1", "/img1.jpg", 100.0, new HashSet<>(), new HashSet<>());
        Page<Item> itemPage = new PageImpl<>(List.of(item), pageable, 1);
        when(itemRepository.findAll(pageable)).thenReturn(itemPage);

        ItemDto itemDto = new ItemDto(1, "Item 1", "Desc 1", "/img1.jpg", 100.0,  1);
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        Slice<ItemDto> result = itemService.handleItemAction("plus", itemId, request, pageable);

        assertEquals(1, result.getContent().size());
        verify(cartService).handleItemAction("plus", itemId, request);
    }

    @Test
    void getItem_ShouldReturnItemWithCartCount() {
        Integer itemId = 1;
        Integer cartId = 1;
        Item item = new Item(1, "Test Item", "Desc 1", "/img1.jpg", 100.0, new HashSet<>(), new HashSet<>());
        CartItem cartItem = new CartItem();
        cartItem.setCount(2);

        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("cart_id", cartId.toString())});
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(cartService.gerCartItem(itemId, cartId)).thenReturn(cartItem);

        ItemDto expectedDto = new ItemDto(1, "Test Item", "Desc 1", "/img1.jpg", 100.0,  2);
        when(itemMapper.toDto(item)).thenReturn(expectedDto);

        ItemDto result = itemService.getItem(itemId, request);

        assertEquals(2, result.getCount());
        assertEquals("Test Item", result.getTitle());
    }

    @Test
    void getItem_WhenItemNotFound_ShouldThrowException() {
        Integer itemId = 999;
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("cart_id", "1")});
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            itemService.getItem(itemId, request);
        });
    }
}
