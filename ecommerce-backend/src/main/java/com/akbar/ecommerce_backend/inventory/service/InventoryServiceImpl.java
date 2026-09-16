package com.akbar.ecommerce_backend.inventory.service;

import com.akbar.ecommerce_backend.common.PageResponseDTO;
import com.akbar.ecommerce_backend.exception.InventoryAlreadyExistsException;
import com.akbar.ecommerce_backend.exception.ResourceNotFoundException;
import com.akbar.ecommerce_backend.inventory.dto.InventoryRequestDTO;
import com.akbar.ecommerce_backend.inventory.dto.InventoryResponseDTO;
import com.akbar.ecommerce_backend.inventory.entity.InventoryEntity;
import com.akbar.ecommerce_backend.inventory.mapper.InventoryMapper;
import com.akbar.ecommerce_backend.inventory.repository.InventoryRepository;
import com.akbar.ecommerce_backend.product.entity.ProductEntity;
import com.akbar.ecommerce_backend.product.enums.ProductStatus;
import com.akbar.ecommerce_backend.product.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final InventoryMapper inventoryMapper;

    public InventoryServiceImpl(InventoryRepository inventoryRepository, ProductRepository productRepository,
                                InventoryMapper inventoryMapper) {

        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.inventoryMapper = inventoryMapper;
    }



    @Override
    public InventoryResponseDTO createInventory(InventoryRequestDTO requestDTO) {

        log.info("Creating inventory. Product ID: {}",
                requestDTO.getProductId());


        ProductEntity product = productRepository.findByIdAndStatusNot(requestDTO.getProductId(),
                        ProductStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("No product found with id: "
                        + requestDTO.getProductId()));

        if (inventoryRepository.existsByProductId(requestDTO.getProductId())) {

            throw new InventoryAlreadyExistsException("Inventory already exists for product id: "
                            + requestDTO.getProductId());
        }

        InventoryEntity inventory = inventoryMapper.toEntity(requestDTO);

        inventory.setProduct(product);

        InventoryEntity savedInventory = inventoryRepository.save(inventory);

        log.info("Inventory created successfully. Inventory ID: {}, Product ID: {}",
                savedInventory.getId(),
                product.getId());

        return inventoryMapper.toResponseDTO(savedInventory);

    }





    @Override
    public PageResponseDTO<InventoryResponseDTO> getAllInventories(Pageable pageable) {

        log.debug("Fetching inventories. Page: {}, Size: {}",
                pageable.getPageNumber(),
                pageable.getPageSize());

        Page<InventoryEntity> inventories =
                inventoryRepository.findAll(pageable);

        log.debug("Fetched {} inventories out of {} total inventories. Page: {}, Size: {}",
                inventories.getNumberOfElements(),
                inventories.getTotalElements(),
                inventories.getNumber(),
                inventories.getSize());


        List<InventoryResponseDTO> response =
                inventories.getContent()
                        .stream()
                        .map(inventoryMapper::toResponseDTO)
                        .toList();

        return new PageResponseDTO<>(
                response,
                inventories.getNumber(),
                inventories.getSize(),
                inventories.getTotalElements(),
                inventories.getTotalPages(),
                inventories.isFirst(),
                inventories.isLast()
        );
    }






    @Override
    public InventoryResponseDTO getInventoryById(Long id) {

        log.debug("Fetching inventory by ID: {}",
                id);

        InventoryEntity inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No inventory found with id: " + id));


        log.debug("Inventory fetched successfully. Inventory ID: {}, Product ID: {}",
                inventory.getId(),
                inventory.getProduct().getId());

        return inventoryMapper.toResponseDTO(inventory);

    }




    @Override
    public InventoryResponseDTO getInventoryByProductId(Long productId) {

        log.debug("Fetching inventory by Product ID: {}",
                productId);

        ProductEntity product = productRepository.findByIdAndStatusNot(productId, ProductStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("No product found with id: " + productId));


        InventoryEntity inventory = inventoryRepository.findByProductId(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("No inventory found for product id: "
                                                + productId));


        log.debug("Inventory fetched successfully. Inventory ID: {}, Product ID: {}",
                inventory.getId(),
                product.getId());

        return inventoryMapper.toResponseDTO(inventory);

    }







}
