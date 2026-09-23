package com.akbar.ecommerce_backend.payment.controller;

import com.akbar.ecommerce_backend.common.PageResponseDTO;
import com.akbar.ecommerce_backend.exception.ErrorResponse;
import com.akbar.ecommerce_backend.payment.dto.PaymentRequestDTO;
import com.akbar.ecommerce_backend.payment.dto.PaymentResponseDTO;
import com.akbar.ecommerce_backend.payment.dto.PaymentRetryRequestDTO;
import com.akbar.ecommerce_backend.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor

@Tag(
        name = "Payment Management",
        description = "APIs for managing Payments"
)

public class PaymentController {


    private final PaymentService paymentService;



    @Operation(
            summary = "Initiate payment for an order",
            description = "Initiates a payment attempt for the authenticated user's pending order."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Payment initiated successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = PaymentResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid payment request",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User or order not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Payment cannot be initiated for the current order state",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    public ResponseEntity<PaymentResponseDTO> initiatePayment(
            @Valid
            @RequestBody
            PaymentRequestDTO requestDTO) {

        return ResponseEntity.status(201)
                .body(paymentService.initiatePayment(requestDTO));

    }








    @Operation(
            summary = "Get payment by ID",
            description = "Retrieves a payment belonging to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment fetched successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = PaymentResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Payment or user not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> getMyPaymentById(

            @Parameter(
                    description = "ID of the payment",
                    example = "1"
            )
            @PathVariable Long id) {

        return ResponseEntity.ok(paymentService.getMyPaymentById(id));

    }









    @Operation(
            summary = "Get my payments",
            description = "Retrieves paginated payment history of the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payments fetched successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = PageResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping
    public ResponseEntity<PageResponseDTO<PaymentResponseDTO>> getMyPayments(

            @ParameterObject
            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable) {

        return ResponseEntity.ok(paymentService.getMyPayments(pageable));
    }









    @Operation(
            summary = "Retry failed payment",
            description = "Retries a failed payment for the authenticated user's pending order."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment retry processed successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = PaymentResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid retry request",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Payment or user not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Payment cannot be retried in its current state",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{id}/retry")
    public ResponseEntity<PaymentResponseDTO> retryPayment(

            @Parameter(
                    description = "ID of the failed payment",
                    example = "1"
            )
            @PathVariable Long id,

            @Valid
            @RequestBody
            PaymentRetryRequestDTO requestDTO) {

        return ResponseEntity.ok(paymentService.retryPayment(id, requestDTO));

    }






}