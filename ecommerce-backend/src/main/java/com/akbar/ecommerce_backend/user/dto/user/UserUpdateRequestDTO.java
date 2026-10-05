package com.akbar.ecommerce_backend.user.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserUpdateRequestDTO {


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
