package com.akbar.ecommerce_backend.category.controller;


import com.akbar.ecommerce_backend.category.dto.CategoryRequestDTO;
import com.akbar.ecommerce_backend.category.dto.CategoryResponseDTO;
import com.akbar.ecommerce_backend.category.dto.CategoryUpdateRequestDTO;
import com.akbar.ecommerce_backend.category.enums.CategoryStatus;
import com.akbar.ecommerce_backend.category.service.CategoryService;
import com.akbar.ecommerce_backend.common.PageResponseDTO;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")

@Tag(
        name = "Category Management",
        description = "APIs for managing Category"
)

public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }



    @Operation(
            summary = "Create a new category",
            description = "Creates a new category after validating the request and checking category name uniqueness."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "403", description = "Access denied. ADMIN role required.", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "409", description = "Category already exists", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            ))
    })

    @PostMapping
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryRequestDTO requestDTO) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategoty(requestDTO));
    }





    @Operation(
            summary = "Get all categories",
            description = "Returns a paginated list of non-deleted categories with optional keyword and status filtering."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories fetched successfully"),
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
    @GetMapping
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<PageResponseDTO<CategoryResponseDTO>> getAllCategories(

            @Parameter(
                    description = "Search keyword for category name or description",
                    example = "elect"
            )
            @RequestParam(required = false) String keyword,


            @Parameter(
                    description = "Filter categories by status",
                    example = "ACTIVE"
            )
            @RequestParam(required = false) CategoryStatus status,

            @ParameterObject
            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable) {

        return ResponseEntity.ok(categoryService.getAllCategories(keyword, status, pageable));

    }






    @Operation(
            summary = "Get category by ID",
            description = "Retrieves a category by its ID. Deleted categories are not returned."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
            ))
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(

            @Parameter(
                    description = "Unique ID of the category",
                    example = "1"
            )
            @PathVariable Long id) {

        return ResponseEntity.ok(categoryService.getCategoryById(id));

    }







    @Operation(
            summary = "Update category",
            description = "Updates an existing category name and description. " +
                    "Deleted categories cannot be updated."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(
                           schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "403", description = "Access denied. ADMIN role required.", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "409", description = "Category name already exists", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            ))
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(

            @Parameter(
                    description = "Unique ID of the category",
                    example = "1"
            )
            @PathVariable Long id,

            @Valid
            @RequestBody CategoryUpdateRequestDTO requestDTO) {

        return ResponseEntity.ok(categoryService.updateCategory(id, requestDTO));

    }







    @Operation(
            summary = "Delete category",
            description = "Soft deletes a category by changing its status to DELETED. " +
                    "A category cannot be deleted if products are associated with it."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "403", description = "Access denied. ADMIN role required.", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "409", description = "Category cannot be deleted because products are associated with it", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            ))
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(

            @Parameter(
                    description = "Unique ID of the category",
                    example = "1"
            )
            @PathVariable Long id) {

        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();

    }








    @Operation(
            summary = "Restore category",
            description = "Restores a deleted category by changing its status from DELETED to ACTIVE."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category restored successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "403", description = "Access denied. ADMIN role required.", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "404", description = "Deleted category not found", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            ))
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/{id}/restore")
    public ResponseEntity<CategoryResponseDTO> restoreCategory(

            @Parameter(
                    description = "Unique ID of the category to restore",
                    example = "1"
            )
            @PathVariable Long id) {

        return ResponseEntity.ok(categoryService.restoreCategory(id));

    }





}
