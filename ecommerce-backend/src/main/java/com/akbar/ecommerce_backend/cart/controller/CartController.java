package com.akbar.ecommerce_backend.cart.controller;

import com.akbar.ecommerce_backend.cart.dto.*;
import com.akbar.ecommerce_backend.cart.service.CartService;
import com.akbar.ecommerce_backend.exception.ErrorResponse;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor

@Tag(
        name = "Cart Management",
        description = "APIs for managing Cart"
)

public class CartController {

    private final CartService cartService;



    @Operation(
            summary = "Add product to cart",
            description = "Adds a product to the authenticated user's shopping cart. If the product already exists in the cart, its quantity is increased."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product added to cart successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload",
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
                    description = "Product or user not found",
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
    @PostMapping("/items")
    public ResponseEntity<CartItemResponseDTO> addItemToCart(

            @Valid
            @RequestBody CartItemRequestDTO requestDTO) {

        return ResponseEntity.ok(cartService.addItemToCart(requestDTO));

    }







    @Operation(
            summary = "Get current user's cart",
            description = "Retrieves the shopping cart of the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cart retrieved successfully"
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
                    description = "User or cart not found",
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
    public ResponseEntity<CartResponseDTO> getMyCart() {

        return ResponseEntity.ok(cartService.getMyCart());

    }








    @Operation(
            summary = "Update cart item quantity",
            description = "Updates the quantity of a product already present in the authenticated user's cart."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cart item quantity updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload or cart item ID",
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
                    description = "Cart item not found",
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
                    ))
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartItemResponseDTO> updateCartItem(

            @Parameter(
                    description = "ID of the cart item",
                    example = "1"
            )
            @PathVariable Long cartItemId,

            @Valid
            @RequestBody CartItemUpdateRequestDTO requestDTO) {

        return ResponseEntity.ok(cartService.updateCartItem(cartItemId,
                        requestDTO));

    }









    @Operation(
            summary = "Remove product from cart",
            description = "Removes a product from the authenticated user's shopping cart."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Cart item removed successfully"
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
                    description = "Cart item not found",
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
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(

            @Parameter(
                    description = "ID of the cart item",
                    example = "1"
            )
            @PathVariable Long cartItemId) {

        cartService.removeCartItem(cartItemId);

        return ResponseEntity.noContent().build();

    }








    @Operation(
            summary = "Clear current user's cart",
            description = "Removes all products from the authenticated user's shopping cart while keeping the cart itself."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Cart cleared successfully"
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
                    description = "Cart not found",
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
    @DeleteMapping
    public ResponseEntity<Void> clearCart() {

        cartService.clearCart();

        return ResponseEntity.noContent().build();

    }







    @Operation(
            summary = "Get current user's cart item count",
            description = "Returns the total quantity of all products currently present in the authenticated user's cart."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cart item count retrieved successfully"
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
                    description = "Cart not found",
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
                    ))
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/count")
    public ResponseEntity<CartCountResponseDTO> getCartCount() {

        return ResponseEntity.ok(cartService.getCartCount());

    }



}
