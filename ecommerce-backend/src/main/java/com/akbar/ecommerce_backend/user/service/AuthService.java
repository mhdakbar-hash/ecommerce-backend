package com.akbar.ecommerce_backend.user.service;

import com.akbar.ecommerce_backend.user.dto.auth.ChangePasswordRequestDTO;
import com.akbar.ecommerce_backend.user.dto.auth.LoginRequestDTO;
import com.akbar.ecommerce_backend.user.dto.auth.LoginResponseDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserResponseDTO;

public interface AuthService {

     LoginResponseDTO login(LoginRequestDTO requestDTO);

     UserResponseDTO getCurrentUser();

     void changePassword(ChangePasswordRequestDTO passwordRequestDTO);

     LoginResponseDTO refreshToken(String refreshToken);

     void logout(String refreshToken);

}
