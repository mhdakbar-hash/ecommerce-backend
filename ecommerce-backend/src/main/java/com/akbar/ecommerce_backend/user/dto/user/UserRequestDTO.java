package com.akbar.ecommerce_backend.user.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserRequestDTO {


    @Schema(
            description = "User's first name",
            example = "Muhammad"
    )
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters.")
    private String firstName;



    @Schema(
            description = "User's last name",
            example = "Akbar"
    )
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "last name must be between 2 and 50 characters.")
    private String lastName;



    @Schema(
            description = "Unique email address",
            example = "akbar@gmail.com"
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address.")
    private String email;



    @Schema(
            description = "User account password",
            example = "Password@123"
    )
    @NotBlank(message = "Password is required.")
    @Size(min = 4, max = 20, message = "Password must be between 4 and 20 characters.")
    private String password;



    @Schema(
            description = "User phone number",
            example = "9876543210"
    )
    @NotBlank(message = "Phone number is required.")
    @Pattern(
            regexp = "^\\d{10}$",
            message = "Phone number must be exactly 10 digits."
    )
    private String phoneNumber;

}
