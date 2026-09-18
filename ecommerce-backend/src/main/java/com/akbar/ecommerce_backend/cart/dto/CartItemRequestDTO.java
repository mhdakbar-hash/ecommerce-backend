package com.akbar.ecommerce_backend.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
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
public class CartItemRequestDTO {


    @Schema(
            description = "ID of the product to add to the cart",
            example = "1"
    )
    @NotNull(message = "Product ID is required")

    private Long productId;


    @Schema(
            description = "Quantity of the product to add",
            example = "2"
    )
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")

    private Integer quantity;

}