package ru.yandex.practicum.s5_1_online_store.model;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@Table(schema = "store", name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItems> orderItems = new HashSet<>();

    @Column(name = "total_sum")
    private Integer totalSum;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    public int getItemCount(Item item) {
        return orderItems.stream()
                .filter(oi -> oi.getItem().equals(item))
                .findFirst()
                .map(OrderItems::getCount)
                .orElse(0);
    }
}
