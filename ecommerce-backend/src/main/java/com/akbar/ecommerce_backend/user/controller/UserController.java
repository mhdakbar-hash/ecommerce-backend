package com.akbar.ecommerce_backend.user.controller;

import com.akbar.ecommerce_backend.exception.ErrorResponse;
import com.akbar.ecommerce_backend.user.dto.common.PageResponseDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserRequestDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserResponseDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserUpdateRequestDTO;
import com.akbar.ecommerce_backend.user.enums.UserStatus;
import com.akbar.ecommerce_backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/users")
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
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )),
            @ApiResponse(responseCode = "409", description = "Email already exists", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            ))
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
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            ))
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
                    size = 20,
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
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            ))
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
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            ))
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
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            ))
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
            @ApiResponse(responseCode = "200", description = "User restored successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Deleted user not found", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class))
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
