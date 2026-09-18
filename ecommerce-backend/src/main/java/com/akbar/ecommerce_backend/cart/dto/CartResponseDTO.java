package com.akbar.ecommerce_backend.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CartResponseDTO {

    @Schema(
            description = "Unique ID of the cart",
            example = "1"
    )
    private Long id;


    @Schema(
            description = "List of items present in the cart"
    )
    private List<CartItemResponseDTO> cartItems;
}
