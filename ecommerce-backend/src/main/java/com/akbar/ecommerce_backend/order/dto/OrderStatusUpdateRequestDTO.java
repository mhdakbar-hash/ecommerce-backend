package com.akbar.ecommerce_backend.order.dto;

import com.akbar.ecommerce_backend.order.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Request to update the status of an order")

public class OrderStatusUpdateRequestDTO {


    @NotNull(message = "Order status is required")
    @Schema(
            description = "New status to assign to the order",
            example = "PROCESSING"
    )
    private OrderStatus status;

}