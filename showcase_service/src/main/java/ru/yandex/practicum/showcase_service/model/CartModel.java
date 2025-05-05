package ru.yandex.practicum.showcase_service.model;

import ru.yandex.practicum.showcase_service.dto.ItemDto;

import java.util.List;

public record CartModel(List<ItemDto> items, double total, boolean isAvailable) {
}
