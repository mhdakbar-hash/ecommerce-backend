package com.akbar.ecommerce_backend.user.service;

import com.akbar.ecommerce_backend.user.dto.common.PageResponseDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserRequestDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserResponseDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserUpdateRequestDTO;
import com.akbar.ecommerce_backend.user.enums.UserStatus;
import org.springframework.data.domain.Pageable;


public interface UserService {

    UserResponseDTO registerUser(UserRequestDTO requestDTO);

    UserResponseDTO getUserById(Long id);

    PageResponseDTO<UserResponseDTO> getAllUsers(String keyword, UserStatus status, Pageable pageable);

    UserResponseDTO updateUser(Long id, UserUpdateRequestDTO requestDTO);

    void deleteUser(Long id);

    UserResponseDTO restoreUser(Long id);


}
