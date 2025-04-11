package ru.yandex.practicum.s5_1_online_store.module;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.s5_1_online_store.dto.ItemDto;
import ru.yandex.practicum.s5_1_online_store.dto.OrderDto;
import ru.yandex.practicum.s5_1_online_store.mappers.ItemMapper;
import ru.yandex.practicum.s5_1_online_store.mappers.OrderMapper;
import ru.yandex.practicum.s5_1_online_store.model.*;
import ru.yandex.practicum.s5_1_online_store.repository.OrderRepository;
import ru.yandex.practicum.s5_1_online_store.services.OrderService;
import ru.yandex.practicum.s5_1_online_store.services.UserService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserService userService;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private OrderService orderService;

    private final UUID testUserId = UUID.randomUUID();
    private final User testUser = new User();
    private final Order testOrder = Order.builder().id(1).build();
    private final OrderDto testOrderDto = new OrderDto();

    @BeforeEach
    void setUp() {
        testOrderDto.setId(1);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("user_id", testUserId.toString())});
        when(userService.getUser(testUserId)).thenReturn(testUser);
    }

    @Test
    void getOrders_ShouldReturnOrderDtos() {
        Order order1 = Order.builder().id(1).build();
        Order order2 = Order.builder().id(2).build();

        when(orderRepository.findAllByUser(testUser)).thenReturn(List.of(order1, order2));
        when(orderMapper.toDto(order1)).thenReturn(new OrderDto(1, Set.of(), 100.0));
        when(orderMapper.toDto(order2)).thenReturn(new OrderDto(2, Set.of(), 200.0));

        List<OrderDto> result = orderService.getOrders(request);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(2, result.get(1).getId());
        verify(orderMapper, times(2)).toDto(any());
    }

    @Test
    void getOrder_ShouldReturnOrderDto() {
        Item item = new Item(1, "Test Item", "Desc 1", "/img1.jpg", 100.0, new HashSet<>(), new HashSet<>());
        OrderItem orderItem = new OrderItem(new OrderItemId(1, 1), item, 1);
        testOrder.setOrderItems(Set.of(orderItem));

        when(orderRepository.findByUser(testUser)).thenReturn(Optional.of(testOrder));
        when(orderMapper.toDto(testOrder)).thenReturn(testOrderDto);

        ItemDto itemDto = new ItemDto(1, "Test Item", "Desc 1", "/img1.jpg", 100.0,  3);
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        OrderDto result = orderService.getOrder(1, request);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(1, result.getItems().size());
        verify(itemMapper).toDto(item);
    }

    @Test
    void getOrder_WhenOrderNotFound_ShouldThrowException() {
        when(orderRepository.findByUser(testUser)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            orderService.getOrder(1, request);
        });
    }
}
