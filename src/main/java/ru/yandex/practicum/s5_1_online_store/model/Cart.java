package ru.yandex.practicum.s5_1_online_store.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(schema = "store", name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "cart_id")
    private Set<CartItem> cartItems = new HashSet<>();

    public void addItem(Item item, int count) {
        CartItem cartItem = new CartItem();
        cartItem.setItem(item);
        cartItem.setCount(count);
        cartItems.add(cartItem);
    }
}
