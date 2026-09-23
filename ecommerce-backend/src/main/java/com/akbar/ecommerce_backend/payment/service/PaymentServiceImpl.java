package com.akbar.ecommerce_backend.payment.service;

import com.akbar.ecommerce_backend.common.PageResponseDTO;
import com.akbar.ecommerce_backend.exception.PaymentInitiationException;
import com.akbar.ecommerce_backend.exception.PaymentRetryException;
import com.akbar.ecommerce_backend.exception.ResourceNotFoundException;
import com.akbar.ecommerce_backend.order.entity.OrderEntity;
import com.akbar.ecommerce_backend.order.enums.OrderStatus;
import com.akbar.ecommerce_backend.order.repository.OrderRepository;
import com.akbar.ecommerce_backend.payment.dto.PaymentRequestDTO;
import com.akbar.ecommerce_backend.payment.dto.PaymentResponseDTO;
import com.akbar.ecommerce_backend.payment.dto.PaymentRetryRequestDTO;
import com.akbar.ecommerce_backend.payment.entity.PaymentEntity;
import com.akbar.ecommerce_backend.payment.enums.PaymentStatus;
import com.akbar.ecommerce_backend.payment.gateway.PaymentGateway;
import com.akbar.ecommerce_backend.payment.gateway.PaymentGatewayResponse;
import com.akbar.ecommerce_backend.payment.mapper.PaymentMapper;
import com.akbar.ecommerce_backend.payment.repository.PaymentRepository;
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

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {


    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentGateway paymentGateway;




    @Override
    @Transactional
    public PaymentResponseDTO initiatePayment(PaymentRequestDTO requestDTO) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();


        log.info("Initiating payment. Order ID: {}, User email: {}",
                requestDTO.getOrderId(),
                email);


        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "No user found with email: " + email));


        OrderEntity order = orderRepository.findByIdAndUserId(requestDTO.getOrderId(),
                        user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No order found with id: " + requestDTO.getOrderId()));


        if (order.getStatus() != OrderStatus.PENDING) {
            throw new PaymentInitiationException(
                    "Payment cannot be initiated for order with status "
                            + order.getStatus());
        }


        if (paymentRepository.existsByOrderId(order.getId())) {

            throw new PaymentInitiationException(
                    "A payment already exists for this order. "
                            + "Use the retry payment API for a failed payment.");
        }


        PaymentEntity payment = PaymentEntity.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .status(PaymentStatus.INITIATED)
                .paymentMethod(requestDTO.getPaymentMethod())
                .build();


        PaymentEntity savedPayment = paymentRepository.save(payment);


        PaymentGatewayResponse gatewayResponse =
                paymentGateway.processPayment(savedPayment);


        savedPayment.setStatus(
                gatewayResponse.getStatus());

        savedPayment.setTransactionReference(
                gatewayResponse.getTransactionReference());


        if (gatewayResponse.getStatus() == PaymentStatus.SUCCESS) {

            order.setStatus(OrderStatus.CONFIRMED);

            orderRepository.save(order);

            log.info("Order confirmed after successful payment. Order ID: {}",
                    order.getId());
        }


        PaymentEntity updatedPayment =
                paymentRepository.save(savedPayment);


        log.info("Payment processed successfully. Payment ID: {}, Order ID: {}, Status: {}, Transaction Reference: {}",
                updatedPayment.getId(),
                order.getId(),
                updatedPayment.getStatus(),
                updatedPayment.getTransactionReference());

        return paymentMapper.toPaymentResponseDTO(updatedPayment);

    }







    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getMyPaymentById(Long id) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();


        String email = authentication.getName();

        log.debug("Fetching payment by ID. Payment ID: {}, User email: {}",
                id,
                email);

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "No user found with email: " + email));


        PaymentEntity payment = paymentRepository
                        .findByIdAndOrderUserId(id, user.getId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "No payment found with id: " + id));


        log.debug("Payment fetched successfully. Payment ID: {}, User ID: {}",
                payment.getId(),
                user.getId());

        return paymentMapper.toPaymentResponseDTO(payment);

    }






    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<PaymentResponseDTO> getMyPayments(Pageable pageable) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        log.debug("Fetching payments for user. User email: {}",
                email);

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "No user found with email: " + email));


        Page<PaymentEntity> paymentPage =
                paymentRepository.findByOrderUserId(user.getId(), pageable);


        List<PaymentResponseDTO> payments =
                paymentPage.getContent()
                        .stream()
                        .map(paymentMapper::toPaymentResponseDTO)
                        .toList();


        log.debug("Payments fetched successfully. User ID: {}, Payment count: {}",
                user.getId(),
                payments.size());

        return new PageResponseDTO<>(
                payments,
                paymentPage.getNumber(),
                paymentPage.getSize(),
                paymentPage.getTotalElements(),
                paymentPage.getTotalPages(),
                paymentPage.isFirst(),
                paymentPage.isLast());

    }






    @Override
    @Transactional
    public PaymentResponseDTO retryPayment(Long id, PaymentRetryRequestDTO requestDTO) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        log.info("Retrying payment. Payment ID: {}, User email: {}",
                id,
                email);

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "No user found with email: " + email));


        PaymentEntity payment = paymentRepository.findByIdAndOrderUserId(id, user.getId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "No payment found with id: " + id));


        if (payment.getStatus() != PaymentStatus.FAILED) {

            throw new PaymentRetryException(
                    "Payment cannot be retried because its current status is "
                            + payment.getStatus());
        }


        OrderEntity order = payment.getOrder();


        if (order.getStatus() != OrderStatus.PENDING) {

            throw new PaymentRetryException(
                    "Payment cannot be retried because order has status "
                            + order.getStatus());
        }


        payment.setPaymentMethod(
                requestDTO.getPaymentMethod());


        log.debug("Processing payment retry through gateway. Payment ID: {}, Order ID: {}, Payment Method: {}",
                payment.getId(),
                order.getId(),
                payment.getPaymentMethod());


        PaymentGatewayResponse gatewayResponse =
                paymentGateway.processPayment(payment);


        payment.setStatus(
                gatewayResponse.getStatus());

        payment.setTransactionReference(
                gatewayResponse.getTransactionReference());

        if (gatewayResponse.getStatus() == PaymentStatus.SUCCESS) {

            order.setStatus(OrderStatus.CONFIRMED);

            orderRepository.save(order);

            log.info("Order confirmed after successful payment retry. Order ID: {}",
                    order.getId());}


        PaymentEntity updatedPayment =
                paymentRepository.save(payment);


        log.info("Payment retry processed. Payment ID: {}, Order ID: {}, Status: {}, Transaction Reference: {}",
                updatedPayment.getId(),
                order.getId(),
                updatedPayment.getStatus(),
                updatedPayment.getTransactionReference());

        return paymentMapper.toPaymentResponseDTO(updatedPayment);

    }












}