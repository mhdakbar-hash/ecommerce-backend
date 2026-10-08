package com.akbar.ecommerce_backend.order.controller;

import com.akbar.ecommerce_backend.common.PageResponseDTO;
import com.akbar.ecommerce_backend.exception.ErrorResponse;
import com.akbar.ecommerce_backend.order.dto.OrderResponseDTO;
import com.akbar.ecommerce_backend.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor

@Tag(
        name = "Order Management",
        description = "APIs for creating, retrieving, and cancelling orders"
)

public class OrderController {

    private final OrderService orderService;



    @Operation(
            summary = "Create order from current user's cart",
            description = "Creates an order using the authenticated user's current cart."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Order created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = OrderResponseDTO.class
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
                    responseCode = "404",
                    description = "User, cart, or inventory not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "status": 404,
                                              "message": "Cart not found",
                                              "timeStamp": "2026-07-27T14:20:15"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Cart is empty or inventory stock is insufficient",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "status": 409,
                                              "message": "Cart is empty",
                                              "timeStamp": "2026-07-27T14:20:15"
                                            }
                                            """
                            )
                    )
            )
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder() {

        return ResponseEntity.status(201)
                .body(orderService.createOrder());
    }








    @Operation(
            summary = "Get current user's orders",
            description = "Returns paginated orders belonging to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Orders retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = PageResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination or sort parameters",
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
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "status": 404,
                                              "message": "User not found",
                                              "timeStamp": "2026-07-27T14:20:15"
                                            }
                                            """
                            )
                    )
            )
    })

    @GetMapping
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<PageResponseDTO<OrderResponseDTO>> getMyOrders(

            @ParameterObject
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable) {

        return ResponseEntity.ok(
                orderService.getMyOrders(pageable));
    }








    @Operation(
            summary = "Get current user's order by ID",
            description = "Returns a specific order only if it belongs to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Order retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = OrderResponseDTO.class
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
                    responseCode = "404",
                    description = "Order or user not found",
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
    public ResponseEntity<OrderResponseDTO> getMyOrderById(
            @Parameter(
                    description = "ID of the order",
                    example = "1"
            )
            @PathVariable Long id) {

        return ResponseEntity.ok(orderService.getMyOrderById(id));

    }







    @Operation(
            summary = "Cancel current user's order",
            description = "Cancels an order belonging to the authenticated user when its current status allows cancellation."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Order cancelled successfully",
                    content = @Content(
                            mediaType = "application/json"
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
                    responseCode = "404",
                    description = "User or order not found",
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
                    description = "Order cannot be cancelled in its current state",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "status": 409,
                                              "message": "Order cannot be cancelled in its current state",
                                              "timeStamp": "2026-07-27T14:20:15"
                                            }
                                            """
                            )
                    )
            )
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelMyOrder(
            @Parameter(
                    description = "ID of the order to cancel",
                    example = "1"
            )
            @PathVariable Long id) {

        return ResponseEntity.ok(orderService.cancelMyOrder(id));
    }


}