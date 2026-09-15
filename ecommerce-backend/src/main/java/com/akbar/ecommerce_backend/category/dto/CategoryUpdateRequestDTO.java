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
public class CategoryUpdateRequestDTO {


    @Schema(
            description = "Updated name of the category",
            example = "Mobile Electronics"
    )
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")

    private String name;


    @Schema(
            description = "Updated description of the category",
            example = "Mobile phones, tablets and related accessories"
    )
    @Size(max = 500, message = "Category description cannot exceed 500 characters")
    private String description;

}
