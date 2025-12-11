package com.codeit.cafe.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 주문 엔티티
 */
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false)
    private Integer totalPrice;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    @Builder
    public Order(String customerName) {
        this.customerName = customerName;
        this.status = OrderStatus.PENDING;
        this.totalPrice = 0;
        this.orderedAt = LocalDateTime.now();
    }

    public void addOrderItem(Menu menu, int quantity) {
        OrderItem orderItem = OrderItem.builder()
                .order(this)
                .menu(menu)
                .menuName(menu.getName())
                .menuPrice(menu.getPrice())
                .quantity(quantity)
                .build();

        this.orderItems.add(orderItem);
        calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        // 총 금액 계산
        this.totalPrice = orderItems.stream()
                .mapToInt(item -> item.getMenuPrice() * item.getQuantity())
                .sum();
    }

}













