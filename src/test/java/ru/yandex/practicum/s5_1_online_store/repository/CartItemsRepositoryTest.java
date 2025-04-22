package ru.yandex.practicum.s5_1_online_store.repository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.s5_1_online_store.model.CartItem;
import ru.yandex.practicum.s5_1_online_store.model.CartItemWithItem;
import ru.yandex.practicum.s5_1_online_store.model.Item;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CartItemsRepositoryTest extends AbstractTestContainerTest{

    @Autowired
    private CartItemsRepository cartItemsRepository;

    @Autowired
    private ItemRepository itemRepository;

    @BeforeAll
    static void setup(@Autowired CartItemsRepository repository) {
        repository.deleteAll().block();
    }

    @Test
    void findById_ItemIdAndId_CartId_shouldReturnCartItemWhenExists() {
        Integer cartId = 1;
        Integer itemId = 1;
        CartItem cartItem = CartItem.builder()
                .cartId(cartId)
                .itemId(itemId)
                .count(2)
                .item(Item.builder().id(1).title("Item 1").price(100.0).build())
                .build();

        cartItemsRepository.save(cartItem).block();
        CartItem foundItem = cartItemsRepository.findByItemIdAndCartId(itemId, cartId).block();

        assertNotNull(foundItem);
        assertEquals(2, foundItem.getCount());
    }

    @Test
    void findByCartIdWithItem_shouldReturnCartItemsWithItems() {
        Integer cartId = 2;

        Item item1 = Item.builder().title("Item 1").description("descr 1").imgPath("img 1").price(100.0).build();
        Item item2 = Item.builder().title("Item 2").description("descr 2").imgPath("img 2").price(200.0).build();

        Item savedItem1 = itemRepository.save(item1).block();
        Item savedItem2 = itemRepository.save(item2).block();

        CartItem cartItem1 = CartItem.builder()
                .cartId(cartId)
                .itemId(savedItem1.getId())
                .count(1)
                .build();
        CartItem cartItem2 = CartItem.builder()
                .cartId(cartId)
                .itemId(savedItem2.getId())
                .count(2)
                .build();

        cartItemsRepository.saveAll(List.of(cartItem1, cartItem2)).blockLast();

        List<CartItemWithItem> items = cartItemsRepository.findByCartIdWithItem(cartId).collectList().block();

        assertNotNull(items);
        assertEquals(2, items.size());
        assertTrue(items.stream().allMatch(ci -> ci.getCartId().equals(cartId)));
    }

    @Test
    void deleteAllById_CartId_shouldDeleteAllItemsForCart() {
        Integer cartId = 3;
        CartItem cartItem1 = CartItem.builder()
                .cartId(cartId)
                .itemId(1)
                .count(1)
                .item(null)
                .build();
        CartItem cartItem2 = CartItem.builder()
                .cartId(cartId)
                .itemId(2)
                .count(1)
                .item(null)
                .build();

        cartItemsRepository.saveAll(List.of(cartItem1, cartItem2)).blockLast();
        cartItemsRepository.deleteAllByCartId(cartId).block();
        CartItem deletedItem = cartItemsRepository.findByItemIdAndCartId(1, cartId).block();

        assertNull(deletedItem);
    }

    @Test
    void findByCartIdWithItem_shouldReturnEmptyForNonExistingCart() {
        List<CartItemWithItem> items = cartItemsRepository.findByCartIdWithItem(999).collectList().block();

        assertNotNull(items);
        assertTrue(items.isEmpty());
    }
}
