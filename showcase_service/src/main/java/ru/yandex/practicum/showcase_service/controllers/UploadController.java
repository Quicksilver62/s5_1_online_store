package ru.yandex.practicum.showcase_service.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.showcase_service.dto.ItemDto;
import ru.yandex.practicum.showcase_service.facades.ItemFacade;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final ItemFacade itemFacade;

    @PostMapping(consumes = MediaType.APPLICATION_NDJSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> upload(@Valid @RequestBody Flux<ItemDto> itemDtoFlux) {
        return itemFacade.saveAll(itemDtoFlux);
    }
}
