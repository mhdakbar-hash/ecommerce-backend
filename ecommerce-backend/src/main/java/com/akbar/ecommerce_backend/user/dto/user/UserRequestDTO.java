package com.akbar.ecommerce_backend.user.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserRequestDTO {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters.")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "last name must be between 2 and 50 characters.")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 4, max = 20, message = "Password must be between 4 and 20 characters.")
    private String password;

    @NotBlank(message = "Phone number is required.")
    private String phoneNumber;

}
