package com.akbar.ecommerce_backend.cart.repository;

import com.akbar.ecommerce_backend.cart.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository
        extends JpaRepository<CartItemEntity, Long> {

    Optional<CartItemEntity> findByCartIdAndProductId(Long cartId, Long productId);

    Optional<CartItemEntity> findByIdAndCartUserId(Long cartItemId, Long userId);

}