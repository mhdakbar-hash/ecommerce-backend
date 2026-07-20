package com.akbar.ecommerce_backend.user.dto.auth;

import com.akbar.ecommerce_backend.user.enums.Role;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class LoginResponseDTO {

    private String token;
    private String tokenType;
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
}
