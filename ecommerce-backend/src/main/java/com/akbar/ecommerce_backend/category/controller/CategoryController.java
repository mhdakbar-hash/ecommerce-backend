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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")

@Tag(
        name = "Category Management",
        description = "APIs for creating, retrieving, updating, deleting, and restoring categories"
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
            @ApiResponse(
                    responseCode = "201",
                    description = "Category created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = CategoryResponseDTO.class
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
                                                "name": "Category name is required"
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
                    responseCode = "409",
                    description = "Category already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "CategoryAlreadyExists",
                                    value = """
                                            {
                                              "status": 409,
                                              "message": "Category already exists",
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

    @PostMapping
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryRequestDTO requestDTO) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategory(requestDTO));
    }





    @Operation(
            summary = "Get all categories",
            description = "Returns a paginated list of non-deleted categories with optional keyword and status filtering."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categories fetched successfully",
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
                    size = 10,
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
            @ApiResponse(
                    responseCode = "200",
                    description = "Category retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = CategoryResponseDTO.class
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
                    description = "Category not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "CategoryNotFound",
                                    value = """
                                            {
                                              "status": 404,
                                              "message": "Category not found",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {}
                                            }
                                            """
                            )
                    )
            )
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
            @ApiResponse(
                    responseCode = "200",
                    description = "Category updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = CategoryResponseDTO.class
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
                                                "name": "Category name is required"
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
                    description = "Category not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "CategoryNotFound",
                                    value = """
                                            {
                                              "status": 404,
                                              "message": "Category not found",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {}
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Category name already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "CategoryAlreadyExists",
                                    value = """
                                            {
                                              "status": 409,
                                              "message": "Category name already exists",
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
            @ApiResponse(
                    responseCode = "204",
                    description = "Category deleted successfully",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(
                                    type = "string",
                                    example = "Category deleted successfully."
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
                    description = "Category not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "CategoryNotFound",
                                    value = """
                                            {
                                              "status": 404,
                                              "message": "Category not found",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {}
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Category cannot be deleted because products are associated with it",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "CategoryInUse",
                                    value = """
                                            {
                                              "status": 409,
                                              "message": "Category cannot be deleted because products are associated with it",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {}
                                            }
                                            """
                            )
                    )
            )
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
            @ApiResponse(
                    responseCode = "200",
                    description = "Category restored successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = CategoryResponseDTO.class
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
                    description = "Deleted category not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "DeletedCategoryNotFound",
                                    value = """
                                            {
                                              "status": 404,
                                              "message": "Deleted category not found",
                                              "timeStamp": "2026-07-27T14:20:15",
                                              "errors": {}
                                            }
                                            """
                            )
                    )
            )
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
