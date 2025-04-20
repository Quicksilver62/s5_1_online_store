package ru.yandex.practicum.s5_1_online_store.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.s5_1_online_store.dto.ItemDto;
import ru.yandex.practicum.s5_1_online_store.helpers.Helper;
import ru.yandex.practicum.s5_1_online_store.mappers.ItemMapper;
import ru.yandex.practicum.s5_1_online_store.model.CartItem;
import ru.yandex.practicum.s5_1_online_store.model.Item;
import ru.yandex.practicum.s5_1_online_store.repository.CartItemsRepository;
import ru.yandex.practicum.s5_1_online_store.repository.ItemRepository;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final CartService cartService;
    private final ItemMapper itemMapper;
    private final CartItemsRepository cartItemsRepository;

    public Mono<Slice<ItemDto>> getItems(ServerHttpRequest request, ServerHttpResponse response, Pageable pageable) {
        return Helper.getUserIdFromCookie(request)
                .flatMap(userId -> {
                    UUID uuid = UUID.fromString(userId);
                    return cartService.getUserCart(uuid);
                })
                .flatMap(cart -> Helper.setCartIdCookie(response, cart.getId())
                        .thenReturn(cart))
                .flatMap(cart -> getItemsWithCount(cart.getId(), pageable));
    }

    public Mono<Slice<ItemDto>> handleItemAction(String action, Integer itemId, ServerHttpRequest request,
                                           Pageable pageable) {
        return Helper.getCartIdFromCookie(request)
                .flatMap(cartId ->
                        cartService.handleItemAction(action, itemId, request)
                                .then(getItemsWithCount(cartId, pageable))
                );
    }

    private Mono<Slice<ItemDto>> getItemsWithCount(Integer cartId, Pageable pageable) {
        return itemRepository.findAllBy(pageable)
                .flatMap(item ->
                        cartItemsRepository.findById_ItemIdAndId_CartId(cartId, item.getId())
                                .map(CartItem::getCount)
                                .defaultIfEmpty(0)
                                .map(count -> {
                                    ItemDto dto = itemMapper.toDto(item);
                                    dto.setCount(count);
                                    return dto;
                                })
                )
                .collectList()
                .zipWith(itemRepository.count())
                .map(tuple -> new SliceImpl<>(
                        tuple.getT1(),
                        pageable,
                        (pageable.getOffset() + pageable.getPageSize()) < tuple.getT2()
                ));
    }

    public Mono<ItemDto> getItem(Integer id, ServerHttpRequest request) {
        return Helper.getCartIdFromCookie(request)
                .flatMap(cartId -> Mono.zip(
                        itemRepository.findById(id)
                                .switchIfEmpty(Mono.error(new NoSuchElementException(id + " not found"))),
                        cartService.getCartItem(id, cartId)
                ))
                .map(tuple -> {
                    Item item = tuple.getT1();
                    CartItem cartItem = tuple.getT2();
                    ItemDto dto = itemMapper.toDto(item);
                    dto.setCount(cartItem.getCount());
                    return dto;
                });
    }
}
