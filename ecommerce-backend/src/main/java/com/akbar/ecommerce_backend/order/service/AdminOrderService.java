package com.akbar.ecommerce_backend.order.service;

import com.akbar.ecommerce_backend.common.PageResponseDTO;
import com.akbar.ecommerce_backend.order.dto.AdminOrderResponseDTO;
import com.akbar.ecommerce_backend.order.dto.OrderStatusUpdateRequestDTO;
import com.akbar.ecommerce_backend.order.enums.OrderStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface AdminOrderService {

    PageResponseDTO<AdminOrderResponseDTO> getAllOrders(
            Pageable pageable,
            OrderStatus status,
            String orderNumber,
            String userEmail,
            LocalDateTime startDate,
            LocalDateTime endDate
    );


    AdminOrderResponseDTO getOrderById(Long id);

    AdminOrderResponseDTO updateOrderStatus(Long id, OrderStatusUpdateRequestDTO requestDTO);

}