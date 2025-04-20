package ru.yandex.practicum.s5_1_online_store.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.s5_1_online_store.dto.ItemDto;
import ru.yandex.practicum.s5_1_online_store.helpers.Helper;
import ru.yandex.practicum.s5_1_online_store.mappers.ItemMapper;
import ru.yandex.practicum.s5_1_online_store.model.*;
import ru.yandex.practicum.s5_1_online_store.repository.*;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemsRepository cartItemsRepository;
    private final ItemMapper itemMapper;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemsRepository orderItemsRepository;

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
        return cartItemsRepository.findById_ItemIdAndId_CartId(itemId, cartId);
    }

    public Mono<Void> addNewItemToCart(String action, Integer itemId, Integer cartId) {
        if (!"plus".equalsIgnoreCase(action)) {
            return Mono.empty();
        }
        return itemRepository.findById(itemId)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Item not found")))
                .flatMap(item -> {
                    CartItem newCartItem = CartItem.builder()
                            .id(new CartItemId(itemId, cartId))
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
                .map(cartItem -> {
                    ItemDto dto = itemMapper.toDto(cartItem.getItem());
                    dto.setCount(cartItem.getCount());
                    return dto;
                });
    }

    public Mono<Void> handleItemAction(String action, Integer itemId, ServerHttpRequest request) {
        return Helper.getCartIdFromCookie(request)
                .flatMap(cartId -> cartItemsRepository.findById_ItemIdAndId_CartId(itemId, cartId)
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
                        .then(cartItemsRepository.deleteAllById_CartId(cart.getId()))
                );
    }

    private Mono<Order> createOrderFromCart(Cart cart) {
        return cartItemsRepository.findByCartIdWithItem(cart.getId())
                .collectList()
                .flatMap(cartItems -> {
                    double totalSum = cartItems.stream()
                            .mapToDouble(ci -> ci.getItem().getPrice() * ci.getCount())
                            .sum();

                    return orderRepository.save(Order.builder()
                                    .userId(cart.getUserId())
                                    .totalSum(totalSum)
                                    .createdAt(LocalDateTime.now())
                                    .build())
                            .flatMap(savedOrder ->
                                    orderItemsRepository.saveAll(
                                            cartItems.stream()
                                                    .map(ci -> new OrderItem(
                                                            new OrderItemId(ci.getId().getItemId(), savedOrder.getId()),
                                                            ci.getCount(),
                                                            ci.getItem()
                                                    ))
                                                    .toList()
                                            )
                                            .then(Mono.just(savedOrder))
                            );
                });
    }
}
