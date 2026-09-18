package com.akbar.ecommerce_backend.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CartItemResponseDTO {

    @Schema(
            description = "Unique ID of the cart item",
            example = "1"
    )
    private Long id;


    @Schema(
            description = "ID of the associated product",
            example = "1"
    )
    private Long productId;


    @Schema(
            description = "Quantity of the product in the cart",
            example = "2"
    )
    private Integer quantity;

}
