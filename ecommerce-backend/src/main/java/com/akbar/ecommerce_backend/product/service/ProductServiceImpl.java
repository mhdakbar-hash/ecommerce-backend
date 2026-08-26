package com.akbar.ecommerce_backend.product.service;

import com.akbar.ecommerce_backend.exception.SkuAlreadyExistsException;
import com.akbar.ecommerce_backend.product.dto.ProductRequestDTO;
import com.akbar.ecommerce_backend.product.dto.ProductResponseDTO;
import com.akbar.ecommerce_backend.product.entity.ProductEntity;
import com.akbar.ecommerce_backend.product.enums.ProductStatus;
import com.akbar.ecommerce_backend.product.mapper.ProductMapper;
import com.akbar.ecommerce_backend.product.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper){

        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }




    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO requestDTO) {

        String sku = requestDTO.getSku().trim().toUpperCase();

        log.info("Creating new product. Sku: {}", sku);

        if (productRepository.existsBySkuIgnoreCase(sku)) {
            throw new SkuAlreadyExistsException("Product with SKU '" + sku + "' already exists.");
        }

        ProductEntity product = productMapper.toEntity(requestDTO);

        product.setSku(sku);
        product.setStatus(ProductStatus.ACTIVE);

        ProductEntity savedProduct = productRepository.save(product);

        log.info("Product created successfully. Product ID: {}, SKU: {}",
                savedProduct.getId(),
                savedProduct.getSku());

        return productMapper.toResponseDTO(savedProduct);

    }












}
