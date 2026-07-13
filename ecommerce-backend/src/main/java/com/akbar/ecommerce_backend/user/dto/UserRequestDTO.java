package com.akbar.ecommerce_backend.user.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserRequestDTO {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phoneNumber;

}
