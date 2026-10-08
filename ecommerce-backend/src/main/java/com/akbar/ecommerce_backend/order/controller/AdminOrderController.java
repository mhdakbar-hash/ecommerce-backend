package com.akbar.ecommerce_backend.order.controller;

import com.akbar.ecommerce_backend.common.PageResponseDTO;
import com.akbar.ecommerce_backend.exception.ErrorResponse;
import com.akbar.ecommerce_backend.order.dto.AdminOrderResponseDTO;
import com.akbar.ecommerce_backend.order.dto.OrderStatusUpdateRequestDTO;
import com.akbar.ecommerce_backend.order.enums.OrderStatus;
import com.akbar.ecommerce_backend.order.service.AdminOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor

@Tag(
        name = "Admin Order Management",
        description = "Administrative APIs for managing customer orders"
)

public class AdminOrderController {


    private final AdminOrderService adminOrderService;




    @Operation(
            summary = "Get all orders",
            description = "Returns all customer orders with pagination and optional filtering."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Orders fetched successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = PageResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination, sorting, filter, or date/time parameters",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "status": 400,
                                              "message": "Invalid sort field: invalidField",
                                              "timeStamp": "2026-07-27T14:20:15"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "status": 401,
                                              "message": "Authentication is required.",
                                              "timeStamp": "2026-07-27T14:20:15"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Admin access required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "status": 403,
                                              "message": "Access denied.",
                                              "timeStamp": "2026-07-27T14:20:15"
                                            }
                                            """
                            )
                    )
            )
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping
    public ResponseEntity<PageResponseDTO<AdminOrderResponseDTO>> getAllOrders(

            @ParameterObject
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable,

            @Parameter(
                    description = "Filter orders by status",
                    example = "CONFIRMED"
            )
            @RequestParam(required = false)
            OrderStatus status,

            @Parameter(
                    description = "Search by order number",
                    example = "ORD-"
            )
            @RequestParam(required = false)
            String orderNumber,

            @Parameter(
                    description = "Filter by customer email",
                    example = "customer@gmail.com"
            )
            @RequestParam(required = false)
            String userEmail,

            @Parameter(
                    description = "Filter orders created from this date/time",
                    example = "2026-09-01T00:00:00"
            )
            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime startDate,

            @Parameter(
                    description = "Filter orders created until this date/time",
                    example = "2026-09-30T23:59:59"
            )
            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime endDate) {


        return ResponseEntity.ok(adminOrderService.getAllOrders(
                        pageable,
                        status,
                        orderNumber,
                        userEmail,
                        startDate,
                        endDate)
        );
    }








    @Operation(
            summary = "Get order details",
            description = "Returns detailed information about any customer order for authorized administrators."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Order fetched successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = AdminOrderResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "status": 401,
                                              "message": "Authentication is required.",
                                              "timeStamp": "2026-07-27T14:20:15"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Admin access required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "status": 403,
                                              "message": "Access denied.",
                                              "timeStamp": "2026-07-27T14:20:15"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "status": 404,
                                              "message": "Order not found",
                                              "timeStamp": "2026-07-27T14:20:15"
                                            }
                                            """
                            )
                    )
            )
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}")
    public ResponseEntity<AdminOrderResponseDTO> getOrderById(

            @Parameter(
                    description = "ID of the order",
                    example = "101"
            )
            @PathVariable Long id) {

        return ResponseEntity.ok(adminOrderService.getOrderById(id));
    }










    @Operation(
            summary = "Update order status",
            description = "Updates the status of a customer order using valid order lifecycle transitions."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Order status updated successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = AdminOrderResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "status": 400,
                                              "message": "Validation failed",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {
                                                "status": "Status is required"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "status": 401,
                                              "message": "Authentication is required.",
                                              "timeStamp": "2026-07-27T14:20:15"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Admin access required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "status": 403,
                                              "message": "Access denied.",
                                              "timeStamp": "2026-07-27T14:20:15"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "status": 404,
                                              "message": "Order not found",
                                              "timeStamp": "2026-07-27T14:20:15"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Invalid order status transition",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "status": 409,
                                              "message": "Invalid order status transition",
                                              "timeStamp": "2026-07-27T14:20:15"
                                            }
                                            """
                            )
                    )
            )
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/{id}/status")
    public ResponseEntity<AdminOrderResponseDTO> updateOrderStatus(

            @Parameter(
                    description = "ID of the order",
                    example = "101"
            )
            @PathVariable Long id,

            @Valid
            @RequestBody
            OrderStatusUpdateRequestDTO requestDTO) {

        return ResponseEntity.ok(adminOrderService.updateOrderStatus(id, requestDTO));
    }




}