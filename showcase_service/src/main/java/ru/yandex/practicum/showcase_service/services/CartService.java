package ru.yandex.practicum.showcase_service.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.showcase_service.dto.ItemDto;
import ru.yandex.practicum.showcase_service.helpers.Helper;
import ru.yandex.practicum.showcase_service.model.*;
import ru.yandex.practicum.showcase_service.repository.*;
import ru.yandex.practicum.showcase_service.model.Cart;
import ru.yandex.practicum.showcase_service.model.CartItem;
import ru.yandex.practicum.showcase_service.model.Order;
import ru.yandex.practicum.showcase_service.repository.CartItemsRepository;
import ru.yandex.practicum.showcase_service.repository.CartRepository;
import ru.yandex.practicum.showcase_service.repository.ItemRepository;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final OrderService orderService;
    private final CartRepository cartRepository;
    private final CartItemsRepository cartItemsRepository;
    private final ItemRepository itemRepository;

    public Mono<Cart> getUserCart(UUID userId) {
        return cartRepository.findByUserId(userId)
                .switchIfEmpty(Mono.defer(() -> {
                    Cart newCart = Cart.builder()
                            .userId(userId)
                            .build();
                    return cartRepository.save(newCart);
                }));
    }

    public Mono<Cart> getCartById(Integer id) {
        return cartRepository.findById(id);
    }

    public Mono<CartItem> getCartItem(Integer itemId, Integer cartId) {
        return cartItemsRepository.findByItemIdAndCartId(itemId, cartId);
    }

    public Mono<Void> addNewItemToCart(String action, Integer itemId, Integer cartId) {
        if (!"plus".equalsIgnoreCase(action)) {
            return Mono.empty();
        }
        return itemRepository.findById(itemId)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Item not found")))
                .flatMap(item -> {
                    CartItem newCartItem = CartItem.builder()
                            .cartId(cartId)
                            .itemId(itemId)
                            .count(1)
                            .build();
                    return cartItemsRepository.save(newCartItem).then();
                });
    }

    public Mono<Void> addItemToCart(CartItem cartItem) {
        cartItem.setCount(cartItem.getCount() + 1);
        return cartItemsRepository.save(cartItem).then();
    }

    public Mono<Void> removeItemFromCart(CartItem cartItem) {
        if (cartItem.getCount() <= 1) {
            return cartItemsRepository.delete(cartItem);
        }
        cartItem.setCount(cartItem.getCount() - 1);
        return cartItemsRepository.save(cartItem).then();
    }

    public Mono<Void> removeAllFromCart(CartItem cartItem) {
        return cartItemsRepository.delete(cartItem);
    }

    public Flux<ItemDto> getCartItems(ServerHttpRequest request) {
        return Helper.getCartIdFromCookie(request)
                .flatMapMany(cartItemsRepository::findByCartIdWithItem)
                .map(cartItemWithItem -> ItemDto.builder()
                        .id(cartItemWithItem.getItemId())
                        .title(cartItemWithItem.getItemTitle())
                        .description(cartItemWithItem.getItemDescription())
                        .imgPath(cartItemWithItem.getItemImgPath())
                        .price(cartItemWithItem.getItemPrice())
                        .count(cartItemWithItem.getCount())
                        .build());
    }

    public Mono<Void> handleItemAction(String action, Integer itemId, ServerHttpRequest request) {
        return Helper.getCartIdFromCookie(request)
                .flatMap(cartId -> cartItemsRepository.findByItemIdAndCartId(itemId, cartId)
                        .flatMap(cartItem -> switch (action.toLowerCase()) {
                            case "plus" -> addItemToCart(cartItem);
                            case "minus" -> removeItemFromCart(cartItem);
                            case "delete" -> removeAllFromCart(cartItem);
                            default -> Mono.error(new IllegalArgumentException("Unknown action: " + action));
                        })
                        .switchIfEmpty(addNewItemToCart(action, itemId, cartId))
                );
    }

    @Transactional
    public Mono<Void> buy(ServerHttpRequest request) {
        return Helper.getCartIdFromCookie(request)
                .flatMap(this::getCartById)
                .flatMap(cart -> createOrderFromCart(cart)
                        .then(cartItemsRepository.deleteAllByCartId(cart.getId()))
                );
    }

    private Mono<Order> createOrderFromCart(Cart cart) {
        return cartItemsRepository.findByCartIdWithItem(cart.getId())
                .collectList()
                .flatMap(cartItemsWithItems -> {
                    var cartItems = cartItemsWithItems.stream()
                            .map(cartItemWithItem -> CartItem.builder()
                                    .cartId(cartItemWithItem.getCartId())
                                    .itemId(cartItemWithItem.getItemId())
                                    .count(cartItemWithItem.getCount())
                                    .build())
                            .toList();
                    double totalSum = cartItemsWithItems.stream()
                            .mapToDouble(ci -> ci.getItemPrice() * ci.getCount())
                            .sum();
                    return orderService.saveOrder(cart.getUserId(), totalSum, cartItems);
                });
    }
}
