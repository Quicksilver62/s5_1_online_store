package ru.yandex.practicum.s5_1_online_store.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.s5_1_online_store.model.Cart;
import ru.yandex.practicum.s5_1_online_store.model.CartItem;
import ru.yandex.practicum.s5_1_online_store.model.Item;
import ru.yandex.practicum.s5_1_online_store.model.User;
import ru.yandex.practicum.s5_1_online_store.repository.CartItemsRepository;
import ru.yandex.practicum.s5_1_online_store.repository.CartRepository;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemsRepository cartItemsRepository;

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
}
