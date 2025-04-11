package ru.yandex.practicum.s5_1_online_store.services;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.s5_1_online_store.dto.ItemDto;
import ru.yandex.practicum.s5_1_online_store.helpers.Helper;
import ru.yandex.practicum.s5_1_online_store.mappers.ItemMapper;
import ru.yandex.practicum.s5_1_online_store.model.*;
import ru.yandex.practicum.s5_1_online_store.repository.CartItemsRepository;
import ru.yandex.practicum.s5_1_online_store.repository.CartRepository;
import ru.yandex.practicum.s5_1_online_store.repository.ItemRepository;
import ru.yandex.practicum.s5_1_online_store.repository.OrderRepository;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemsRepository cartItemsRepository;
    private final ItemMapper itemMapper;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;

    public Cart getUserCart(User user) {
        var cartOpt = cartRepository.findByUserId(user.getId());
        if (cartOpt.isPresent()) {
            return cartOpt.get();
        }
        var cart = Cart.builder()
                .user(user)
                .build();
        cartRepository.save(cart);
        return cart;
    }

    public Cart getCartById(Integer id) {
        return cartRepository.findById(id).orElseThrow(IllegalArgumentException::new);
    }

    public CartItem gerCartItem(Integer itemId, Integer cartId) {
        return cartItemsRepository.findCartItemByItemIdAndCartId(itemId, cartId)
                .orElseThrow(IllegalArgumentException::new);
    }

    public void addNewItemToCart(Cart cart, Item item) {
        cart.addItem(item, 1);
        cartRepository.save(cart);
    }

    public void addItemToCart(CartItem cartItem) {
        cartItem.setCount(cartItem.getCount() + 1);
        cartItemsRepository.save(cartItem);
    }

    public void removeItemFromCart(CartItem cartItem) {
        if (Objects.isNull(cartItem)) {
            return;
        }
        var count = cartItem.getCount() - 1;
        if (count == 0) {
            cartItemsRepository.delete(cartItem);
        } else {
            cartItem.setCount(count);
            cartItemsRepository.save(cartItem);
        }
    }

    public void removeAllFromCart(CartItem cartItem) {
        cartItemsRepository.delete(cartItem);
    }

    public List<ItemDto> getCartItems(HttpServletRequest request) {
        Integer cartId = Helper.getCartIdFromCookie(request);
        Cart cart = getCartById(cartId);
        return cart.getCartItems().stream()
                .map(cartItem -> {
                    var item = cartItem.getItem();
                    var dto = itemMapper.toDto(item);
                    dto.setCount(cartItem.getCount());
                    return dto;
                })
                .toList();
    }

    public void handleItemAction(String action, Integer itemId, HttpServletRequest request) {
        Integer cartId = Helper.getCartIdFromCookie(request);
        Cart cart = getCartById(cartId);
        var cartItem = cart.getCartItems().stream()
                .filter(ci -> ci.getItem().getId().equals(itemId))
                .findFirst()
                .orElse(null);
        switch (action) {
            case "plus":
                if (Objects.isNull(cartItem)) {
                    addNewItemToCart(cart, itemRepository.findById(itemId).orElseThrow());
                } else {
                    addItemToCart(cartItem);
                }
                break;
            case "minus":
                removeItemFromCart(cartItem);
                break;
            case "delete":
                removeAllFromCart(cartItem);
                break;
            default:
                throw new IllegalArgumentException("Unknown action: " + action);

        }
    }

    @Transactional
    public void buy(HttpServletRequest request) {
        Integer cartId = Helper.getCartIdFromCookie(request);
        Cart cart = getCartById(cartId);
        if (Objects.isNull(cart)) {
            return;
        }
        Order order = Order.builder()
                .user(cart.getUser())
                .totalSum(cart.getCartItems().stream()
                        .mapToDouble(ci -> ci.getItem().getPrice())
                        .sum()
                )
                .build();
        cart.getCartItems().forEach(cartItem -> order.addItem(cartItem.getItem(), cartItem.getCount()));
        orderRepository.save(order);
        cart.getCartItems().clear();
    }
}
