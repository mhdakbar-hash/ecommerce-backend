package com.akbar.ecommerce_backend.payment.mapper;

import com.akbar.ecommerce_backend.payment.dto.PaymentResponseDTO;
import com.akbar.ecommerce_backend.payment.entity.PaymentEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponseDTO toPaymentResponseDTO(PaymentEntity entity) {

        return PaymentResponseDTO.builder()
                .id(entity.getId())
                .orderId(entity.getOrder().getId())
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .paymentMethod(entity.getPaymentMethod())
                .transactionReference(
                        entity.getTransactionReference()
                )
                .refundReference(
                        entity.getRefundReference()
                )
                .createdAt(entity.getCreatedAt())
                .build();
    }

}
