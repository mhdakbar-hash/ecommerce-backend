package com.akbar.ecommerce_backend.user.service;

import com.akbar.ecommerce_backend.user.dto.auth.LoginRequestDTO;
import com.akbar.ecommerce_backend.user.dto.auth.LoginResponseDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserResponseDTO;

public interface AuthService {

     LoginResponseDTO login(LoginRequestDTO requestDTO);

     UserResponseDTO getCurrentUser();
}
