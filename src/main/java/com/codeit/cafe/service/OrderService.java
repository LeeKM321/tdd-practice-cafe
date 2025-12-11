package com.codeit.cafe.service;

import com.codeit.cafe.domain.Menu;
import com.codeit.cafe.domain.Order;
import com.codeit.cafe.domain.OrderItem;
import com.codeit.cafe.dto.OrderCreateRequest;
import com.codeit.cafe.dto.OrderItemRequest;
import com.codeit.cafe.dto.OrderResponse;
import com.codeit.cafe.repository.MenuRepository;
import com.codeit.cafe.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;

    public OrderResponse createOrder(OrderCreateRequest request) {
        // 1. 주문 생성
        Order order = Order.builder()
                .customerName(request.getCustomerName())
                .build();

        // 2. 주문 항목 추가
        for (OrderItemRequest itemRequest : request.getOrderItems()) {
            Menu menu = getAvailableMenu(itemRequest.getMenuId());
            order.addOrderItem(menu, itemRequest.getQuantity());
        }

        // 3. 주문 저장
        Order saved = orderRepository.save(order);

        return OrderResponse.from(saved);
    }

    private Menu getAvailableMenu(Long menuId) {
        Menu menu = menuRepository.findById(menuId)
                // 커스텀 예외 클래스 사용하면 더 좋습니다.
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴입니다." + menuId));
        if (!menu.getAvailable()) {
            throw new IllegalStateException("주문할 수 없는 메뉴입니다." + menu.getName());
        }
        return menu;
    }
}










