package ru.yandex.practicum.s5_1_online_store.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.s5_1_online_store.dto.ItemDto;
import ru.yandex.practicum.s5_1_online_store.dto.OrderDto;
import ru.yandex.practicum.s5_1_online_store.helpers.Helper;
import ru.yandex.practicum.s5_1_online_store.mappers.ItemMapper;
import ru.yandex.practicum.s5_1_online_store.model.CartItem;
import ru.yandex.practicum.s5_1_online_store.model.Order;
import ru.yandex.practicum.s5_1_online_store.model.OrderItem;
import ru.yandex.practicum.s5_1_online_store.model.OrderItemId;
import ru.yandex.practicum.s5_1_online_store.repository.OrderItemsRepository;
import ru.yandex.practicum.s5_1_online_store.repository.OrderRepository;

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
                                                new OrderItemId(ci.getId().getItemId(), savedOrder.getId()),
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
                .map(orderItems -> {
                    OrderDto orderDto = new OrderDto();
                    orderDto.setId(order.getId());
                    orderDto.setTotalSum(order.getTotalSum());
                    Set<ItemDto> itemDtos = new HashSet<>();
                    orderItems.forEach(orderItem -> {
                        var itemDto = itemMapper.toDto(orderItem.getItem());
                        itemDtos.add(itemDto);
                    });
                    orderDto.setItems(itemDtos);
                    return orderDto;
                });
    }
}
