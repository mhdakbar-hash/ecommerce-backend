package com.akbar.ecommerce_backend.payment.repository;

import com.akbar.ecommerce_backend.payment.entity.PaymentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByIdAndOrderUserId(Long id, Long userId);

    Page<PaymentEntity> findByOrderUserId(Long userId, Pageable pageable);

    boolean existsByOrderId(Long orderId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT p
        FROM PaymentEntity p
        JOIN p.order o
        WHERE p.id = :paymentId 
          AND o.user.id = :userId
        """)
    Optional<PaymentEntity> findByIdAndOrderUserIdForUpdate(
            @Param("paymentId") Long paymentId,
            @Param("userId") Long userId);

}