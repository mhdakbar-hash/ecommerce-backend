package com.akbar.ecommerce_backend.user.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class LoginRequestDTO {


    @Schema(
            description = "Registered email address of the user",
            example = "akbar@gmail.com"
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;


    @Schema(
            description = "User account password",
            example = "Password@123"
    )
    @NotBlank(message = "Password is required")
    private String password;

}
