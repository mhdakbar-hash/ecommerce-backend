package com.akbar.ecommerce_backend.user.service;

import com.akbar.ecommerce_backend.exception.EmailAlreadyExistsException;
import com.akbar.ecommerce_backend.exception.ResourceNotFoundException;
import com.akbar.ecommerce_backend.user.dto.UserRequestDTO;
import com.akbar.ecommerce_backend.user.dto.UserResponseDTO;
import com.akbar.ecommerce_backend.user.dto.UserUpdateRequestDTO;
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

        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists.");
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

        UserEntity user = userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("No user found with id: " + id));

        return userMapper.toResponseDTO(user);
    }




    @Override
    public List<UserResponseDTO> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponseDTO)
                .toList();
    }


    @Override
    public UserResponseDTO updateUser(Long id, UserUpdateRequestDTO requestDTO) {

        UserEntity user = userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("No user found with id: " + id));

        user.setFirstName(requestDTO.getFirstName());
        user.setLastName(requestDTO.getLastName());
        user.setPhoneNumber(requestDTO.getPhoneNumber());

        userRepository.save(user);
        return userMapper.toResponseDTO(user);

    }

    @Override
    public void deleteUser(Long id) {

    }
}
