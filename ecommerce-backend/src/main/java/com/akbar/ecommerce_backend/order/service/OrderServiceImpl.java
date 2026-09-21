package com.akbar.ecommerce_backend.order.service;

import com.akbar.ecommerce_backend.cart.entity.CartEntity;
import com.akbar.ecommerce_backend.cart.entity.CartItemEntity;
import com.akbar.ecommerce_backend.cart.repository.CartRepository;
import com.akbar.ecommerce_backend.common.PageResponseDTO;
import com.akbar.ecommerce_backend.exception.CartEmptyException;
import com.akbar.ecommerce_backend.exception.OrderCancellationException;
import com.akbar.ecommerce_backend.exception.ResourceNotFoundException;
import com.akbar.ecommerce_backend.order.dto.OrderResponseDTO;
import com.akbar.ecommerce_backend.order.entity.OrderEntity;
import com.akbar.ecommerce_backend.order.entity.OrderItemEntity;
import com.akbar.ecommerce_backend.order.enums.OrderStatus;
import com.akbar.ecommerce_backend.order.mapper.OrderMapper;
import com.akbar.ecommerce_backend.order.repository.OrderRepository;
import com.akbar.ecommerce_backend.user.entity.UserEntity;
import com.akbar.ecommerce_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {


    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;




    @Override
    @Transactional
    public OrderResponseDTO createOrder() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();


        log.info("Creating order. User email: {}",
                email);


        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "No user found with email: " + email));


        CartEntity cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                                "No cart found for user id: " + user.getId()));


        if (cart.getCartItems().isEmpty()) {
            throw new CartEmptyException(
                    "Cannot create order because cart is empty");
        }

        OrderEntity order = OrderEntity.builder()
                .user(user)
                .orderNumber(
                        "ORD-" + UUID.randomUUID()
                )
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .orderItems(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItemEntity cartItem :
                cart.getCartItems()) {

            BigDecimal unitPrice =
                    cartItem.getProduct().getPrice();

            BigDecimal subtotal =
                    unitPrice.multiply(
                            BigDecimal.valueOf(
                                    cartItem.getQuantity()
                            )
                    );

            OrderItemEntity orderItem =
                    OrderItemEntity.builder()
                            .order(order)
                            .product(cartItem.getProduct())
                            .productName(
                                    cartItem.getProduct().getName()
                            )
                            .sku(
                                    cartItem.getProduct().getSku()
                            )
                            .quantity(
                                    cartItem.getQuantity()
                            )
                            .unitPrice(unitPrice)
                            .subtotal(subtotal)
                            .build();

            order.getOrderItems().add(orderItem);

            totalAmount = totalAmount.add(subtotal);
        }

        order.setTotalAmount(totalAmount);

        OrderEntity savedOrder =
                orderRepository.save(order);


        log.info("Order created successfully. Order ID: {}, Order Number: {}, User ID: {}, Total Amount: {}",
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                user.getId(),
                savedOrder.getTotalAmount());


        return orderMapper.toOrderResponseDTO(savedOrder);

    }







    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<OrderResponseDTO> getMyOrders(Pageable pageable) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        log.debug("Fetching orders for user. User email: {}",
                email);

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "No user found with email: " + email)
                );


        Page<OrderEntity> orderPage =
                orderRepository.findByUserId(user.getId(), pageable);


        List<OrderResponseDTO> orders =
                orderPage.getContent()
                        .stream()
                        .map(orderMapper::toOrderResponseDTO)
                        .toList();

        return new PageResponseDTO<>(
                orders,
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
    public OrderResponseDTO getMyOrderById(Long id) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        log.debug("Fetching order by ID. Order ID: {}, User email: {}",
                id,
                email);


        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "No user found with email: " + email));


        OrderEntity order = orderRepository.findByIdAndUserId(
                id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                                "No order found with id: " + id));

        log.debug("Order fetched successfully. Order ID: {}, User ID: {}",
                order.getId(),
                user.getId());

        return orderMapper.toOrderResponseDTO(order);

    }






    @Override
    @Transactional
    public OrderResponseDTO cancelMyOrder(Long id) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        log.info("Cancelling order. Order ID: {}, User email: {}",
                id,
                email);


        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "No user found with email: " + email));


        OrderEntity order = orderRepository.findByIdAndUserId(
                id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                                "No order found with id: " + id));


        OrderStatus currentStatus = order.getStatus();


        if (currentStatus != OrderStatus.PENDING
                && currentStatus != OrderStatus.CONFIRMED) {

            throw new OrderCancellationException(
                    "Order cannot be cancelled because its current status is " + currentStatus);
        }

        order.setStatus(OrderStatus.CANCELLED);

        OrderEntity savedOrder =
                orderRepository.save(order);


        log.info("Order cancelled successfully. Order ID: {}, Order Number: {}, User ID: {}",
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                user.getId());

        return orderMapper.toOrderResponseDTO(savedOrder);
    }


}