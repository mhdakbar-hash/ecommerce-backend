package com.akbar.ecommerce_backend.payment.repository;

import com.akbar.ecommerce_backend.payment.entity.PaymentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByIdAndOrderUserId(Long id, Long userId);

    Page<PaymentEntity> findByOrderUserId(Long userId, Pageable pageable);

    boolean existsByOrderId(Long orderId);

}