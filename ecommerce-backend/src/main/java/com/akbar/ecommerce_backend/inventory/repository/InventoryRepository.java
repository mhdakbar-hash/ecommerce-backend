package com.akbar.ecommerce_backend.inventory.repository;

import com.akbar.ecommerce_backend.inventory.entity.InventoryEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {


    boolean existsByProductId(Long productId);

    Optional<InventoryEntity> findByProductId(Long productId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT i
            FROM InventoryEntity i
            WHERE i.product.id = :productId
            """)
    Optional<InventoryEntity> findByProductIdForUpdate(@Param("productId") Long productId);



    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT i
        FROM InventoryEntity i
        WHERE i.id = :id
        """)
    Optional<InventoryEntity> findByIdForUpdate(@Param("id") Long id);

}