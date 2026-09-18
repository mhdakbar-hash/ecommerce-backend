package com.akbar.ecommerce_backend.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class CartCountResponseDTO {

    @Schema(
            description = "Total quantity of all products in the cart",
            example = "5"
    )
    private Integer totalItems;

}