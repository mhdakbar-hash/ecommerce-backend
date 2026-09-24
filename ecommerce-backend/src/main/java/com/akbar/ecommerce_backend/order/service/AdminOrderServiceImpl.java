package com.akbar.ecommerce_backend.order.service;

import com.akbar.ecommerce_backend.common.PageResponseDTO;
import com.akbar.ecommerce_backend.exception.OrderStatusUpdateException;
import com.akbar.ecommerce_backend.exception.ResourceNotFoundException;
import com.akbar.ecommerce_backend.order.dto.AdminOrderResponseDTO;
import com.akbar.ecommerce_backend.order.dto.OrderStatusUpdateRequestDTO;
import com.akbar.ecommerce_backend.order.entity.OrderEntity;
import com.akbar.ecommerce_backend.order.enums.OrderStatus;
import com.akbar.ecommerce_backend.order.mapper.OrderMapper;
import com.akbar.ecommerce_backend.order.repository.OrderRepository;
import com.akbar.ecommerce_backend.order.specification.OrderSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminOrderServiceImpl implements AdminOrderService {


    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;




    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<AdminOrderResponseDTO> getAllOrders(
            Pageable pageable,
            OrderStatus status,
            String orderNumber,
            String userEmail,
            LocalDateTime startDate,
            LocalDateTime endDate) {


        log.debug("Fetching admin orders. Status: {}, Order Number: {}, User Email: {}, Start Date: {}, End Date: {}",
                status,
                orderNumber,
                userEmail,
                startDate,
                endDate);


        Specification<OrderEntity> specification =
                Specification.where(null);

        if (status != null) {

            specification = specification.and(
                            OrderSpecification.hasStatus(status)
                    );
        }

        if (orderNumber != null
                && !orderNumber.isBlank()) {

            specification =
                    specification.and(
                            OrderSpecification.hasOrderNumber(orderNumber)
                    );
        }

        if (userEmail != null
                && !userEmail.isBlank()) {

            specification =
                    specification.and(
                            OrderSpecification.hasUserEmail(userEmail)
                    );
        }

        if (startDate != null) {

            specification =
                    specification.and(
                            OrderSpecification
                                    .createdAtGreaterThanOrEqualTo(startDate)
                    );
        }

        if (endDate != null) {

            specification =
                    specification.and(
                            OrderSpecification
                                    .createdAtLessThanOrEqualTo(endDate)
                    );
        }


        Page<OrderEntity> orderPage = orderRepository.findAll(
                        specification,
                        pageable);


        List<AdminOrderResponseDTO> content =
                orderPage.getContent()
                        .stream()
                        .map(orderMapper::toAdminOrderResponseDTO)
                        .toList();

        return new PageResponseDTO<>(
                content,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.isFirst(),
                orderPage.isLast()
        );
    }






    @Override
    @Transactional(readOnly = true)
    public AdminOrderResponseDTO getOrderById(Long id) {


        log.debug("Fetching admin order details. Order ID: {}",
                id);

        OrderEntity order = orderRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "No order found with id: " + id));


        return orderMapper.toAdminOrderResponseDTO(order);

    }






    @Override
    @Transactional
    public AdminOrderResponseDTO updateOrderStatus(Long id, OrderStatusUpdateRequestDTO requestDTO) {

        log.info("Updating order status. Order ID: {}, New Status: {}",
                id,
                requestDTO.getStatus());


        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                                        "No order found with id: " + id));


        OrderStatus currentStatus = order.getStatus();

        OrderStatus newStatus = requestDTO.getStatus();


        if (currentStatus == newStatus) {

            throw new OrderStatusUpdateException(
                    "Order already has status "
                            + currentStatus);
        }


        boolean validTransition =
                (currentStatus == OrderStatus.CONFIRMED
                        && newStatus == OrderStatus.PROCESSING)
                        ||
                        (currentStatus == OrderStatus.PROCESSING
                                && newStatus == OrderStatus.SHIPPED)
                        ||
                        (currentStatus == OrderStatus.SHIPPED
                                && newStatus == OrderStatus.DELIVERED);


        if (!validTransition) {

            throw new OrderStatusUpdateException(
                    "Order status cannot be changed from "
                            + currentStatus + " to " + newStatus);
        }


        order.setStatus(newStatus);

        OrderEntity updatedOrder = orderRepository.save(order);

        log.info("Order status updated successfully. Order ID: {}, Previous Status: {}, New Status: {}",
                updatedOrder.getId(),
                currentStatus,
                updatedOrder.getStatus());

        return orderMapper.toAdminOrderResponseDTO(updatedOrder);
    }


}