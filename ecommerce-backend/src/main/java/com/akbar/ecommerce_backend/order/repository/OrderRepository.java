package com.akbar.ecommerce_backend.order.repository;

import com.akbar.ecommerce_backend.order.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long>,
        JpaSpecificationExecutor<OrderEntity> {


    Page<OrderEntity> findByUserId(Long userId, Pageable pageable);

    Optional<OrderEntity> findByIdAndUserId(Long id, Long userId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT o
        FROM OrderEntity o
        WHERE o.id = :orderId
          AND o.user.id = :userId
        """)
    Optional<OrderEntity> findByIdAndUserIdForUpdate(
            @Param("orderId") Long orderId,
            @Param("userId") Long userId);

}