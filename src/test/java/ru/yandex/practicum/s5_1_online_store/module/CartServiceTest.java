package ru.yandex.practicum.s5_1_online_store.module;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.s5_1_online_store.dto.ItemDto;
import ru.yandex.practicum.s5_1_online_store.mappers.ItemMapper;
import ru.yandex.practicum.s5_1_online_store.model.*;
import ru.yandex.practicum.s5_1_online_store.repository.CartItemsRepository;
import ru.yandex.practicum.s5_1_online_store.repository.CartRepository;
import ru.yandex.practicum.s5_1_online_store.repository.ItemRepository;
import ru.yandex.practicum.s5_1_online_store.repository.OrderRepository;
import ru.yandex.practicum.s5_1_online_store.services.CartService;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemsRepository cartItemsRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private CartService cartService;

    private User testUser;
    private Cart testCart;
    private Item testItem;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testCart = Cart.builder().id(1).user(testUser).build();
        testItem = new Item(1, "Test Item", "Desc 1", "/img1.jpg", 100.0, new HashSet<>(), new HashSet<>());
        testCartItem = new CartItem(new CartItemId(1, 1), testItem, 1);
        testCart.getCartItems().add(testCartItem);
    }

    @Test
    void getUserCart_WhenCartExists_ShouldReturnCart() {
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));

        Cart result = cartService.getUserCart(testUser);

        assertEquals(testCart, result);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void getUserCart_WhenCartNotExists_ShouldCreateNewCart() {
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        Cart result = cartService.getUserCart(testUser);

        assertNotNull(result);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void getCartById_ShouldReturnCart() {
        when(cartRepository.findById(1)).thenReturn(Optional.of(testCart));

        Cart result = cartService.getCartById(1);

        assertEquals(testCart, result);
    }

    @Test
    void getCartById_WhenNotFound_ShouldThrowException() {
        when(cartRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            cartService.getCartById(999);
        });
    }

    @Test
    void addNewItemToCart_ShouldAddItem() {
        when(cartRepository.save(testCart)).thenReturn(testCart);

        cartService.addNewItemToCart(testCart, testItem);

        assertEquals(2, testCart.getCartItems().size());
        verify(cartRepository).save(testCart);
    }

    @Test
    void addItemToCart_ShouldIncrementCount() {
        cartService.addItemToCart(testCartItem);

        assertEquals(2, testCartItem.getCount());
        verify(cartItemsRepository).save(testCartItem);
    }

    @Test
    void removeItemFromCart_ShouldDecrementCount() {
        testCartItem.setCount(2);

        cartService.removeItemFromCart(testCartItem);

        assertEquals(1, testCartItem.getCount());
        verify(cartItemsRepository).save(testCartItem);
    }

    @Test
    void removeItemFromCart_WhenCountIsOne_ShouldDeleteItem() {
        testCartItem.setCount(1);

        cartService.removeItemFromCart(testCartItem);

        verify(cartItemsRepository).delete(testCartItem);
        verify(cartItemsRepository, never()).save(any());
    }

    @Test
    void removeAllFromCart_ShouldDeleteItem() {
        cartService.removeAllFromCart(testCartItem);

        verify(cartItemsRepository).delete(testCartItem);
    }

    @Test
    void getCartItems_ShouldReturnItemDtos() {
        // Arrange
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("cart_id", "1")});
        when(cartRepository.findById(1)).thenReturn(Optional.of(testCart));

        ItemDto itemDto = new ItemDto();
        itemDto.setId(1);
        itemDto.setCount(1);
        when(itemMapper.toDto(testItem)).thenReturn(itemDto);

        List<ItemDto> result = cartService.getCartItems(request);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
    }

    @Test
    void handleItemAction_WithPlusAction_ShouldAddItem() {
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("cart_id", "1")});
        when(cartRepository.findById(1)).thenReturn(Optional.of(testCart));
        Item item = new Item(2, "Test Item", "Desc 1", "/img1.jpg", 100.0, new HashSet<>(), new HashSet<>());
        when(itemRepository.findById(2)).thenReturn(Optional.of(item));

        cartService.handleItemAction("plus", 2, request);

        verify(cartRepository).save(any());
    }

    @Test
    void handleItemAction_WithMinusAction_ShouldDecrementCount() {
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("cart_id", "1")});
        when(cartRepository.findById(1)).thenReturn(Optional.of(testCart));

        cartService.handleItemAction("minus", 1, request);

        verify(cartItemsRepository).delete(any());
    }

    @Test
    void handleItemAction_WithDeleteAction_ShouldRemoveItem() {
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("cart_id", "1")});
        when(cartRepository.findById(1)).thenReturn(Optional.of(testCart));

        cartService.handleItemAction("delete", 1, request);

        verify(cartItemsRepository).delete(any());
    }

    @Test
    void handleItemAction_WithUnknownAction_ShouldThrowException() {
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("cart_id", "1")});
        when(cartRepository.findById(1)).thenReturn(Optional.of(testCart));

        assertThrows(IllegalArgumentException.class, () -> {
            cartService.handleItemAction("unknown", 1, request);
        });
    }

    @Test
    @Transactional
    void buy_ShouldCreateOrderAndClearCart() {
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("cart_id", "1")});
        when(cartRepository.findById(1)).thenReturn(Optional.of(testCart));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        cartService.buy(request);

        verify(orderRepository).save(any(Order.class));
        assertTrue(testCart.getCartItems().isEmpty());
    }
}
