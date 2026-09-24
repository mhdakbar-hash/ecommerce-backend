package com.akbar.ecommerce_backend.payment.service;

import com.akbar.ecommerce_backend.common.PageResponseDTO;
import com.akbar.ecommerce_backend.payment.dto.PaymentRequestDTO;
import com.akbar.ecommerce_backend.payment.dto.PaymentResponseDTO;
import com.akbar.ecommerce_backend.payment.dto.PaymentRetryRequestDTO;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    PaymentResponseDTO initiatePayment(PaymentRequestDTO requestDTO);

    PaymentResponseDTO getMyPaymentById(Long id);

    PageResponseDTO<PaymentResponseDTO> getMyPayments(Pageable pageable);

    PaymentResponseDTO retryPayment(Long id, PaymentRetryRequestDTO requestDTO);

    PaymentResponseDTO refundPayment(Long id);

}