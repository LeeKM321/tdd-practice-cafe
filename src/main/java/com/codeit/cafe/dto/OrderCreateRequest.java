package com.codeit.cafe.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreateRequest {

    @NotBlank(message = "고객 이름은 필수입니다.")
    private String customerName;

    @NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다.")
    @Valid
    private List<OrderItemRequest> orderItems;

}









