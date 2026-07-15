package com.akbar.ecommerce_backend.user.service;

import com.akbar.ecommerce_backend.user.dto.UserRequestDTO;
import com.akbar.ecommerce_backend.user.dto.UserResponseDTO;
import com.akbar.ecommerce_backend.user.dto.UserUpdateRequestDTO;

import java.util.List;

public interface UserService {

    UserResponseDTO registerUser(UserRequestDTO requestDTO);

    UserResponseDTO getUserById(Long id);

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO updateUser(Long id, UserUpdateRequestDTO requestDTO);

    void deleteUser(Long id);


}
