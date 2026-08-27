package com.akbar.ecommerce_backend.product.repository;

import com.akbar.ecommerce_backend.product.entity.ProductEntity;
import com.akbar.ecommerce_backend.product.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    boolean existsBySkuIgnoreCase(String sku);

    Optional<ProductEntity> findByIdAndStatusNot (Long id, ProductStatus status);

}
