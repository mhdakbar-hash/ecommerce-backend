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
public class CartItemUpdateRequestDTO {


    @Schema(
            description = "New quantity of the product",
            example = "5"
    )
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")

    private Integer quantity;
}