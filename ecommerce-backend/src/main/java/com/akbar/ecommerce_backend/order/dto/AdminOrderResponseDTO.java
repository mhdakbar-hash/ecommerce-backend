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
@Schema(description = "Administrative view of a customer order")

public class AdminOrderResponseDTO {

    @Schema(
            description = "Unique identifier of the order",
            example = "101"
    )
    private Long id;


    @Schema(
            description = "Unique business order number",
            example = "ORD-8f72a91c"
    )
    private String orderNumber;


    @Schema(
            description = "ID of the customer who placed the order",
            example = "25"
    )
    private Long userId;

    @Schema(
            description = "Email address of the customer who placed the order",
            example = "customer@gmail.com"
    )
    private String userEmail;


    @Schema(
            description = "Current status of the order",
            example = "CONFIRMED"
    )
    private OrderStatus status;


    @Schema(
            description = "Total amount of the order",
            example = "2999.00"
    )
    private BigDecimal totalAmount;


    @Schema(
            description = "Items included in the order"
    )
    private List<OrderItemResponseDTO> orderItems;


    @Schema(
            description = "Date and time when the order was created",
            example = "2026-09-24T14:30:00"
    )
    private LocalDateTime createdAt;

}