package ru.yandex.practicum.showcase_service.module;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.showcase_service.dto.ItemDto;
import ru.yandex.practicum.showcase_service.facades.ItemFacade;
import ru.yandex.practicum.showcase_service.mappers.ItemMapper;
import ru.yandex.practicum.showcase_service.model.Cart;
import ru.yandex.practicum.showcase_service.model.CartItem;
import ru.yandex.practicum.showcase_service.model.Item;
import ru.yandex.practicum.showcase_service.repository.CartItemsRepository;
import ru.yandex.practicum.showcase_service.services.CartService;
import ru.yandex.practicum.showcase_service.services.ItemService;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock
    private ItemFacade itemFacade;

    @Mock
    private CartService cartService;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private CartItemsRepository cartItemsRepository;

    @InjectMocks
    private ItemService itemService;

    private final UUID testUserId = UUID.randomUUID();
    private final Integer testCartId = 1;
    private final Integer testItemId = 100;
    private final Cart testCart = Cart.builder().id(testCartId).userId(testUserId).build();
    private final CartItem testCartItem = CartItem.builder().cartId(testCartId).itemId(testItemId).count(2).build();
    private final Item testItem = Item.builder().id(testItemId).title("Test Item").price(10.0).build();

    private MockedStatic<ReactiveSecurityContextHolder> securityContextMock;
    private SecurityContext securityContext;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        authentication = mock(Authentication.class);

        securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        securityContextMock = mockStatic(ReactiveSecurityContextHolder.class);
        securityContextMock.when(ReactiveSecurityContextHolder::getContext)
                .thenReturn(Mono.just(securityContext));
    }

    @AfterEach
    void tearDown() {
        securityContextMock.close();
    }

    @Test
    void getItems_shouldReturnItemsWithCount() {
        Pageable pageable = PageRequest.of(0, 10);
        Item item = testItem;
        Slice<Item> itemSlice = new SliceImpl<>(List.of(item), pageable, false);

        when(cartService.getUserCart()).thenReturn(Mono.just(testCart));
        when(itemFacade.getItemsSlice(pageable)).thenReturn(Mono.just(itemSlice));
        when(cartItemsRepository.findByItemIdAndCartId(testCartId, testItemId)).thenReturn(Mono.just(testCartItem));
        when(itemMapper.toDto(item)).thenReturn(ItemDto.builder().id(testItemId).title(item.getTitle()).build());

        StepVerifier.create(itemService.getItems(pageable))
                .expectNextMatches(slice ->
                        slice.getContent().size() == 1 &&
                                slice.getContent().get(0).getId().equals(testItemId) &&
                                slice.getContent().get(0).getCount() == 2
                )
                .verifyComplete();
    }

    @Test
    void getItems_shouldFallbackToWithoutCount() {
        Pageable pageable = PageRequest.of(0, 10);
        Item item = testItem;
        Slice<Item> itemSlice = new SliceImpl<>(List.of(item), pageable, false);

        when(cartService.getUserCart()).thenReturn(Mono.error(new RuntimeException("failed")));
        when(itemFacade.getItemsSlice(pageable)).thenReturn(Mono.just(itemSlice));
        when(itemMapper.toDto(item)).thenReturn(ItemDto.builder().id(testItemId).title(item.getTitle()).build());

        StepVerifier.create(itemService.getItems(pageable))
                .expectNextMatches(slice ->
                        slice.getContent().get(0).getId().equals(testItemId) &&
                                slice.getContent().get(0).getCount() == 0
                )
                .verifyComplete();
    }

    @Test
    void getItem_shouldReturnItemWithCartCount() {
        when(cartService.getUserCart()).thenReturn(Mono.just(testCart));
        when(itemFacade.findById(testItemId)).thenReturn(Mono.just(testItem));
        when(cartService.getCartItem(testItemId, testCartId)).thenReturn(Mono.just(testCartItem));
        when(itemMapper.toDto(testItem)).thenReturn(ItemDto.builder().id(testItemId).title("mapped").build());

        StepVerifier.create(itemService.getItem(testItemId))
                .expectNextMatches(dto ->
                        dto.getId().equals(testItemId) &&
                                dto.getCount() == 2
                )
                .verifyComplete();
    }

    @Test
    void getItem_shouldReturnItemWithZeroCountIfNoCartItem() {
        when(cartService.getUserCart()).thenReturn(Mono.just(testCart));
        when(itemFacade.findById(testItemId)).thenReturn(Mono.just(testItem));
        when(cartService.getCartItem(testItemId, testCartId)).thenReturn(Mono.empty());
        when(itemMapper.toDto(testItem)).thenReturn(ItemDto.builder().id(testItemId).title("mapped").build());

        StepVerifier.create(itemService.getItem(testItemId))
                .expectNextMatches(dto ->
                        dto.getId().equals(testItemId) &&
                                dto.getCount() == 0
                )
                .verifyComplete();
    }
}
