package com.akbar.ecommerce_backend.payment.gateway;

import com.akbar.ecommerce_backend.payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PaymentGatewayResponse {

    private PaymentStatus status;

    private String transactionReference;

}