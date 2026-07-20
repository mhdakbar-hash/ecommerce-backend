package com.akbar.ecommerce_backend.user.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserUpdateRequestDTO {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters.")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "last name must be between 2 and 50 characters.")
    private String lastName;

    @NotBlank(message = "Phone number is required.")
    private String phoneNumber;

}
