package ru.yandex.practicum.showcase_service.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.showcase_service.dto.ItemDto;
import ru.yandex.practicum.showcase_service.dto.OrderDto;
import ru.yandex.practicum.showcase_service.helpers.Helper;
import ru.yandex.practicum.showcase_service.mappers.ItemMapper;
import ru.yandex.practicum.showcase_service.model.*;
import ru.yandex.practicum.showcase_service.model.CartItem;
import ru.yandex.practicum.showcase_service.model.Order;
import ru.yandex.practicum.showcase_service.model.OrderItem;
import ru.yandex.practicum.showcase_service.repository.OrderItemsRepository;
import ru.yandex.practicum.showcase_service.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final ItemMapper itemMapper;

    public Flux<OrderDto> getOrders(ServerHttpRequest request) {
        return Helper.getUserIdFromCookie(request)
                .flatMapMany(userId -> {
                    UUID uuid = UUID.fromString(userId);
                    return orderRepository.findAllByUserId(uuid)
                            .flatMap(this::getOrderDto);
                });
    }

    public Mono<OrderDto> getOrder(Integer orderId, ServerHttpRequest request) {
        return Helper.getUserIdFromCookie(request)
                .flatMap(userId -> {
                    UUID uuid = UUID.fromString(userId);
                    return orderRepository.findByIdAndUserId(orderId, uuid);
                })
                .flatMap(this::getOrderDto);
    }

    public Mono<Order> saveOrder(UUID userId, Double totalSum, List<CartItem> cartItems) {
        return orderRepository.save(Order.builder()
                        .userId(userId)
                        .totalSum(totalSum)
                        .createdAt(LocalDateTime.now())
                        .build())
                .flatMap(savedOrder ->
                        orderItemsRepository.saveAll(
                                cartItems.stream()
                                        .map(ci -> new OrderItem(
                                                ci.getItemId(),
                                                savedOrder.getId(),
                                                ci.getCount(),
                                                ci.getItem()
                                        ))
                                        .toList()
                                )
                                .then(Mono.just(savedOrder))
                );
    }

    private Mono<OrderDto> getOrderDto(Order order) {
        return orderItemsRepository.findByOrderIdWithItem(order.getId())
                .collectList()
                .map(orderItemWithItems -> {
                    OrderDto orderDto = new OrderDto();
                    orderDto.setId(order.getId());
                    orderDto.setTotalSum(order.getTotalSum());
                    Set<ItemDto> itemDtos = new HashSet<>();
                    var orderItems = orderItemWithItems.stream()
                            .map(orderItemWithItem -> OrderItem.builder()
                                    .orderId(orderItemWithItem.getOrderId())
                                    .itemId(orderItemWithItem.getItemId())
                                    .count(orderItemWithItem.getCount())
                                    .build())
                            .toList();
                    orderItems.forEach(orderItem -> {
                        var itemDto = itemMapper.toDto(orderItem.getItem());
                        itemDtos.add(itemDto);
                    });
                    orderDto.setItems(itemDtos);
                    return orderDto;
                });
    }
}
