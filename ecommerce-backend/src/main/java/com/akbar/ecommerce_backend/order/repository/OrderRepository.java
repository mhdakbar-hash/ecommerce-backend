package com.akbar.ecommerce_backend.order.repository;

import com.akbar.ecommerce_backend.order.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long>,
        JpaSpecificationExecutor<OrderEntity> {


    Page<OrderEntity> findByUserId(Long userId, Pageable pageable);

    Optional<OrderEntity> findByIdAndUserId(Long id, Long userId);

}