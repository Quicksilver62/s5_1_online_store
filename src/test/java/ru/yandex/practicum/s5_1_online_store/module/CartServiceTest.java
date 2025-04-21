package ru.yandex.practicum.s5_1_online_store.module;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.s5_1_online_store.mappers.ItemMapper;
import ru.yandex.practicum.s5_1_online_store.model.*;
import ru.yandex.practicum.s5_1_online_store.repository.CartItemsRepository;
import ru.yandex.practicum.s5_1_online_store.repository.CartRepository;
import ru.yandex.practicum.s5_1_online_store.repository.ItemRepository;
import ru.yandex.practicum.s5_1_online_store.services.CartService;
import ru.yandex.practicum.s5_1_online_store.services.OrderService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private OrderService orderService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemsRepository cartItemsRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private ServerHttpRequest request;

    @InjectMocks
    private CartService cartService;

    @Test
    void getUserCart_NewCart_CreatesAndReturnsNewCart() {
        UUID userId = UUID.randomUUID();
        Cart newCart = Cart.builder().id(1).userId(userId).build();

        when(cartRepository.findByUserId(userId)).thenReturn(Mono.empty());
        when(cartRepository.save(any())).thenReturn(Mono.just(newCart));

        Cart result = cartService.getUserCart(userId).block();
        assertNotNull(result);
        assertEquals(newCart, result);

        verify(cartRepository).findByUserId(userId);
        verify(cartRepository).save(any());
    }

    @Test
    void getCartById_ExistingCart_ReturnsCart() {
        Cart existingCart = Cart.builder().id(1).userId(UUID.randomUUID()).build();

        when(cartRepository.findById(1)).thenReturn(Mono.just(existingCart));

        Cart result = cartService.getCartById(1).block();
        assertEquals(existingCart, result);
    }

    @Test
    void getCartById_NonExistingCart_ReturnsNull() {
        when(cartRepository.findById(1)).thenReturn(Mono.empty());

        Cart result = cartService.getCartById(1).block();
        assertNull(result);
    }

    @Test
    void getCartItem_ExistingItem_ReturnsItem() {
        CartItem cartItem = CartItem.builder()
                .id(new CartItemId(1, 1))
                .count(1)
                .build();

        when(cartItemsRepository.findById_ItemIdAndId_CartId(1, 1)).thenReturn(Mono.just(cartItem));

        CartItem result = cartService.getCartItem(1, 1).block();
        assertEquals(cartItem, result);
    }

    @Test
    void addNewItemToCart_ValidActionAndItem_AddsItem() {
        Item item = Item.builder().id(1).price(100.0).build();
        when(itemRepository.findById(1)).thenReturn(Mono.just(item));
        when(cartItemsRepository.save(any())).thenReturn(Mono.just(new CartItem()));

        cartService.addNewItemToCart("plus", 1, 1).block();

        verify(itemRepository).findById(1);
        verify(cartItemsRepository).save(any());
    }

    @Test
    void addNewItemToCart_InvalidAction_DoesNothing() {
        cartService.addNewItemToCart("invalid", 1, 1).block();

        verifyNoInteractions(itemRepository, cartItemsRepository);
    }

    @Test
    void addNewItemToCart_NonExistingItem_ThrowsException() {
        when(itemRepository.findById(1)).thenReturn(Mono.empty());

        assertThrows(NoSuchElementException.class, () -> {
            cartService.addNewItemToCart("plus", 1, 1).block();
        });
    }

    @Test
    void addItemToCart_IncrementsCountAndSaves() {
        CartItem cartItem = CartItem.builder()
                .id(new CartItemId(1, 1))
                .count(1)
                .build();

        when(cartItemsRepository.save(cartItem)).thenReturn(Mono.just(cartItem));

        cartService.addItemToCart(cartItem).block();

        assertEquals(2, cartItem.getCount());
        verify(cartItemsRepository).save(cartItem);
    }

    @Test
    void removeItemFromCart_CountGreaterThan1_DecrementsCount() {
        CartItem cartItem = CartItem.builder()
                .id(new CartItemId(1, 1))
                .count(2)
                .build();

        when(cartItemsRepository.save(cartItem)).thenReturn(Mono.just(cartItem));

        cartService.removeItemFromCart(cartItem).block();

        assertEquals(1, cartItem.getCount());
        verify(cartItemsRepository).save(cartItem);
        verify(cartItemsRepository, never()).delete(any());
    }

    @Test
    void removeItemFromCart_CountEquals1_DeletesItem() {
        CartItem cartItem = CartItem.builder()
                .id(new CartItemId(1, 1))
                .count(1)
                .build();

        when(cartItemsRepository.delete(cartItem)).thenReturn(Mono.empty());

        cartService.removeItemFromCart(cartItem).block();

        verify(cartItemsRepository).delete(cartItem);
        verify(cartItemsRepository, never()).save(any());
    }

    @Test
    void removeAllFromCart_DeletesItem() {
        CartItem cartItem = new CartItem();
        when(cartItemsRepository.delete(cartItem)).thenReturn(Mono.empty());

        cartService.removeAllFromCart(cartItem).block();

        verify(cartItemsRepository).delete(cartItem);
    }

    @Test
    void buy_CreatesOrderAndClearsCart() {
        Integer cartId = 1;
        UUID userId = UUID.randomUUID();
        Cart cart = Cart.builder().id(cartId).userId(userId).build();

        CartItem cartItem = CartItem.builder()
                .id(new CartItemId(1, cartId))
                .count(1)
                .item(Item.builder().id(1).price(100.0).build())
                .build();

        HttpCookie cookie = new HttpCookie("cart_id", cartId.toString());
        MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
        cookies.add("cart_id", cookie);

        when(request.getCookies()).thenReturn(cookies);

        when(cartRepository.findById(cartId)).thenReturn(Mono.just(cart));
        when(cartItemsRepository.findByCartIdWithItem(cartId)).thenReturn(Flux.just(cartItem));
        when(orderService.saveOrder(userId, 100.0, List.of(cartItem))).thenReturn(Mono.just(new Order()));
        when(cartItemsRepository.deleteAllById_CartId(cartId)).thenReturn(Mono.empty());

        cartService.buy(request).block();

        verify(cartRepository).findById(cartId);
        verify(cartItemsRepository).findByCartIdWithItem(cartId);
        verify(orderService).saveOrder(userId, 100.0, List.of(cartItem));
        verify(cartItemsRepository).deleteAllById_CartId(cartId);
    }
}
