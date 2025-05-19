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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.showcase_service.dto.ItemDto;
import ru.yandex.practicum.showcase_service.helpers.Helper;
import ru.yandex.practicum.showcase_service.mappers.ItemMapper;
import ru.yandex.practicum.showcase_service.model.*;
import ru.yandex.practicum.showcase_service.repository.OrderItemsRepository;
import ru.yandex.practicum.showcase_service.repository.OrderRepository;
import ru.yandex.practicum.showcase_service.services.OrderService;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemsRepository orderItemsRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private OrderService orderService;

    private final UUID testUserId = UUID.randomUUID();
    private final Integer testOrderId = 1;
    private final Integer testItemId = 100;
    private final Order testOrder = Order.builder()
            .id(testOrderId)
            .userId(testUserId)
            .totalSum(100.0)
            .createdAt(LocalDateTime.now())
            .build();
    private final CartItem testCartItem = CartItem.builder()
            .itemId(testItemId)
            .count(2)
            .item(Item.builder().id(testItemId).title("Test Item").price(50.0).build())
            .build();
    private final OrderItem testOrderItem = OrderItem.builder()
            .orderId(testOrderId)
            .itemId(testItemId)
            .count(2)
            .item(testCartItem.getItem())
            .build();
    private final ItemDto testItemDto = new ItemDto(testItemId, "Test Item", "Description", "img.jpg", 50.0, 2);

    private MockedStatic<Helper> helperMock;

    @BeforeEach
    void setUp() {
        helperMock = Mockito.mockStatic(Helper.class);
        helperMock.when(Helper::getCurrentUserId).thenReturn(Mono.just(testUserId));
    }

    @AfterEach
    void tearDown() {
        helperMock.close();
    }


    @Test
    void getOrders_shouldReturnOrders() {
        OrderItemWithItem orderItemWithItem = new OrderItemWithItem(
                testOrderId, testItemId, 2,
                "Test Item", "Description", "img.jpg", 50.0
        );

        when(orderRepository.findAllByUserId(testUserId)).thenReturn(Flux.just(testOrder));
        when(orderItemsRepository.findByOrderIdWithItem(testOrderId))
                .thenReturn(Flux.just(orderItemWithItem));
        when(itemMapper.toDto(any())).thenReturn(testItemDto);

        StepVerifier.create(orderService.getOrders())
                .expectNextMatches(orderDto ->
                        orderDto.getId().equals(testOrderId) &&
                                orderDto.getTotalSum().equals(100.0) &&
                                orderDto.getItems().size() == 1
                )
                .verifyComplete();
    }

    @Test
    void getOrder_shouldReturnOrder() {
        OrderItemWithItem orderItemWithItem = new OrderItemWithItem(
                testOrderId, testItemId, 2,
                "Test Item", "Description", "img.jpg", 50.0
        );

        when(orderRepository.findByIdAndUserId(testOrderId, testUserId))
                .thenReturn(Mono.just(testOrder));
        when(orderItemsRepository.findByOrderIdWithItem(testOrderId))
                .thenReturn(Flux.just(orderItemWithItem));
        when(itemMapper.toDto(any())).thenReturn(testItemDto);

        StepVerifier.create(orderService.getOrder(testOrderId))
                .expectNextMatches(orderDto ->
                        orderDto.getId().equals(testOrderId) &&
                                orderDto.getTotalSum().equals(100.0) &&
                                orderDto.getItems().size() == 1
                )
                .verifyComplete();
    }

    @Test
    void getOrder_notFound_shouldReturnEmpty() {
        when(orderRepository.findByIdAndUserId(testOrderId, testUserId))
                .thenReturn(Mono.empty());

        StepVerifier.create(orderService.getOrder(testOrderId))
                .expectError(NoSuchElementException.class)
                .verify();
    }
}
