package ru.yandex.practicum.s5_1_online_store.module;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.s5_1_online_store.dto.ItemDto;
import ru.yandex.practicum.s5_1_online_store.dto.OrderDto;
import ru.yandex.practicum.s5_1_online_store.mappers.ItemMapper;
import ru.yandex.practicum.s5_1_online_store.model.*;
import ru.yandex.practicum.s5_1_online_store.repository.OrderItemsRepository;
import ru.yandex.practicum.s5_1_online_store.repository.OrderRepository;
import ru.yandex.practicum.s5_1_online_store.services.OrderService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemsRepository orderItemsRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private ServerHttpRequest request;

    @InjectMocks
    private OrderService orderService;

    @Test
    void getOrders_WithValidUserId_ReturnsOrders() {
        String userId = UUID.randomUUID().toString();
        UUID uuid = UUID.fromString(userId);

        HttpCookie userIdCookie = new HttpCookie("user_id", userId);
        LinkedMultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
        cookies.add("user_id", userIdCookie);
        when(request.getCookies()).thenReturn(cookies);

        Order order1 = Order.builder().id(1).userId(uuid).totalSum(100.0).build();
        Order order2 = Order.builder().id(2).userId(uuid).totalSum(200.0).build();
        when(orderRepository.findAllByUserId(uuid)).thenReturn(Flux.just(order1, order2));

        mockOrderDtoConversion(order1, List.of(1));
        mockOrderDtoConversion(order2, List.of(2));

        List<OrderDto> result = orderService.getOrders(request).collectList().block();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(100.0, result.get(0).getTotalSum());
        assertEquals(200.0, result.get(1).getTotalSum());
    }

    @Test
    void getOrder_WithInvalidOrderId_ReturnsEmpty() {
        String userId = UUID.randomUUID().toString();
        UUID uuid = UUID.fromString(userId);
        Integer orderId = 999;

        HttpCookie userIdCookie = new HttpCookie("user_id", userId);
        LinkedMultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
        cookies.add("user_id", userIdCookie);
        when(request.getCookies()).thenReturn(cookies);

        when(orderRepository.findByIdAndUserId(orderId, uuid)).thenReturn(Mono.empty());

        OrderDto result = orderService.getOrder(orderId, request).block();

        assertNull(result);
    }

    @Test
    void saveOrder_WithValidData_SavesOrderAndItems() {
        UUID userId = UUID.randomUUID();
        double totalSum = 300.0;

        Item item1 = Item.builder().id(1).price(100.0).build();
        Item item2 = Item.builder().id(2).price(200.0).build();
        CartItem cartItem1 = CartItem.builder()
                .cartId(1)
                .itemId(1)
                .count(1)
                .item(item1)
                .build();
        CartItem cartItem2 = CartItem.builder()
                .cartId(2)
                .itemId(1)
                .count(1)
                .item(item2)
                .build();
        List<CartItem> cartItems = List.of(cartItem1, cartItem2);

        Order savedOrder = Order.builder()
                .id(1)
                .userId(userId)
                .totalSum(totalSum)
                .createdAt(LocalDateTime.now())
                .build();
        when(orderRepository.save(any())).thenReturn(Mono.just(savedOrder));

        when(orderItemsRepository.saveAll(anyList())).thenReturn(Flux.empty());

        Order result = orderService.saveOrder(userId, totalSum, cartItems).block();

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(totalSum, result.getTotalSum());

        verify(orderRepository).save(any());
        verify(orderItemsRepository).saveAll(anyList());
    }

    @Test
    void getOrders_WithMissingUserIdCookie_ThrowsException() {
        when(request.getCookies()).thenReturn(new LinkedMultiValueMap<>());

        assertThrows(IllegalArgumentException.class, () -> {
            orderService.getOrders(request).blockFirst();
        });
    }

    private void mockOrderDtoConversion(Order order, List<Integer> itemIds) {
        List<OrderItemWithItem> projections = itemIds.stream()
                .map(id -> {
                    OrderItemWithItem projection = mock(OrderItemWithItem.class);
                    when(projection.getOrderId()).thenReturn(order.getId());
                    when(projection.getItemId()).thenReturn(id);
                    when(projection.getCount()).thenReturn(1);
                    return projection;
                })
                .collect(Collectors.toList());

        when(orderItemsRepository.findByOrderIdWithItem(order.getId()))
                .thenReturn(Flux.fromIterable(projections));

        itemIds.forEach(id -> {
            ItemDto itemDto = ItemDto.builder().id(id).price(100.0).build();
            lenient().when(itemMapper.toDto(any())).thenReturn(itemDto);
        });
    }
}
