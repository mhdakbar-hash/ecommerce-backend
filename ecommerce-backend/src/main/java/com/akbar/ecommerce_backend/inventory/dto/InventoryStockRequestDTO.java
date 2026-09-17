package com.akbar.ecommerce_backend.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class InventoryStockRequestDTO {


    @Schema(
            description = "Quantity of stock to add or remove",
            example = "20"
    )
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")

    private Integer quantity;
}
