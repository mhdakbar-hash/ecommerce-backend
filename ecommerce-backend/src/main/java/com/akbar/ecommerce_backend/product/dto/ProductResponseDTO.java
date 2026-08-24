package com.akbar.ecommerce_backend.product.dto;


import com.akbar.ecommerce_backend.product.enums.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductResponseDTO {

    @Schema(
            description = "Unique identifier of the product",
            example = "1"
    )
    private Long id;


    @Schema(
            description = "Name of the product",
            example = "iPhone 15"
    )
    private String name;


    @Schema(
            description = "Detailed description of the product",
            example = "Apple iPhone 15 with 128GB storage."
    )
    private String description;


    @Schema(
            description = "Unique Stock Keeping Unit of the product",
            example = "IPHONE15-BLK-128"
    )
    private String sku;


    @Schema(
            description = "Selling price of the product",
            example = "69999.00"
    )
    private BigDecimal price;


    @Schema(
            description = "Available stock quantity",
            example = "25"
    )
    private Integer stockQuantity;


    @Schema(
            description = "URL of the product image",
            example = "https://example.com/images/iphone15.jpg"
    )
    private String imageUrl;


    @Schema(
            description = "Current status of the product",
            example = "ACTIVE"
    )
    private ProductStatus status;


    @Schema(
            description = "Timestamp when the product was created",
            example = "2026-08-24T10:30:00"
    )
    private LocalDateTime createdAt;


    @Schema(
            description = "Timestamp when the product was last updated",
            example = "2026-08-24T12:45:00"
    )
    private LocalDateTime updatedAt;

}
