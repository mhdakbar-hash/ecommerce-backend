package com.akbar.ecommerce_backend.user.dto.auth;

import com.akbar.ecommerce_backend.user.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class LoginResponseDTO {


    @Schema(
            description = "JWT access token used for authenticated requests",
            example = "eyJhbGciOiJIUzI1NiJ9..."
    )
    private String token;

    @Schema(
            description = "Authentication scheme",
            example = "Bearer"
    )
    private String tokenType;


    @Schema(
            description = "Refresh token used to obtain a new access token",
            example = "dGhpcy1pcy1hLXJhbmRvbS1yZWZyZXNoLXRva2Vu..."
    )
    private String refreshToken;


    @Schema(
            description = "Unique identifier of the authenticated user",
            example = "1"
    )
    private Long id;

    @Schema(
            description = "First name of the authenticated user",
            example = "Muhammad"
    )
    private String firstName;

    @Schema(
            description = "Last name of the authenticated user",
            example = "Akbar"
    )
    private String lastName;

    @Schema(
            description = "Registered email address of the authenticated user",
            example = "akbar@gmail.com"
    )
    private String email;

    @Schema(
            description = "Role assigned to the authenticated user",
            example = "ADMIN"
    )
    private Role role;
}
