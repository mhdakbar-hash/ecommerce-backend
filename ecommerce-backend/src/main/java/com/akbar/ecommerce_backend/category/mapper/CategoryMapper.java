package com.akbar.ecommerce_backend.category.mapper;

import com.akbar.ecommerce_backend.category.dto.CategoryRequestDTO;
import com.akbar.ecommerce_backend.category.dto.CategoryResponseDTO;
import com.akbar.ecommerce_backend.category.entity.CategoryEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {


    public CategoryEntity toEntity(CategoryRequestDTO requestDTO) {

        return CategoryEntity.builder()
                .name(requestDTO.getName())
                .description(requestDTO.getDescription())
                .build();
    }



    public CategoryResponseDTO toResponseDTO(CategoryEntity entity) {

        return CategoryResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
