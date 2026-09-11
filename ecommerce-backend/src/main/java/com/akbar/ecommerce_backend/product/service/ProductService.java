package com.akbar.ecommerce_backend.product.service;

import com.akbar.ecommerce_backend.common.PageResponseDTO;
import com.akbar.ecommerce_backend.product.dto.ProductRequestDTO;
import com.akbar.ecommerce_backend.product.dto.ProductResponseDTO;
import com.akbar.ecommerce_backend.product.dto.ProductUpdateRequestDTO;
import com.akbar.ecommerce_backend.product.enums.ProductStatus;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponseDTO createProduct(ProductRequestDTO requestDTO);

    ProductResponseDTO getProductById(Long id);

    PageResponseDTO<ProductResponseDTO> getAllProducts(String keyword, ProductStatus status, Pageable pageable);

    ProductResponseDTO updateProduct(Long id, ProductUpdateRequestDTO requestDTO);

    void deleteProduct(Long id);

    ProductResponseDTO restoreProduct(Long id);

}
