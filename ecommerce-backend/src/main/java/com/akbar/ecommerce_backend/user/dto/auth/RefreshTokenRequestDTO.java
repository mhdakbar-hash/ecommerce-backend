package com.akbar.ecommerce_backend.user.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RefreshTokenRequestDTO {

    @NotBlank(message = "Refresh token is required")
    @Schema(
            description = "Refresh token used to generate a new access token",
            example = "dGhpcy1pcy1hLXJlZnJlc2gtdG9rZW4..."
    )
    private String refreshToken;

}