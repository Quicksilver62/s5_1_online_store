package ru.yandex.practicum.s5_1_online_store.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(schema = "store", name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CartItem> cartItems = new HashSet<>();

    public void addItem(Item item, int count) {
        CartItem cartItem = new CartItem();
        cartItem.setCart(this);
        cartItem.setItem(item);
        cartItem.setCount(count);
        cartItems.add(cartItem);
    }

    public int getItemCount(Item item) {
        return cartItems.stream()
                .filter(ci -> ci.getItem().equals(item))
                .findFirst()
                .map(CartItem::getCount)
                .orElse(0);
    }
}
