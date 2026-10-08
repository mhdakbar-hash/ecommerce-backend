package com.akbar.ecommerce_backend.inventory.controller;

import com.akbar.ecommerce_backend.common.PageResponseDTO;
import com.akbar.ecommerce_backend.exception.ErrorResponse;
import com.akbar.ecommerce_backend.inventory.dto.InventoryRequestDTO;
import com.akbar.ecommerce_backend.inventory.dto.InventoryResponseDTO;
import com.akbar.ecommerce_backend.inventory.dto.InventoryStockRequestDTO;
import com.akbar.ecommerce_backend.inventory.dto.InventoryUpdateRequestDTO;
import com.akbar.ecommerce_backend.inventory.service.InventoryService;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")

@Tag(
        name = "Inventory Management",
        description = "APIs for creating, retrieving, updating, and managing inventory stock"
)

public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }




    @Operation(
            summary = "Create inventory",
            description = "Creates an inventory record for a product with the specified initial available quantity."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Inventory created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = InventoryResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "ValidationError",
                                    value = """
                                            {
                                              "status": 400,
                                              "message": "Validation Failed",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {
                                                "availableQuantity": "Available quantity must not be negative"
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
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "status": 401,
                                              "message": "Authentication is required.",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {}
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied. ADMIN role required.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "AccessDenied",
                                    value = """
                                            {
                                              "status": 403,
                                              "message": "Access denied.",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {}
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "ProductNotFound",
                                    value = """
                                            {
                                              "status": 404,
                                              "message": "Product not found",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {}
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Inventory already exists for the product",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InventoryAlreadyExists",
                                    value = """
                                            {
                                              "status": 409,
                                              "message": "Inventory already exists for the product",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {}
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InternalServerError",
                                    value = """
                                            {
                                              "status": 500,
                                              "message": "Something went wrong. Please try again later.",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {}
                                            }
                                            """
                            )
                    )
            )
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    public ResponseEntity<InventoryResponseDTO> createInventory(@Valid @RequestBody InventoryRequestDTO requestDTO) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.createInventory(requestDTO));
    }








    @Operation(
            summary = "Get all inventories",
            description = "Retrieves all inventory records with pagination and sorting."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventory records retrieved successfully",
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
                                    name = "InvalidPaginationOrSort",
                                    value = """
                                            {
                                              "status": 400,
                                              "message": "Invalid pagination or sort parameters",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {}
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
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "status": 401,
                                              "message": "Authentication is required.",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {}
                                            }
                                            """
                            )
                    )
            )
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping
    public ResponseEntity<PageResponseDTO<InventoryResponseDTO>> getAllInventories(

            @ParameterObject
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable) {

        return ResponseEntity.ok(inventoryService.getAllInventories(pageable));

    }









    @Operation(
            summary = "Get inventory by ID",
            description = "Retrieves an inventory record using its unique ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventory retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = InventoryResponseDTO.class
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
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "status": 401,
                                              "message": "Authentication is required.",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {}
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inventory not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InventoryNotFound",
                                    value = """
                                            {
                                              "status": 404,
                                              "message": "Inventory not found",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {}
                                            }
                                            """
                            )
                    )
            )
    })

    @GetMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<InventoryResponseDTO> getInventoryById(

            @Parameter(
                    description = "Unique ID of the inventory record",
                    example = "1"
            )
            @PathVariable Long id) {

        return ResponseEntity.ok(inventoryService.getInventoryById(id));

    }








    @Operation(
            summary = "Get inventory by product ID",
            description = "Retrieves the inventory record associated with a specific product."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventory retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = InventoryResponseDTO.class
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
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "status": 401,
                                              "message": "Authentication is required.",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {}
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product or inventory not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "ProductOrInventoryNotFound",
                                    value = """
                                            {
                                              "status": 404,
                                              "message": "Product or inventory not found",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {}
                                            }
                                            """
                            )
                    )
            )
    })

    @GetMapping("/product/{productId}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<InventoryResponseDTO> getInventoryByProductId(

            @Parameter(
                    description = "Unique ID of the product",
                    example = "1"
            )
            @PathVariable Long productId) {

        return ResponseEntity.ok(inventoryService.getInventoryByProductId(productId));

    }







    @Operation(
            summary = "Update inventory",
            description = "Updates the available quantity of an existing inventory record."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventory updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = InventoryResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "ValidationError",
                                    value = """
                                            {
                                              "status": 400,
                                              "message": "Validation Failed",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {
                                                "availableQuantity": "Available quantity must not be negative"
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
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "status": 401,
                                              "message": "Authentication is required.",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {}
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied. ADMIN role required.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "AccessDenied",
                                    value = """
                                            {
                                              "status": 403,
                                              "message": "Access denied.",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {}
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inventory not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InventoryNotFound",
                                    value = """
                                            {
                                              "status": 404,
                                              "message": "Inventory not found",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {}
                                            }
                                            """
                            )
                    )
            )
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/{id}")
    public ResponseEntity<InventoryResponseDTO> updateInventory(

            @Parameter(
                    description = "Unique ID of the inventory record",
                    example = "1"
            )
            @PathVariable Long id,

            @Valid
            @RequestBody InventoryUpdateRequestDTO requestDTO) {

        return ResponseEntity.ok(inventoryService.updateInventory(id, requestDTO));

    }







    @Operation(
            summary = "Add stock",
            description = "Adds the specified quantity to the available stock of an existing inventory record."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Stock added successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = InventoryResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "ValidationError",
                                    value = """
                                            {
                                              "status": 400,
                                              "message": "Validation Failed",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {
                                                "quantity": "Quantity must be greater than zero"
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
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "status": 401,
                                              "message": "Authentication is required.",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {}
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied. ADMIN role required.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "AccessDenied",
                                    value = """
                                            {
                                              "status": 403,
                                              "message": "Access denied.",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {}
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inventory not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InventoryNotFound",
                                    value = """
                                            {
                                              "status": 404,
                                              "message": "Inventory not found",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {}
                                            }
                                            """
                            )
                    )
            )
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/{id}/add-stock")
    public ResponseEntity<InventoryResponseDTO> addStock(

            @Parameter(
                    description = "Unique ID of the inventory record",
                    example = "1"
            )
            @PathVariable Long id,

            @Valid
            @RequestBody InventoryStockRequestDTO requestDTO) {

        return ResponseEntity.ok(inventoryService.addStock(id, requestDTO));

    }







    @Operation(
            summary = "Remove stock",
            description = "Removes the specified quantity from the available stock of an existing inventory record."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Stock removed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = InventoryResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "ValidationError",
                                    value = """
                                            {
                                              "status": 400,
                                              "message": "Validation Failed",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {
                                                "quantity": "Quantity must be greater than zero"
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
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "status": 401,
                                              "message": "Authentication is required.",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {}
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied. ADMIN role required.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "AccessDenied",
                                    value = """
                                            {
                                              "status": 403,
                                              "message": "Access denied.",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {}
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inventory not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InventoryNotFound",
                                    value = """
                                            {
                                              "status": 404,
                                              "message": "Inventory not found",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {}
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Insufficient available stock",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InsufficientStock",
                                    value = """
                                            {
                                              "status": 409,
                                              "message": "Insufficient available stock",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {}
                                            }
                                            """
                            )
                    )
            )
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/{id}/remove-stock")
    public ResponseEntity<InventoryResponseDTO> removeStock(

            @Parameter(
                    description = "Unique ID of the inventory record",
                    example = "1"
            )
            @PathVariable Long id,

            @Valid
            @RequestBody InventoryStockRequestDTO requestDTO) {

        return ResponseEntity.ok(inventoryService.removeStock(id, requestDTO));

    }






}