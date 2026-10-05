package com.akbar.ecommerce_backend.product.controller;

import com.akbar.ecommerce_backend.common.PageResponseDTO;
import com.akbar.ecommerce_backend.exception.ErrorResponse;
import com.akbar.ecommerce_backend.product.dto.ProductRequestDTO;
import com.akbar.ecommerce_backend.product.dto.ProductResponseDTO;
import com.akbar.ecommerce_backend.product.dto.ProductUpdateRequestDTO;
import com.akbar.ecommerce_backend.product.enums.ProductStatus;
import com.akbar.ecommerce_backend.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/v1/products")
@Tag(
        name = "Product Management",
        description = "APIs for managing Products"
)
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {

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

    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductRequestDTO requestDTO) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(requestDTO));

    }




    @Operation(
            summary = "Get product by ID",
            description = "Retrieves a product by its ID. Deleted products are not returned."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            ))
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(

            @Parameter(
                    description = "Unique ID of the Product",
                    example = "1"
            )
            @PathVariable Long id) {

        return ResponseEntity.ok(productService.getProductById(id));

    }





    @Operation(
            summary = "Get all products",
            description = "Retrieves a paginated list of products with optional keyword and status filters. " +
                    "Deleted products are excluded."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            ))
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping
    public ResponseEntity<PageResponseDTO<ProductResponseDTO>> getAllProducts(

            @Parameter(
                    description = "Search keyword for product name, description, or SKU",
                    example = "iphone"
            )
            @RequestParam(required = false) String keyword,

            @Parameter(
                    description = "Filter products by status",
                    example = "ACTIVE"
            )
            @RequestParam(required = false) ProductStatus status,

            @ParameterObject
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable) {

        return ResponseEntity.ok(
                productService.getAllProducts(keyword, status, pageable));
    }





    @Operation(
            summary = "Update product",
            description = "Updates the editable details of an existing product. " +
                    "SKU and status cannot be changed through this API."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "403", description = "Access denied. ADMIN role required.", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            ))
    })

    @PutMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ProductResponseDTO> updateProduct(

            @Parameter(
                    description = "ID of the product to update",
                    example = "1"
            )
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequestDTO requestDTO) {


        return ResponseEntity.ok(productService.updateProduct(id, requestDTO));

    }





    @Operation(
            summary = "Delete product",
            description = "Soft deletes a product by changing its status to DELETED."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "403", description = "Access denied. ADMIN role required.", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            ))
    })

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Void> deleteProduct(

            @Parameter(
                    description = "ID of the product to delete",
                    example = "1"
            )
            @PathVariable Long id) {

        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();

    }






    @Operation(
            summary = "Restore product",
            description = "Restores a soft-deleted product by changing its status back to ACTIVE."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product restored successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "403", description = "Access denied. ADMIN role required.", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "404", description = "Deleted product not found", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            ))
    })

    @PatchMapping("/{id}/restore")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ProductResponseDTO> restoreProduct(
            @Parameter(
                    description = "ID of the deleted product to restore",
                    example = "1"
            )
            @PathVariable Long id) {

        return ResponseEntity.ok(productService.restoreProduct(id));
    }



}
