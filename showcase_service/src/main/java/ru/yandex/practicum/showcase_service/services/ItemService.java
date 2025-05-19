package ru.yandex.practicum.showcase_service.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.showcase_service.dto.ItemDto;
import ru.yandex.practicum.showcase_service.facades.ItemFacade;
import ru.yandex.practicum.showcase_service.mappers.ItemMapper;
import ru.yandex.practicum.showcase_service.model.CartItem;
import ru.yandex.practicum.showcase_service.model.Item;
import ru.yandex.practicum.showcase_service.repository.CartItemsRepository;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final CartService cartService;
    private final ItemMapper itemMapper;
    private final CartItemsRepository cartItemsRepository;
    private final ItemFacade itemFacade;

    public Mono<Slice<ItemDto>> getItems(Pageable pageable) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(auth -> {
                    try {
                        return cartService.getUserCart()
                                .flatMap(cart -> getItemsWithCount(cart.getId(), pageable))
                                .onErrorResume(e -> getItemsWithoutCount(pageable));
                    } catch (IllegalArgumentException e) {
                        return getItemsWithoutCount(pageable);
                    }
                })
                .switchIfEmpty(getItemsWithoutCount(pageable));
    }

    public Mono<Slice<ItemDto>> handleItemAction(String action, Integer itemId, Pageable pageable) {
        return cartService.handleItemAction(action, itemId)
                .then(cartService.getUserCart()
                        .flatMap(cart -> getItemsWithCount(cart.getId(), pageable)));
    }

    public Mono<ItemDto> getItem(Integer id) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(auth -> {
                    try {
                        return cartService.getUserCart()
                                .flatMap(cart -> Mono.zip(
                                        itemFacade.findById(id)
                                                .switchIfEmpty(Mono.error(
                                                        new NoSuchElementException(id + " not found"))),
                                        cartService.getCartItem(id, cart.getId())
                                ))
                                .map(tuple -> {
                                    Item item = tuple.getT1();
                                    CartItem cartItem = tuple.getT2();
                                    ItemDto dto = itemMapper.toDto(item);
                                    dto.setCount(cartItem.getCount());
                                    return dto;
                                });
                    } catch (IllegalArgumentException e) {
                        return getItemDto(id);
                    }
                })
                .switchIfEmpty(getItemDto(id));
    }

    private Mono<Slice<ItemDto>> getItemsWithCount(Integer cartId, Pageable pageable) {
        return itemFacade.getItemsSlice(pageable)
                .flatMap(slice -> Flux.fromIterable(slice.getContent())
                        .flatMap(item ->
                                cartItemsRepository.findByItemIdAndCartId(cartId, item.getId())
                                        .map(CartItem::getCount)
                                        .defaultIfEmpty(0)
                                        .map(count -> {
                                            ItemDto dto = itemMapper.toDto(item);
                                            dto.setCount(count);
                                            return dto;
                                        })
                        )
                        .collectList()
                        .map(items -> new SliceImpl<>(items, pageable, slice.hasNext())
                        ));
    }

    private Mono<Slice<ItemDto>> getItemsWithoutCount(Pageable pageable) {
        return itemFacade.getItemsSlice(pageable)
                .flatMap(slice -> Flux.fromIterable(slice.getContent())
                        .map(item -> {
                            ItemDto dto = itemMapper.toDto(item);
                            dto.setCount(0);
                            return dto;
                        })
                        .collectList()
                        .map(items -> new SliceImpl<>(items, pageable, slice.hasNext())
                        ));
    }

    private Mono<ItemDto> getItemDto(Integer id) {
        return itemFacade.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException(id + " not found")))
                .map(item -> {
                    ItemDto dto = itemMapper.toDto(item);
                    dto.setCount(0);
                    return dto;
                });
    }
}
