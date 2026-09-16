package com.akbar.ecommerce_backend.inventory.service;

import com.akbar.ecommerce_backend.common.PageResponseDTO;
import com.akbar.ecommerce_backend.inventory.dto.InventoryRequestDTO;
import com.akbar.ecommerce_backend.inventory.dto.InventoryResponseDTO;
import org.springframework.data.domain.Pageable;

public interface InventoryService {

    InventoryResponseDTO createInventory(InventoryRequestDTO requestDTO);

    PageResponseDTO<InventoryResponseDTO> getAllInventories(Pageable pageable);

    InventoryResponseDTO getInventoryById(Long id);

    InventoryResponseDTO getInventoryByProductId(Long productId);

}
