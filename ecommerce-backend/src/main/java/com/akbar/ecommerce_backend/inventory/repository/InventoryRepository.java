package com.akbar.ecommerce_backend.inventory.repository;

import com.akbar.ecommerce_backend.inventory.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {


    boolean existsByProductId(Long productId);

    Optional<InventoryEntity> findByProductId(Long productId);

}