package com.akbar.ecommerce_backend.user.dto.user;

import com.akbar.ecommerce_backend.user.enums.Role;
import com.akbar.ecommerce_backend.user.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserResponseDTO {

    @Schema(
            description = "Unique identifier of the user",
            example = "1"
    )
    private Long id;

    @Schema(
            description = "First name of the user",
            example = "Muhammad"
    )
    private String firstName;

    @Schema(
            description = "Last name of the user",
            example = "Akbar"
    )
    private String lastName;

    @Schema(
            description = "Registered email address of the user",
            example = "akbar@gmail.com"
    )
    private String email;

    @Schema(
            description = "Registered phone number of the user",
            example = "9876543210"
    )
    private String phoneNumber;

    @Schema(
            description = "Role assigned to the user",
            example = "CUSTOMER"
    )
    private Role role;

    @Schema(
            description = "Current account status of the user",
            example = "ACTIVE"
    )
    private UserStatus status;

    @Schema(
            description = "Date and time when the user account was created",
            example = "2026-07-27T10:30:45"
    )
    private LocalDateTime createdAt;

}