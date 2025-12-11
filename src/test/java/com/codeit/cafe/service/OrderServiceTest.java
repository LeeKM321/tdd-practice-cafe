package com.codeit.cafe.service;

import com.codeit.cafe.domain.OrderStatus;
import com.codeit.cafe.dto.OrderCreateRequest;
import com.codeit.cafe.dto.OrderItemRequest;
import com.codeit.cafe.dto.OrderResponse;
import com.codeit.cafe.repository.MenuRepository;
import com.codeit.cafe.domain.Menu;
import com.codeit.cafe.domain.Order;
import com.codeit.cafe.repository.OrderRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 테스트")
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private OrderService orderService;

    private Menu americano;
    private Menu latte;

    @BeforeEach
    void setUp() {
        americano = Menu.builder()
                .name("아메리카노")
                .price(4000)
                .available(true)
                .build();

        latte = Menu.builder()
                .name("라떼")
                .price(4500)
                .available(true)
                .build();
    }

    @Nested
    @DisplayName("주문 생성")
    class CreateOrder {

        @Test
        @DisplayName("성공: 단일 메뉴를 주문할 수 있다.")
        void createOrder_WithSingleMenu_Success() {
            // given
            when(menuRepository.findById(1L))
                    .thenReturn(Optional.of(americano));

            // orderRepository가 주문을 save하면 save한 내용 그대로 반환하는 설정
            when(orderRepository.save(any(Order.class)))
                    .thenAnswer(invocation -> {
                        Order order = invocation.getArgument(0);
                        return order;
                    });

            // 주문 요청 DTO 생성
            OrderItemRequest itemRequest = OrderItemRequest.builder()
                    .menuId(1L)
                    .quantity(2)
                    .build();

            OrderCreateRequest request = OrderCreateRequest.builder()
                    .customerName("김춘식")
                    .orderItems(List.of(itemRequest))
                    .build();


            // when
            OrderResponse response = orderService.createOrder(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getOrderItems()).hasSize(1);
            assertThat(response.getCustomerName()).isEqualTo("김춘식");
            assertThat(response.getTotalPrice()).isEqualTo(8000);
            assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);

            verify(menuRepository, times(1)).findById(1L);
            verify(orderRepository, times(1)).save(any(Order.class));

        }


    }

}














