package ru.yandex.practicum.s5_1_online_store.services;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.s5_1_online_store.dto.ItemDto;
import ru.yandex.practicum.s5_1_online_store.dto.OrderDto;
import ru.yandex.practicum.s5_1_online_store.helpers.Helper;
import ru.yandex.practicum.s5_1_online_store.mappers.ItemMapper;
import ru.yandex.practicum.s5_1_online_store.mappers.OrderMapper;
import ru.yandex.practicum.s5_1_online_store.model.Order;
import ru.yandex.practicum.s5_1_online_store.model.User;
import ru.yandex.practicum.s5_1_online_store.repository.OrderRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final UserService userService;
    private final ItemMapper itemMapper;
    private final OrderMapper orderMapper;

    public List<OrderDto> getOrders(HttpServletRequest request) {
        UUID userId = UUID.fromString(Helper.getUserIdFromCookie(request));
        User user = userService.getUser(userId);
        List<Order> orders = repository.findAllByUser(user);
        return orders.stream()
                .map(this::getOrderDto)
                .toList();
    }

    public OrderDto getOrder(Integer orderId, HttpServletRequest request) {
        UUID userId = UUID.fromString(Helper.getUserIdFromCookie(request));
        User user = userService.getUser(userId);
        Order order = repository.findByUser(user).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        return getOrderDto(order);
    }

    private OrderDto getOrderDto(Order order) {
        var orderDto = orderMapper.toDto(order);
        Set<ItemDto> itemDtos = new HashSet<>();
        order.getOrderItems().forEach(orderItem -> {
            var itemDto = itemMapper.toDto(orderItem.getItem());
            itemDtos.add(itemDto);
        });
        orderDto.setItems(itemDtos);
        return orderDto;
    }
}
