package com.akbar.ecommerce_backend.order.dto;

import com.akbar.ecommerce_backend.order.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderResponseDTO {

    @Schema(
            description = "Unique ID of the order",
            example = "1"
    )
    private Long id;


    @Schema(
            description = "Unique business order number",
            example = "ORD-100001"
    )
    private String orderNumber;


    @Schema(
            description = "Current status of the order",
            example = "PENDING"
    )
    private OrderStatus status;


    @Schema(
            description = "Total amount of the order",
            example = "1499.98"
    )
    private BigDecimal totalAmount;


    @Schema(
            description = "Products included in the order"
    )
    private List<OrderItemResponseDTO> orderItems;


    @Schema(
            description = "Order creation timestamp"
    )
    private LocalDateTime createdAt;

}