package ru.yandex.practicum.showcase_service.module;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.showcase_service.dto.ItemDto;
import ru.yandex.practicum.showcase_service.mappers.ItemMapper;
import ru.yandex.practicum.showcase_service.model.Cart;
import ru.yandex.practicum.showcase_service.model.CartItem;
import ru.yandex.practicum.showcase_service.model.Item;
import ru.yandex.practicum.showcase_service.repository.CartItemsRepository;
import ru.yandex.practicum.showcase_service.repository.ItemRepository;
import ru.yandex.practicum.showcase_service.services.CartService;
import ru.yandex.practicum.showcase_service.services.ItemService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CartService cartService;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private CartItemsRepository cartItemsRepository;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @InjectMocks
    private ItemService itemService;

    @Test
    void getItems_WithValidCookies_ReturnsSliceOfItems() {
        String userId = UUID.randomUUID().toString();
        Integer cartId = 1;
        Pageable pageable = PageRequest.of(0, 10);

        HttpCookie userIdCookie = new HttpCookie("user_id", userId);
        LinkedMultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
        cookies.add("user_id", userIdCookie);
        when(request.getCookies()).thenReturn(cookies);

        Cart cart = Cart.builder().id(cartId).userId(UUID.fromString(userId)).build();
        when(cartService.getUserCart(any())).thenReturn(Mono.just(cart));

        Item item1 = Item.builder().id(1).title("Item 1").price(100.0).build();
        Item item2 = Item.builder().id(2).title("Item 2").price(200.0).build();
        when(itemRepository.findAllBy(pageable)).thenReturn(Flux.just(item1, item2));
        when(itemRepository.count()).thenReturn(Mono.just(2L));

        when(cartItemsRepository.findByItemIdAndCartId(cartId, 1))
                .thenReturn(Mono.just(CartItem.builder().count(1).build()));
        when(cartItemsRepository.findByItemIdAndCartId(cartId, 2))
                .thenReturn(Mono.empty());

        ItemDto dto1 = ItemDto.builder().id(1).title("Item 1").price(100.0).count(1).build();
        ItemDto dto2 = ItemDto.builder().id(2).title("Item 2").price(200.0).count(0).build();
        when(itemMapper.toDto(item1)).thenReturn(dto1);
        when(itemMapper.toDto(item2)).thenReturn(dto2);

        Slice<ItemDto> result = itemService.getItems(request, response, pageable).block();

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(1, result.getContent().get(0).getCount());
        assertEquals(0, result.getContent().get(1).getCount());

        verify(response).addCookie(any());
        verify(cartService).getUserCart(any());
    }

    @Test
    void handleItemAction_ValidAction_UpdatesItemAndReturnsSlice() {
        Integer cartId = 1;
        Integer itemId = 1;
        String action = "plus";
        Pageable pageable = PageRequest.of(0, 10);

        HttpCookie cartCookie = new HttpCookie("cart_id", cartId.toString());
        LinkedMultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
        cookies.add("cart_id", cartCookie);
        when(request.getCookies()).thenReturn(cookies);

        when(cartService.handleItemAction(action, itemId, request)).thenReturn(Mono.empty());

        Item item = Item.builder().id(itemId).title("Item 1").price(100.0).build();
        when(itemRepository.findAllBy(pageable)).thenReturn(Flux.just(item));
        when(itemRepository.count()).thenReturn(Mono.just(1L));

        when(cartItemsRepository.findByItemIdAndCartId(cartId, itemId))
                .thenReturn(Mono.just(CartItem.builder().count(2).build()));

        ItemDto dto = ItemDto.builder().id(itemId).title("Item 1").price(100.0).count(2).build();
        when(itemMapper.toDto(item)).thenReturn(dto);

        Slice<ItemDto> result = itemService.handleItemAction(action, itemId, request, pageable).block();

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(2, result.getContent().getFirst().getCount());

        verify(cartService).handleItemAction(action, itemId, request);
    }

    @Test
    void getItem_WithExistingItem_ReturnsItemDto() {
        Integer itemId = 1;
        Integer cartId = 1;

        HttpCookie cartCookie = new HttpCookie("cart_id", cartId.toString());
        LinkedMultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
        cookies.add("cart_id", cartCookie);
        when(request.getCookies()).thenReturn(cookies);

        Item item = Item.builder().id(itemId).title("Item 1").price(100.0).build();
        when(itemRepository.findById(itemId)).thenReturn(Mono.just(item));

        CartItem cartItem = CartItem.builder().itemId(itemId).cartId(cartId).count(1).build();
        when(cartService.getCartItem(itemId, cartId)).thenReturn(Mono.just(cartItem));

        ItemDto expectedDto = ItemDto.builder().id(itemId).title("Item 1").price(100.0).count(1).build();
        when(itemMapper.toDto(item)).thenReturn(expectedDto);

        ItemDto result = itemService.getItem(itemId, request).block();

        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals(1, result.getCount());
    }

    @Test
    void getItems_WithMissingUserIdCookie_ThrowsException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(request.getCookies()).thenReturn(new LinkedMultiValueMap<>());

        assertThrows(IllegalArgumentException.class, () -> {
            itemService.getItems(request, response, pageable).block();
        });
    }
}
