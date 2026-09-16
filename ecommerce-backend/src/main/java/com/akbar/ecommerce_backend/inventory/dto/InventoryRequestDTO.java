package com.akbar.ecommerce_backend.inventory.dto;

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
public class InventoryRequestDTO {


    @Schema(
            description = "ID of the product for which inventory is being created",
            example = "1"
    )
    @NotNull(message = "Product ID is required")

    private Long productId;



    @Schema(
            description = "Initial quantity available for sale",
            example = "50"
    )
    @NotNull(message = "Available quantity is required")
    @Min(value = 0, message = "Available quantity cannot be negative")

    private Integer availableQuantity;

}