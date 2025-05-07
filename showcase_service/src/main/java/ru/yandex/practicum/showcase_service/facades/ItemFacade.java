package ru.yandex.practicum.showcase_service.facades;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.showcase_service.dto.ItemDto;
import ru.yandex.practicum.showcase_service.mappers.ItemMapper;
import ru.yandex.practicum.showcase_service.model.Item;
import ru.yandex.practicum.showcase_service.repository.ItemRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemFacade {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final CacheManager cacheManager;

    @Cacheable(
            value = "item",
            key = "#id"
    )
    public Mono<Item> findById(Integer id) {
        return itemRepository.findById(id);
    }

    @Cacheable(
            value = "itemsSlice",
            key = "{#pageable.pageNumber, #pageable.pageSize}"
    )
    public Mono<Slice<Item>> getItemsSlice(Pageable pageable) {
        return itemRepository.findAllBy(pageable)
                .collectList()
                .zipWith(itemRepository.count())
                .map(tuple -> new SliceImpl<>(
                        tuple.getT1(),
                        pageable,
                        (pageable.getOffset() + pageable.getPageSize()) < tuple.getT2()
                ));
    }

    @CacheEvict(value = "itemsSlice", allEntries = true)
    public Mono<Void> saveAll(Flux<ItemDto> itemDtoFlux) {
        return itemDtoFlux
                .map(itemMapper::fromDto)
                .flatMap(item ->
                    itemRepository.save(item)
                            .doOnSuccess(savedItem -> {
                                Cache itemCache = cacheManager.getCache("item");
                                if (itemCache != null) {
                                    itemCache.evict(savedItem.getId());
                                    itemCache.put(savedItem.getId(), Mono.just(savedItem));
                                }
                            }))
                .then()
                .onErrorResume(Mono::error);
    }

    public Mono<Void> clearCache() {
        return Mono.fromRunnable(() -> {
            Optional.ofNullable(cacheManager.getCache("item"))
                    .ifPresent(Cache::clear);
            Optional.ofNullable(cacheManager.getCache("itemsSlice"))
                    .ifPresent(Cache::clear);
        });
    }
}
