package com.akbar.ecommerce_backend.product.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductUpdateRequestDTO {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    @Schema(
            description = "Updated product name",
            example = "iPhone 16 Pro"
    )
    private String name;


    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Schema(
            description = "Updated product description",
            example = "Latest Apple smartphone with A18 Pro chip"
    )
    private String description;


    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Schema(
            description = "Updated product price",
            example = "129999.00"
    )
    private BigDecimal price;


    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Schema(
            description = "Updated stock quantity",
            example = "25"
    )
    private Integer stockQuantity;


    @Schema(
            description = "Updated product image URL",
            example = "https://example.com/iphone16pro.jpg"
    )
    private String imageUrl;
}