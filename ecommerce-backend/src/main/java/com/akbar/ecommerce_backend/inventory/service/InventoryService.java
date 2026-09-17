package com.akbar.ecommerce_backend.inventory.service;

import com.akbar.ecommerce_backend.common.PageResponseDTO;
import com.akbar.ecommerce_backend.inventory.dto.InventoryRequestDTO;
import com.akbar.ecommerce_backend.inventory.dto.InventoryResponseDTO;
import com.akbar.ecommerce_backend.inventory.dto.InventoryStockRequestDTO;
import com.akbar.ecommerce_backend.inventory.dto.InventoryUpdateRequestDTO;
import org.springframework.data.domain.Pageable;

public interface InventoryService {

    InventoryResponseDTO createInventory(InventoryRequestDTO requestDTO);

    PageResponseDTO<InventoryResponseDTO> getAllInventories(Pageable pageable);

    InventoryResponseDTO getInventoryById(Long id);

    InventoryResponseDTO getInventoryByProductId(Long productId);

    InventoryResponseDTO updateInventory(Long id, InventoryUpdateRequestDTO requestDTO);

    InventoryResponseDTO addStock(Long id, InventoryStockRequestDTO requestDTO);

    InventoryResponseDTO removeStock(Long id, InventoryStockRequestDTO requestDTO);




}
