package ru.yandex.practicum.s5_1_online_store.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.yandex.practicum.s5_1_online_store.dto.OrderDto;
import ru.yandex.practicum.s5_1_online_store.model.Order;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

//    @Mapping(target = "items", ignore = true)
    OrderDto toDto(Order order);
}
