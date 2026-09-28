package com.akbar.ecommerce_backend.product.service;

import com.akbar.ecommerce_backend.category.entity.CategoryEntity;
import com.akbar.ecommerce_backend.category.enums.CategoryStatus;
import com.akbar.ecommerce_backend.category.repository.CategoryRepository;
import com.akbar.ecommerce_backend.common.PageResponseDTO;
import com.akbar.ecommerce_backend.exception.ResourceNotFoundException;
import com.akbar.ecommerce_backend.exception.SkuAlreadyExistsException;
import com.akbar.ecommerce_backend.product.dto.ProductRequestDTO;
import com.akbar.ecommerce_backend.product.dto.ProductResponseDTO;
import com.akbar.ecommerce_backend.product.dto.ProductUpdateRequestDTO;
import com.akbar.ecommerce_backend.product.entity.ProductEntity;
import com.akbar.ecommerce_backend.product.enums.ProductStatus;
import com.akbar.ecommerce_backend.product.mapper.ProductMapper;
import com.akbar.ecommerce_backend.product.repository.ProductRepository;
import com.akbar.ecommerce_backend.product.specification.ProductSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                              ProductMapper productMapper, CategoryRepository categoryRepository){

        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.categoryRepository = categoryRepository;
    }




    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO requestDTO) {

        String sku = requestDTO.getSku().trim().toUpperCase();

        log.info("Creating new product. Sku: {}", sku);

        if (productRepository.existsBySkuIgnoreCase(sku)) {
            throw new SkuAlreadyExistsException("Product with SKU '" + sku + "' already exists.");
        }

        CategoryEntity category = categoryRepository
                .findByIdAndStatus(requestDTO.getCategoryId(), CategoryStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "Active category not found with id: " + requestDTO.getCategoryId())
                );

        ProductEntity product = productMapper.toEntity(requestDTO);

        product.setSku(sku);
        product.setStatus(ProductStatus.ACTIVE);

        product.setCategory(category);

        ProductEntity savedProduct = productRepository.save(product);

        log.info("Product created successfully. Product ID: {}, SKU: {}",
                savedProduct.getId(),
                savedProduct.getSku());

        return productMapper.toResponseDTO(savedProduct);

    }


    @Override
    @Cacheable(value = "products", key = "#id")
    public ProductResponseDTO getProductById(Long id) {

        ProductEntity product = productRepository.findByIdAndStatusNot(id, ProductStatus.DELETED)
                .orElseThrow(()-> new ResourceNotFoundException("No product found with id: " + id));

        return productMapper.toResponseDTO(product);

    }





    @Override
    public PageResponseDTO<ProductResponseDTO> getAllProducts(String keyword, ProductStatus status, Pageable pageable) {

        log.debug("Fetching products. Keyword: {}, Status: {}, Page: {}, Size: {}",
                keyword,
                status,
                pageable.getPageNumber(),
                pageable.getPageSize());

        Specification<ProductEntity> specification =
                ProductSpecification.isNotDeleted();

        if (keyword != null && !keyword.isBlank()) {

            specification = specification.and(
                    ProductSpecification.hasKeyword(keyword)
            );
        }

        if (status != null) {

            specification = specification.and(
                    ProductSpecification.hasStatus(status)
            );
        }

        Page<ProductEntity> products =
                productRepository.findAll(specification, pageable);

        log.debug(
                "Fetched {} products out of {} total products. Page: {}, Size: {}",
                products.getNumberOfElements(),
                products.getTotalElements(),
                products.getNumber(),
                products.getSize()
            );

        List<ProductResponseDTO> response =
                products.getContent()
                        .stream()
                        .map(productMapper::toResponseDTO)
                        .toList();

        return new PageResponseDTO<>(
                response,
                products.getNumber(),
                products.getSize(),
                products.getTotalElements(),
                products.getTotalPages(),
                products.isFirst(),
                products.isLast()
        );

    }



    @Override
    public ProductResponseDTO updateProduct(Long id, ProductUpdateRequestDTO requestDTO) {

        ProductEntity product = productRepository.findByIdAndStatusNot(id, ProductStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("No product found with id: " + id));

        log.info("Updating product. Product ID: {}, SKU: {}", id, product.getSku());

        CategoryEntity category = categoryRepository
                        .findByIdAndStatus(requestDTO.getCategoryId(), CategoryStatus.ACTIVE)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "Active category not found with id: " + requestDTO.getCategoryId())
                        );

        product.setName(requestDTO.getName());
        product.setDescription(requestDTO.getDescription());
        product.setPrice(requestDTO.getPrice());
        product.setImageUrl(requestDTO.getImageUrl());
        product.setCategory(category);

        ProductEntity updatedProduct = productRepository.save(product);

        log.info("Product updated successfully. Product ID: {}, SKU: {}",
                updatedProduct.getId(),
                updatedProduct.getSku());

        return productMapper.toResponseDTO(updatedProduct);
    }




    @Override
    public void deleteProduct(Long id) {

        ProductEntity product = productRepository.findByIdAndStatusNot(id, ProductStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("No product found with id: " + id));

        log.info("Soft delete requested. Product ID: {}, SKU: {}",
                product.getId(),
                product.getSku());

        product.setStatus(ProductStatus.DELETED);

        productRepository.save(product);

        log.info("Product soft deleted successfully. Product ID: {}, SKU: {}",
                product.getId(),
                product.getSku());

    }



    @Override
    public ProductResponseDTO restoreProduct(Long id) {

        ProductEntity product = productRepository.findByIdAndStatus(id, ProductStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("No deleted product found with id: " + id));

        log.info("Restore requested. Product ID: {}, SKU: {}",
                product.getId(),
                product.getSku());

        product.setStatus(ProductStatus.ACTIVE);

        ProductEntity restoredProduct = productRepository.save(product);

        log.info("Product restored successfully. Product ID: {}, SKU: {}",
                restoredProduct.getId(),
                restoredProduct.getSku());

        return productMapper.toResponseDTO(restoredProduct);
    }



}
