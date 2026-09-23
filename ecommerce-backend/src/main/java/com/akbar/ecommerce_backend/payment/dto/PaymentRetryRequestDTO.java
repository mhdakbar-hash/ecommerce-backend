package com.akbar.ecommerce_backend.payment.dto;

import com.akbar.ecommerce_backend.payment.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PaymentRetryRequestDTO {


    @NotNull(message = "Payment method is required")
    @Schema(
            description = "Payment method to use for the retry attempt",
            example = "UPI"
    )
    private PaymentMethod paymentMethod;

}