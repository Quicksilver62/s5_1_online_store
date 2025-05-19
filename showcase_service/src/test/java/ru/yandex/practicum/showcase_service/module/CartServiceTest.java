package ru.yandex.practicum.showcase_service.module;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.client.api.DefaultApi;
import ru.yandex.practicum.showcase_service.model.*;
import ru.yandex.practicum.showcase_service.repository.CartItemsRepository;
import ru.yandex.practicum.showcase_service.repository.CartRepository;
import ru.yandex.practicum.showcase_service.repository.ItemRepository;
import ru.yandex.practicum.showcase_service.services.CartService;
import ru.yandex.practicum.showcase_service.services.OrderService;

import java.util.*;

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
    private DefaultApi paymentApiClient;

    @InjectMocks
    private CartService cartService;

    private final UUID testUserId = UUID.randomUUID();
    private final Integer testCartId = 1;
    private final Integer testItemId = 100;
    private final Cart testCart = Cart.builder().id(testCartId).userId(testUserId).build();
    private final CartItem testCartItem = CartItem.builder().cartId(testCartId).itemId(testItemId).count(2).build();
    private final Item testItem = Item.builder().id(testItemId).title("Test Item").price(10.0).build();

    private SecurityContext securityContext;
    private MockedStatic<ReactiveSecurityContextHolder> reactiveContextMock;

    @BeforeEach
    void setUp() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(testUserId.toString());
        when(authentication.isAuthenticated()).thenReturn(true);

        securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        reactiveContextMock = Mockito.mockStatic(ReactiveSecurityContextHolder.class);
        reactiveContextMock.when(ReactiveSecurityContextHolder::getContext)
                .thenReturn(Mono.just(securityContext));
    }

    @AfterEach
    void tearDown() {
        reactiveContextMock.close();
    }

    @Test
    void getUserCart_shouldReturnExistingCart1() {
        when(cartRepository.findByUserId(testUserId)).thenReturn(Mono.just(testCart));

        StepVerifier.create(cartService.getUserCart())
                .expectNextMatches(cart ->
                        cart.getId().equals(testCartId) &&
                                cart.getUserId().equals(testUserId))
                .verifyComplete();
    }


    @Test
    void getUserCart_shouldReturnExistingCart() {
        when(cartRepository.findByUserId(testUserId)).thenReturn(Mono.just(testCart));

        StepVerifier.create(cartService.getUserCart())
                .expectNext(testCart)
                .verifyComplete();
    }

    @Test
    void getUserCart_shouldCreateNewCartIfNotExists() {
        when(cartRepository.findByUserId(testUserId)).thenReturn(Mono.empty());
        when(cartRepository.save(any())).thenReturn(Mono.just(testCart));

        StepVerifier.create(cartService.getUserCart())
                .expectNextMatches(cart ->
                        cart.getUserId().equals(testUserId))
                .verifyComplete();
    }

    @Test
    void handleItemAction_plusNewItem_shouldAddNewItem() {
        when(cartRepository.findByUserId(testUserId)).thenReturn(Mono.just(testCart));
        when(cartItemsRepository.findByItemIdAndCartId(testItemId, testCartId))
                .thenReturn(Mono.empty());
        when(itemRepository.findById(testItemId)).thenReturn(Mono.just(testItem));
        when(cartItemsRepository.save(any())).thenReturn(Mono.just(testCartItem));

        StepVerifier.create(cartService.handleItemAction("plus", testItemId))
                .verifyComplete();

        verify(cartItemsRepository).save(argThat(item ->
                item.getCount() == 1 &&
                        item.getItemId().equals(testItemId)));
    }

    @Test
    void handleItemAction_minus_shouldDecrementCount() {
        when(cartRepository.findByUserId(testUserId)).thenReturn(Mono.just(testCart));
        when(cartItemsRepository.findByItemIdAndCartId(testItemId, testCartId))
                .thenReturn(Mono.just(testCartItem));
        when(cartItemsRepository.save(any())).thenReturn(Mono.just(testCartItem));

        StepVerifier.create(cartService.handleItemAction("minus", testItemId))
                .verifyComplete();

        verify(cartItemsRepository).save(argThat(item -> item.getCount() == 1));
    }

    @Test
    void handleItemAction_delete_shouldRemoveItem() {
        when(cartRepository.findByUserId(testUserId)).thenReturn(Mono.just(testCart));
        when(cartItemsRepository.findByItemIdAndCartId(testItemId, testCartId))
                .thenReturn(Mono.just(testCartItem));
        when(cartItemsRepository.delete(any())).thenReturn(Mono.empty());

        StepVerifier.create(cartService.handleItemAction("delete", testItemId))
                .verifyComplete();

        verify(cartItemsRepository).delete(testCartItem);
    }

    @Test
    void buy_shouldProcessPaymentAndCreateOrder() {
        CartItemWithItem cartItemWithItem = new CartItemWithItem(
                testItemId, testCartId, 2,
                "Test Item", "Desc", "img.jpg", 10.0
        );

        when(cartRepository.findByUserId(testUserId)).thenReturn(Mono.just(testCart));
        when(cartItemsRepository.findByCartIdWithItem(testCartId))
                .thenReturn(Flux.just(cartItemWithItem));
        when(paymentApiClient.apiPurchasePost(any())).thenReturn(Mono.empty());
        when(orderService.saveOrder(any(), any(), any())).thenReturn(Mono.empty());
        when(cartItemsRepository.deleteAllByCartId(testCartId)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.buy())
                .verifyComplete();

        verify(paymentApiClient).apiPurchasePost(argThat(req ->
                req.getAmount() == 20.0));
        verify(orderService).saveOrder(
                eq(testUserId),
                eq(20.0),
                argThat(items -> items.size() == 1));
    }

    @Test
    void buy_emptyCart_shouldThrowException() {
        when(cartRepository.findByUserId(testUserId)).thenReturn(Mono.just(testCart));
        when(cartItemsRepository.findByCartIdWithItem(testCartId))
                .thenReturn(Flux.empty());

        StepVerifier.create(cartService.buy())
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
