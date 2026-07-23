package com.akbar.ecommerce_backend.user.service;

import com.akbar.ecommerce_backend.user.dto.common.PageResponseDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserRequestDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserResponseDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserUpdateRequestDTO;
import org.springframework.data.domain.Pageable;


public interface UserService {

    UserResponseDTO registerUser(UserRequestDTO requestDTO);

    UserResponseDTO getUserById(Long id);

    PageResponseDTO<UserResponseDTO> getAllUsers(String keyword, Pageable pageable);

    UserResponseDTO updateUser(Long id, UserUpdateRequestDTO requestDTO);

    void deleteUser(Long id);


}
