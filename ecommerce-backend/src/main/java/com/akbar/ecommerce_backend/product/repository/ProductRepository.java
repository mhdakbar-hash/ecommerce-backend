package com.akbar.ecommerce_backend.product.repository;

import com.akbar.ecommerce_backend.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    boolean existsBySkuIgnoreCase(String sku);

}
