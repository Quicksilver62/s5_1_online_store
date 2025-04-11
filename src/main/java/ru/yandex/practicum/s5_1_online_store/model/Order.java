package ru.yandex.practicum.s5_1_online_store.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(schema = "store", name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItem> orderItems = new HashSet<>();

    @Column(name = "total_sum")
    private Double totalSum;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    public void addItem(Item item, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(this);
        orderItem.setItem(item);
        orderItem.setCount(count);
        orderItems.add(orderItem);
    }
}
