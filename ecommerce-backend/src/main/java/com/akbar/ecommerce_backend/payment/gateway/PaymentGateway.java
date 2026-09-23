package com.akbar.ecommerce_backend.payment.gateway;

import com.akbar.ecommerce_backend.payment.entity.PaymentEntity;

public interface PaymentGateway {

    PaymentGatewayResponse processPayment(PaymentEntity payment);
}
