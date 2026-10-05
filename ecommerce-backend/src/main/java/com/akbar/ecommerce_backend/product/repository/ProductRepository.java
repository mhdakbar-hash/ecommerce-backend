package com.akbar.ecommerce_backend.product.repository;

import com.akbar.ecommerce_backend.product.entity.ProductEntity;
import com.akbar.ecommerce_backend.product.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long>,
        JpaSpecificationExecutor<ProductEntity> {


    boolean existsBySkuIgnoreCase(String sku);

    Optional<ProductEntity> findByIdAndStatusNot (Long id, ProductStatus status);

    Optional<ProductEntity> findByIdAndStatus(Long id, ProductStatus status);

    boolean existsByCategoryId(Long categoryId);


    @Query("""
            select p.id
            from ProductEntity p
            where p.category.id = :categoryId
            """)
    List<Long> findProductIdsByCategoryId(@Param("categoryId") Long categoryId);

}
