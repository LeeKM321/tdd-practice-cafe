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

        @Test
        @DisplayName("성공: 여러 메뉴를 주문할 수 있다.")
        void createOrder_WithMultipleMenus_Success() {
            // given
            when(menuRepository.findById(1L))
                    .thenReturn(Optional.of(americano));
            when(menuRepository.findById(2L))
                    .thenReturn(Optional.of(latte));

            // orderRepository가 주문을 save하면 save한 내용 그대로 반환하는 설정
            when(orderRepository.save(any(Order.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // 주문 요청 DTO 생성
            OrderItemRequest itemRequest1 = OrderItemRequest.builder()
                    .menuId(1L)
                    .quantity(1)
                    .build();
            OrderItemRequest itemRequest2 = OrderItemRequest.builder()
                    .menuId(2L)
                    .quantity(2)
                    .build();

            OrderCreateRequest request = OrderCreateRequest.builder()
                    .customerName("김춘식")
                    .orderItems(List.of(itemRequest1, itemRequest2))
                    .build();


            // when
            OrderResponse response = orderService.createOrder(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getOrderItems()).hasSize(2);
            assertThat(response.getTotalPrice()).isEqualTo(13000);
            assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);

            verify(menuRepository, times(1)).findById(1L);
            verify(orderRepository, times(1)).save(any(Order.class));

        }

        @Test
        @DisplayName("실패: 존재하지 않는 메뉴라면 주문이 실패해야 한다.")
        void createOrder_WithNonExistentMenu_ThrowsException() {
            // given
            when(menuRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // 주문 요청 DTO 생성
            OrderItemRequest itemRequest = OrderItemRequest.builder()
                    .menuId(999L)
                    .quantity(2)
                    .build();

            OrderCreateRequest request = OrderCreateRequest.builder()
                    .customerName("김춘식")
                    .orderItems(List.of(itemRequest))
                    .build();


            // when & then
            assertThatThrownBy(() -> orderService.createOrder(request))
                    .isInstanceOf(IllegalArgumentException.class)
                            .hasMessageContaining("존재하지 않는 메뉴");

            verify(orderRepository, never()).save(any(Order.class));

        }

        @Test
        @DisplayName("실패: 판매 불가능한 메뉴")
        void createOrder_WithUnavailableMenu_ThrowsException() {
            // Given
            Menu unavailableMenu = Menu.builder()
                    .name("품절 메뉴")
                    .price(5000)
                    .available(false)
                    .build();

            when(menuRepository.findById(1L)).thenReturn(Optional.of(unavailableMenu));

            OrderCreateRequest request = OrderCreateRequest.builder()
                    .customerName("홍길동")
                    .orderItems(List.of(
                            OrderItemRequest.builder().menuId(1L).quantity(1).build()
                    ))
                    .build();

            // When & Then
            assertThatThrownBy(() -> orderService.createOrder(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("주문할 수 없는 메뉴입니다.");

            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("주문 조회")
    class getOrder {

        @Test
        @DisplayName("성공: 주문 ID로 조회")
        void getOrder_WithValidId_Success() {
            // Given
            Order order = Order.builder()
                    .customerName("홍길동")
                    .build();
            order.addOrderItem(americano, 2);

            when(orderRepository.findByIdWithItems(1L)).thenReturn(order);

            // When
            OrderResponse response = orderService.getOrder(1L);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getCustomerName()).isEqualTo("홍길동");
            assertThat(response.getOrderItems()).hasSize(1);

            verify(orderRepository, times(1)).findByIdWithItems(1L);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 주문")
        void getOrder_WithNonExistentId_ThrowsException() {
            // Given
            when(orderRepository.findByIdWithItems(999L)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> orderService.getOrder(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("주문을 찾을 수 없습니다");
        }

        @Nested
        @DisplayName("주문 상태 변경")
        class UpdateOrderStatus {

            @Test
            @DisplayName("성공: PENDING -> CONFIRMED")
            void updateOrderStatus_PendingToConfirmed_Success() {
                // Given
                Order order = Order.builder()
                        .customerName("홍길동")
                        .build();

                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
                when(orderRepository.save(any(Order.class)))
                        .thenAnswer(invocation -> invocation.getArgument(0));

                // When
                OrderResponse response = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

                // Then
                assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
                verify(orderRepository, times(1)).save(order);
            }

            @Test
            @DisplayName("성공: CONFIRMED -> PREPARING -> COMPLETED")
            void updateOrderStatus_FullFlow_Success() {
                // Given
                Order order = Order.builder()
                        .customerName("홍길동")
                        .build();

                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
                when(orderRepository.save(any(Order.class)))
                        .thenAnswer(invocation -> invocation.getArgument(0));

                // When & Then
                OrderResponse response1 = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);
                assertThat(response1.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

                OrderResponse response2 = orderService.updateOrderStatus(1L, OrderStatus.PREPARING);
                assertThat(response2.getStatus()).isEqualTo(OrderStatus.PREPARING);

                OrderResponse response3 = orderService.updateOrderStatus(1L, OrderStatus.COMPLETED);
                assertThat(response3.getStatus()).isEqualTo(OrderStatus.COMPLETED);
            }

            @Test
            @DisplayName("실패: 잘못된 상태 전환 (PENDING -> COMPLETED)")
            void updateOrderStatus_InvalidTransition_ThrowsException() {
                // Given
                Order order = Order.builder()
                        .customerName("홍길동")
                        .build();

                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

                // When & Then
                assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.COMPLETED))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("주문 상태를");

                verify(orderRepository, never()).save(any(Order.class));
            }

            @Test
            @DisplayName("실패: 완료된 주문의 상태 변경 시도")
            void updateOrderStatus_CompletedOrder_ThrowsException() {
                // Given
                Order order = Order.builder()
                        .customerName("홍길동")
                        .build();
                order.updateStatus(OrderStatus.CONFIRMED);
                order.updateStatus(OrderStatus.PREPARING);
                order.updateStatus(OrderStatus.COMPLETED);

                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

                // When & Then
                assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.PENDING))
                        .isInstanceOf(IllegalStateException.class);
            }
        }


    }



}














