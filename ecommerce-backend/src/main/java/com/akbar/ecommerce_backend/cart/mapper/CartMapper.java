package com.akbar.ecommerce_backend.cart.mapper;

import com.akbar.ecommerce_backend.cart.dto.CartCountResponseDTO;
import com.akbar.ecommerce_backend.cart.dto.CartItemResponseDTO;
import com.akbar.ecommerce_backend.cart.dto.CartResponseDTO;
import com.akbar.ecommerce_backend.cart.entity.CartEntity;
import com.akbar.ecommerce_backend.cart.entity.CartItemEntity;
import org.springframework.stereotype.Component;

@Component
public class CartMapper {

    public CartItemResponseDTO toCartItemResponseDTO(CartItemEntity entity) {

        return CartItemResponseDTO.builder()
                .id(entity.getId())
                .productId(entity.getProduct().getId())
                .quantity(entity.getQuantity())
                .build();
    }


    public CartResponseDTO tocartResponseDTO(CartEntity entity) {

        return CartResponseDTO.builder()
                .id(entity.getId())
                .cartItems(
                        entity.getCartItems()
                            .stream()
                            .map(this::toCartItemResponseDTO)
                            .toList()
                )
                .build();
    }


    public CartCountResponseDTO tocartCountResponseDTO(int totalItems){

        return CartCountResponseDTO.builder()
                .totalItems(totalItems)
                .build();
    }

}