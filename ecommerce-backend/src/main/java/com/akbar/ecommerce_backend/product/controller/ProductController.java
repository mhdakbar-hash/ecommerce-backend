package com.akbar.ecommerce_backend.product.controller;

import com.akbar.ecommerce_backend.exception.ErrorResponse;
import com.akbar.ecommerce_backend.product.dto.ProductRequestDTO;
import com.akbar.ecommerce_backend.product.dto.ProductResponseDTO;
import com.akbar.ecommerce_backend.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@Tag(
        name = "Product Management",
        description = "APIs for managing Products"
)
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService){

        this.productService = productService;
    }


    @Operation(
            summary = "Create a new product",
            description = "Creates a new product after validating the request and checking SKU uniqueness."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )),
            @ApiResponse(responseCode = "409", description = "SKU already exists", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))
    })

    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductRequestDTO requestDTO) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(requestDTO));

    }



}
