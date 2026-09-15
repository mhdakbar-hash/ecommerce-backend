package com.akbar.ecommerce_backend.category.repository;

import com.akbar.ecommerce_backend.category.entity.CategoryEntity;
import com.akbar.ecommerce_backend.category.enums.CategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long>,
        JpaSpecificationExecutor<CategoryEntity> {


    boolean existsByNameIgnoreCase(String name);

    Optional<CategoryEntity> findByIdAndStatusNot(Long id, CategoryStatus status);

    Optional<CategoryEntity> findByIdAndStatus(Long id, CategoryStatus status);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);


}
