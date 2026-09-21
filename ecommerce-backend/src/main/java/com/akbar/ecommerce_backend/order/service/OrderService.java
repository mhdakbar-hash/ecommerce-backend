package com.akbar.ecommerce_backend.order.service;

import com.akbar.ecommerce_backend.common.PageResponseDTO;
import com.akbar.ecommerce_backend.order.dto.OrderResponseDTO;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponseDTO createOrder();

    PageResponseDTO<OrderResponseDTO> getMyOrders(Pageable pageable);

    OrderResponseDTO getMyOrderById(Long id);

    OrderResponseDTO cancelMyOrder(Long id);
}
