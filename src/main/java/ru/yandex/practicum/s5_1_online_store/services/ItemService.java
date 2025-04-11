package ru.yandex.practicum.s5_1_online_store.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.s5_1_online_store.dto.ItemDto;
import ru.yandex.practicum.s5_1_online_store.helpers.Helper;
import ru.yandex.practicum.s5_1_online_store.mappers.ItemMapper;
import ru.yandex.practicum.s5_1_online_store.model.Cart;
import ru.yandex.practicum.s5_1_online_store.model.CartItem;
import ru.yandex.practicum.s5_1_online_store.model.Item;
import ru.yandex.practicum.s5_1_online_store.model.User;
import ru.yandex.practicum.s5_1_online_store.repository.ItemRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;
    private final CartService cartService;
    private final ItemMapper itemMapper;

    public Slice<ItemDto> getItems(HttpServletRequest request, HttpServletResponse response, Pageable pageable) {
        UUID userId = UUID.fromString(Helper.getUserIdFromCookie(request));
        User user = userService.getUser(userId);
        Cart cart = cartService.getUserCart(user);

        Helper.setCartIdCookie(response, cart.getId());
        return getItemsWithCount(cart, pageable);
    }

    public Slice<ItemDto> handleItemAction(String action, Integer itemId, HttpServletRequest request,
                                           Pageable pageable) {
        Integer cartId = Helper.getCartIdFromCookie(request);
        Cart cart = cartService.getCartById(cartId);
        cartService.handleItemAction(action, itemId, request);

        return getItemsWithCount(cart, pageable);
    }

    private Slice<ItemDto> getItemsWithCount(Cart cart, Pageable pageable) {
        Page<Item> items = itemRepository.findAll(pageable);

        List<ItemDto> dtoList = items.stream()
                .map(item -> {
                    Integer cartCount = cart.getCartItems().stream()
                            .filter(cartItem -> cartItem.getItem().getId().equals(item.getId()))
                            .findFirst()
                            .map(CartItem::getCount)
                            .orElse(0);
                    var dto = itemMapper.toDto(item);
                    dto.setCount(cartCount);
                    return dto;
                })
                .toList();

        return new SliceImpl<>(dtoList, items.getPageable(), items.hasNext());
    }

    public ItemDto getItem(Integer id, HttpServletRequest request) {
        Integer cartId = Helper.getCartIdFromCookie(request);
        Item item = itemRepository.findById(id).orElseThrow(() -> new NoSuchElementException(id + " not found"));
        CartItem cartItem = cartService.gerCartItem(id, cartId);
        ItemDto itemDto = itemMapper.toDto(item);
        itemDto.setCount(cartItem.getCount());
        return itemDto;
    }
}
