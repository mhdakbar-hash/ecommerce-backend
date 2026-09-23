package com.akbar.ecommerce_backend.payment.gateway;

import com.akbar.ecommerce_backend.payment.entity.PaymentEntity;
import com.akbar.ecommerce_backend.payment.enums.PaymentMethod;
import com.akbar.ecommerce_backend.payment.enums.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockPaymentGateway implements PaymentGateway {


    @Override
    public PaymentGatewayResponse processPayment(PaymentEntity payment) {

        String transactionReference =
                "MOCK-TXN-" + UUID.randomUUID();


        if (payment.getPaymentMethod() == PaymentMethod.CARD) {

            return new PaymentGatewayResponse(
                    PaymentStatus.FAILED,
                    transactionReference
            );
        }

        return new PaymentGatewayResponse(
                PaymentStatus.SUCCESS,
                transactionReference);

    }
}
