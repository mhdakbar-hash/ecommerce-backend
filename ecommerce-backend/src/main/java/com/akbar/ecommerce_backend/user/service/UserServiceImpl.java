package com.akbar.ecommerce_backend.user.service;

import com.akbar.ecommerce_backend.user.dto.UserRequestDTO;
import com.akbar.ecommerce_backend.user.dto.UserResponseDTO;
import com.akbar.ecommerce_backend.user.entity.UserEntity;
import com.akbar.ecommerce_backend.user.enums.Role;
import com.akbar.ecommerce_backend.user.enums.UserStatus;
import com.akbar.ecommerce_backend.user.mapper.UserMapper;
import com.akbar.ecommerce_backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper){
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }



    @Override
    public UserResponseDTO registerUser(UserRequestDTO requestDTO) {

        String email = requestDTO.getEmail().trim().toLowerCase();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists.");
        }

        UserEntity user= userMapper.toEntity(requestDTO);

        user.setEmail(email);
        user.setRole(Role.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        UserEntity savedUser = userRepository.save(user);

        return userMapper.toResponseDTO(savedUser);
    }




    @Override
    public UserResponseDTO getUserById(Long id) {
        return null;
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return List.of();
    }

    @Override
    public UserResponseDTO updateUser(Long id, UserRequestDTO requestDTO) {
        return null;
    }

    @Override
    public void deleteUser(Long id) {

    }
}
