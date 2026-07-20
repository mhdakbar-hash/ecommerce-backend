package com.akbar.ecommerce_backend.user.service;

import com.akbar.ecommerce_backend.user.dto.user.UserRequestDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserResponseDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserUpdateRequestDTO;

import java.util.List;

public interface UserService {

    UserResponseDTO registerUser(UserRequestDTO requestDTO);

    UserResponseDTO getUserById(Long id);

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO updateUser(Long id, UserUpdateRequestDTO requestDTO);

    void deleteUser(Long id);


}
