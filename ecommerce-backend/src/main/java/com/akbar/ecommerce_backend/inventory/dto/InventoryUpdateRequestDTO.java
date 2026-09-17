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
public class InventoryUpdateRequestDTO {

    @Schema(
            description = "Updated quantity available for sale",
            example = "120"
    )
    @NotNull(message = "Available quantity is required")
    @Min(value = 0, message = "Available quantity cannot be negative")

    private Integer availableQuantity;
}