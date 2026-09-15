package com.akbar.ecommerce_backend.category.dto;

import com.akbar.ecommerce_backend.category.enums.CategoryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CategoryResponseDTO {

    @Schema(
            description = "Unique ID of the category",
            example = "1"
    )
    private Long id;


    @Schema(
            description = "Name of the category",
            example = "Electronics"
    )
    private String name;


    @Schema(
            description = "Description of the category",
            example = "Electronic devices and accessories"
    )
    private String description;


    @Schema(
            description = "Current status of the category",
            example = "ACTIVE"
    )
    private CategoryStatus status;


    @Schema(
            description = "Category creation timestamp",
            example = "2026-09-14T15:00:00"
    )
    private LocalDateTime createdAt;


    @Schema(
            description = "Category last update timestamp",
            example = "2026-09-14T15:00:00"
    )
    private LocalDateTime updatedAt;

}
