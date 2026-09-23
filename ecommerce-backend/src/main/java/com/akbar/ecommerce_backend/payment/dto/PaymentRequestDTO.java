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
public class PaymentRequestDTO {


    @Schema(
            description = "ID of the order for which payment is being initiated",
            example = "101"
    )
    @NotNull(message = "Order ID is required")
    private Long orderId;


    @Schema(
            description = "Payment method selected by the customer",
            example = "UPI",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

}