package com.akbar.ecommerce_backend.product.mapper;

import com.akbar.ecommerce_backend.product.dto.ProductRequestDTO;
import com.akbar.ecommerce_backend.product.dto.ProductResponseDTO;
import com.akbar.ecommerce_backend.product.entity.ProductEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductEntity toEntity(ProductRequestDTO requestDTO) {

        return ProductEntity.builder()
                .name(requestDTO.getName())
                .description(requestDTO.getDescription())
                .sku(requestDTO.getSku())
                .price(requestDTO.getPrice())
                .imageUrl(requestDTO.getImageUrl())
//                .status(ProductStatus.ACTIVE)     -> this will be business logic part
               .build();

    }


    public ProductResponseDTO toResponseDTO(ProductEntity productEntity) {

        return ProductResponseDTO.builder()
                .id(productEntity.getId())
                .name(productEntity.getName())
                .description(productEntity.getDescription())
                .sku(productEntity.getSku())
                .price(productEntity.getPrice())
                .imageUrl(productEntity.getImageUrl())
                .status(productEntity.getStatus())
                .categoryName(productEntity.getCategory().getName())
                .createdAt(productEntity.getCreatedAt())
                .updatedAt(productEntity.getUpdatedAt())
                .build();

    }
}
