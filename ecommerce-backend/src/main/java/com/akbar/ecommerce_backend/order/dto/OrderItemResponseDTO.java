package com.akbar.ecommerce_backend.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderItemResponseDTO {

    @Schema(
            description = "Unique ID of the order item",
            example = "1"
    )
    private Long id;


    @Schema(
            description = "ID of the purchased product",
            example = "1"
    )
    private Long productId;


    @Schema(
            description = "Product name at the time of purchase",
            example = "Wireless Mouse"
    )
    private String productName;


    @Schema(
            description = "Product SKU at the time of purchase",
            example = "MOUSE-001"
    )
    private String sku;


    @Schema(
            description = "Purchased quantity",
            example = "2"
    )
    private Integer quantity;


    @Schema(
            description = "Price of one unit at the time of purchase",
            example = "499.99"
    )
    private BigDecimal unitPrice;


    @Schema(
            description = "Total price for this order item",
            example = "999.98"
    )
    private BigDecimal subtotal;

}