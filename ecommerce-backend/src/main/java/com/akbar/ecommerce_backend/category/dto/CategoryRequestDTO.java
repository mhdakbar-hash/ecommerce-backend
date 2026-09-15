package com.akbar.ecommerce_backend.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CategoryRequestDTO {


    @Schema(
            description = "Name of the category",
            example = "Electronics"
    )
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    private String name;



    @Schema(
            description = "Description of the category",
            example = "Electronic devices and accessories"
    )
    @Size(max = 500, message = "Category description cannot exceed 500 characters")
    private String description;


}