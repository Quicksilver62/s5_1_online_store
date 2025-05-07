package ru.yandex.practicum.showcase_service.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.showcase_service.AbstractTestContainerTest;
import ru.yandex.practicum.showcase_service.facades.ItemFacade;
import ru.yandex.practicum.showcase_service.model.Item;
import ru.yandex.practicum.showcase_service.repository.ItemRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemFacadeTest extends AbstractTestContainerTest {

    @MockitoBean
    private ItemRepository itemRepository;

    @Autowired
    private ItemFacade itemFacade;

    @BeforeEach
    void setUp() {
        itemFacade.clearCache();
    }

    @Test
    void findById_shouldReturnItemAndCacheIt() {
        Item expectedItem = Item.builder().id(1).title("Test Item").price(100.0).build();
        when(itemRepository.findById(1)).thenReturn(Mono.just(expectedItem));

        Item result1 = itemFacade.findById(1).block();

        when(itemRepository.findById(1)).thenReturn(Mono.empty());

        itemFacade.findById(1).block();

        assertNotNull(result1);
        assertEquals(expectedItem, result1);

        verify(itemRepository, times(1)).findById(1);
    }

    @Test
    void getItemsSlice_shouldReturnPaginatedResults() {
        Item item1 = Item.builder().id(1).title("Item 1").price(100.0).build();
        Item item2 = Item.builder().id(2).title("Item 2").price(200.0).build();

        Pageable pageable = PageRequest.of(0, 2);
        when(itemRepository.findAllBy(pageable)).thenReturn(Flux.just(item1, item2));
        when(itemRepository.count()).thenReturn(Mono.just(10L));

        Slice<Item> result = itemFacade.getItemsSlice(pageable).block();

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().contains(item1));
        assertTrue(result.getContent().contains(item2));
    }

    @Test
    void findById_shouldReturnNullForNonExistingItem() {
        when(itemRepository.findById(999)).thenReturn(Mono.empty());
        Item result = itemFacade.findById(999).block();
        assertNull(result);
    }
}
