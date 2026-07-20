package com.akbar.ecommerce_backend.user.mapper;

import com.akbar.ecommerce_backend.user.dto.user.UserRequestDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserResponseDTO;
import com.akbar.ecommerce_backend.user.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserEntity toEntity(UserRequestDTO requestDTO){

        return UserEntity.builder()
                .firstName(requestDTO.getFirstName())
                .lastName(requestDTO.getLastName())
                .email(requestDTO.getEmail())
                .password(requestDTO.getPassword())
                .phoneNumber(requestDTO.getPhoneNumber())
//                .role(Role.CUSTOMER)
//                .status(UserStatus.ACTIVE)
                .build();

    }

    public UserResponseDTO toResponseDTO(UserEntity userEntity){

        return UserResponseDTO.builder()
                .id(userEntity.getId())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .email(userEntity.getEmail())
                .phoneNumber(userEntity.getPhoneNumber())
                .role(userEntity.getRole())
                .status(userEntity.getStatus())
                .createdAt(userEntity.getCreatedAt())
                .build();
    }
}
