package com.akbar.ecommerce_backend.inventory.mapper;

import com.akbar.ecommerce_backend.inventory.dto.InventoryRequestDTO;
import com.akbar.ecommerce_backend.inventory.dto.InventoryResponseDTO;
import com.akbar.ecommerce_backend.inventory.entity.InventoryEntity;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public InventoryEntity toEntity(InventoryRequestDTO requestDTO) {

        return InventoryEntity.builder()
                .availableQuantity(requestDTO.getAvailableQuantity())
                .reservedQuantity(0)
                .build();
    }


    public InventoryResponseDTO toResponseDTO(InventoryEntity entity) {

        return InventoryResponseDTO.builder()
                .id(entity.getId())
                .productId(entity.getProduct().getId())
                .availableQuantity(entity.getAvailableQuantity())
                .reservedQuantity(entity.getReservedQuantity())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
