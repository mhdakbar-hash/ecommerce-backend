package com.akbar.ecommerce_backend.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductRequestDTO {


    @Schema(
            description = "Name of the product",
            example = "iPhone 15"
    )
    @NotBlank(message = "Product name is required.")
    @Size(min = 2, max = 150, message = "Product name must be between 2 and 150 characters.")
    private String name;



    @Schema(
            description = "Detailed description of the product",
            example = "Apple iPhone 15 with 128GB storage."
    )
//    @NotBlank(message = "Product description is required.")
    @Size(min = 10, max = 2000, message = "Product description must be between 10 and 2000 characters.")
    private String description;



    @Schema(
            description = "Unique Stock Keeping Unit of the product",
            example = "IPHONE15-BLK-128"
    )
    @NotBlank(message = "SKU is required.")
    @Size(min = 3, max = 100, message = "SKU must be between 3 and 100 characters.")
    private String sku;



    @Schema(
            description = "Selling price of the product",
            example = "69999.00"
    )
    @NotNull(message = "Product price is required.")
    @DecimalMin(value = "0.01", message = "Product price must be greater than 0.")
    private BigDecimal price;



    @Schema(
            description = "Available stock quantity",
            example = "25"
    )
    @NotNull(message = "Stock quantity is required.")
    @PositiveOrZero(message = "Stock quantity cannot be negative.")
    private Integer stockQuantity;



    @Schema(
            description = "URL of the product image",
            example = "https://example.com/images/iphone15.jpg"
    )
    @Size(max = 500, message = "Image URL cannot exceed 500 characters.")
    private String imageUrl;


}
