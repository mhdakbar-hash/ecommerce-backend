package com.akbar.ecommerce_backend.user.controller;

import com.akbar.ecommerce_backend.exception.ErrorResponse;
import com.akbar.ecommerce_backend.common.PageResponseDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserRequestDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserResponseDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserUpdateRequestDTO;
import com.akbar.ecommerce_backend.user.enums.UserStatus;
import com.akbar.ecommerce_backend.user.service.UserService;
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
@RequestMapping("/api/v1/users")
@Tag(
        name = "User Management",
        description = "APIs for user registration, retrieval, update, deletion, and restoration"
)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }




    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with CUSTOMER role after validating the request and encrypting the password."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = UserResponseDTO.class
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
                                            "email": "Email must be valid"
                                          }
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "EmailAlreadyExists",
                                    value = """
                                        {
                                          "status": 409,
                                          "message": "Email already exists",
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

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody UserRequestDTO requestDTO){

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.registerUser(requestDTO));
    }






    @Operation(
            summary = "Get all users",
            description = "Returns a paginated list of users with optional search and status filtering."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
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
                                    name = "InvalidPagination",
                                    value = """
                                        {
                                          "status": 400,
                                          "message": "Invalid pagination request",
                                          "timeStamp": "2026-07-27T14:20:15",
                                          "errors": {}
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
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
                    description = "Access denied",
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
            )
    })

    @GetMapping
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<PageResponseDTO<UserResponseDTO>> getAllUsers(

            @Parameter(
                    description = "Search users by first name, last name, email or phone number",
                    example = "akbar"
            )
            @RequestParam(required = false) String keyword,


            @Parameter(
                    description = "Filter users by account status",
                    example = "ACTIVE"
            )
            @RequestParam(required = false) UserStatus status,


            @ParameterObject
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )

            Pageable pageable){

        return ResponseEntity.ok(userService.getAllUsers(keyword, status, pageable));
    }





    @Operation(
            summary = "Get user by ID",
            description = "Retrieves a user using the provided user ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = UserResponseDTO.class
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
                                    name = "UserNotFound",
                                    value = """
                                        {
                                          "status": 404,
                                          "message": "User not found",
                                          "timeStamp": "2026-07-27T14:20:15",
                                          "errors": {}
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
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
                    description = "Access denied",
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
            )
    })

    @GetMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<UserResponseDTO> getUserById(

            @Parameter(
                    description = "Unique ID of the user",
                    example = "1"
            )
            @PathVariable Long id){

        return ResponseEntity.ok(userService.getUserById(id));
    }






    @Operation(
            summary = "Update user",
            description = "Updates the details of an existing user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = UserResponseDTO.class
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
                                            "phone": "Phone number must be valid"
                                          }
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
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
                    description = "Access denied",
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
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "UserNotFound",
                                    value = """
                                        {
                                          "status": 404,
                                          "message": "User not found",
                                          "timeStamp": "2026-07-27T14:20:15",
                                          "errors": {}
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "EmailAlreadyExists",
                                    value = """
                                        {
                                          "status": 409,
                                          "message": "Email already exists",
                                          "timeStamp": "2026-07-27T14:20:15",
                                          "errors": {}
                                        }
                                        """
                            )
                    )
            )
    })

    @PutMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<UserResponseDTO> updateUser(

            @Parameter(
                    description = "Unique ID of the user",
                    example = "1"
            )
            @PathVariable Long id,
             @Valid @RequestBody UserUpdateRequestDTO requestDTO){

        return ResponseEntity.ok(userService.updateUser(id, requestDTO));

    }




    @Operation(
            summary = "Delete user",
            description = "Deletes a user using the provided user ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "User deleted successfully",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(
                                    type = "string",
                                    example = "User deleted successfully."
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
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
                    description = "Access denied",
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
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "UserNotFound",
                                    value = """
                                        {
                                          "status": 404,
                                          "message": "User not found",
                                          "timeStamp": "2026-07-27T14:20:15",
                                          "errors": {}
                                        }
                                        """
                            )
                    )
            )
    })

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Void> deleteUser(

            @Parameter(
                    description = "Unique ID of the user",
                    example = "1"
            )
            @PathVariable Long id){

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }





    @Operation(
            summary = "Restore a deleted user",
            description = "Restores a soft-deleted user by changing the account status from DELETED to ACTIVE."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User restored successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = UserResponseDTO.class
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
                    description = "Access denied",
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
                    description = "Deleted user not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "DeletedUserNotFound",
                                    value = """
                                        {
                                          "status": 404,
                                          "message": "Deleted user not found",
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
    public ResponseEntity<UserResponseDTO> restoreUser(

            @Parameter(
                    description = "ID of the deleted user to restore",
                    example = "1"
            )
            @PathVariable Long id){

        return ResponseEntity.ok(userService.restoreUser(id));
    }



}
