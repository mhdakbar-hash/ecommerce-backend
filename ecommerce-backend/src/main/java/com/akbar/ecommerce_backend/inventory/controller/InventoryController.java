package com.akbar.ecommerce_backend.inventory.controller;

import com.akbar.ecommerce_backend.common.PageResponseDTO;
import com.akbar.ecommerce_backend.exception.ErrorResponse;
import com.akbar.ecommerce_backend.inventory.dto.InventoryRequestDTO;
import com.akbar.ecommerce_backend.inventory.dto.InventoryResponseDTO;
import com.akbar.ecommerce_backend.inventory.service.InventoryService;
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
@RequestMapping("/api/v1/inventory")

@Tag(
        name = "Inventory Management",
        description = "APIs for managing Inventory"
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
            @ApiResponse(responseCode = "201", description = "Inventory created successfully"),

            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "403", description = "Access denied. ADMIN role required.", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "409", description = "Inventory already exists for the product", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            ))
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
            @ApiResponse(responseCode = "200", description = "Inventory records retrieved successfully"),

            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            ))
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping
    public ResponseEntity<PageResponseDTO<InventoryResponseDTO>> getAllInventories(

            @ParameterObject
            @PageableDefault(
                    page = 0,
                    size = 20,
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
            @ApiResponse(responseCode = "200", description = "Inventory retrieved successfully"),

            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "404", description = "Inventory not found", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            ))
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
            @ApiResponse(responseCode = "200", description = "Inventory retrieved successfully"),

            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "404", description = "Product or inventory not found", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            ))
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






}