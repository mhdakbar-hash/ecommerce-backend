package com.akbar.ecommerce_backend.order.mapper;

import com.akbar.ecommerce_backend.order.dto.AdminOrderResponseDTO;
import com.akbar.ecommerce_backend.order.dto.OrderItemResponseDTO;
import com.akbar.ecommerce_backend.order.dto.OrderResponseDTO;
import com.akbar.ecommerce_backend.order.entity.OrderEntity;
import com.akbar.ecommerce_backend.order.entity.OrderItemEntity;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderItemResponseDTO toOrderItemResponseDTO(OrderItemEntity entity) {

        return OrderItemResponseDTO.builder()
                .id(entity.getId())
                .productId(entity.getProduct().getId())
                .productName(entity.getProductName())
                .sku(entity.getSku())
                .quantity(entity.getQuantity())
                .unitPrice(entity.getUnitPrice())
                .subtotal(entity.getSubtotal())
                .build();
    }




    public OrderResponseDTO toOrderResponseDTO(OrderEntity entity) {

        return OrderResponseDTO.builder()
                .id(entity.getId())
                .orderNumber(entity.getOrderNumber())
                .status(entity.getStatus())
                .totalAmount(entity.getTotalAmount())
                .orderItems(
                        entity.getOrderItems()
                                .stream()
                                .map(this::toOrderItemResponseDTO)
                                .toList()
                )
                .createdAt(entity.getCreatedAt())
                .build();
    }




    public AdminOrderResponseDTO toAdminOrderResponseDTO(OrderEntity entity) {

        return AdminOrderResponseDTO.builder()
                .id(entity.getId())
                .orderNumber(entity.getOrderNumber())
                .userId(entity.getUser().getId())
                .userEmail(entity.getUser().getEmail())
                .status(entity.getStatus())
                .totalAmount(entity.getTotalAmount())
                .orderItems(
                        entity.getOrderItems()
                                .stream()
                                .map(this::toOrderItemResponseDTO)
                                .toList()
                )
                .createdAt(entity.getCreatedAt())
                .build();
    }

}