package com.akbar.ecommerce_backend.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class InventoryResponseDTO {

    @Schema(
            description = "Unique ID of the inventory record",
            example = "1"
    )
    private Long id;

    @Schema(
            description = "ID of the associated product",
            example = "1"
    )
    private Long productId;

    @Schema(
            description = "Quantity currently available for sale",
            example = "50"
    )
    private Integer availableQuantity;

    @Schema(
            description = "Quantity currently reserved",
            example = "0"
    )
    private Integer reservedQuantity;

    @Schema(
            description = "Inventory creation timestamp",
            example = "2026-09-16T15:30:00"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Inventory last update timestamp",
            example = "2026-09-16T15:30:00"
    )
    private LocalDateTime updatedAt;

}