package com.akbar.ecommerce_backend.user.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class LoginRequestDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;


    @NotBlank(message = "Password is required")
    private String password;

}
