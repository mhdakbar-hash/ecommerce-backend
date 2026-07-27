package com.akbar.ecommerce_backend.user.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ChangePasswordRequestDTO {


    @Schema(
            description = "Current password of the authenticated user",
            example = "OldPassword@123"
    )
    @NotBlank(message = "Old password is required.")
    private String oldPassword;



    @Schema(
            description = "New password for the user account",
            example = "NewPassword@123"
    )
    @NotBlank(message = "New password is required.")
    private String newPassword;



    @Schema(
            description = "Confirmation of the new password. Must match the new password",
            example = "NewPassword@123"
    )
    @NotBlank(message = "Confirm password is required.")
    private String confirmPassword;
}
