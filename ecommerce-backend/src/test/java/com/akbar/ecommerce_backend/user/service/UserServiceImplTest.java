package com.akbar.ecommerce_backend.user.service;

import com.akbar.ecommerce_backend.exception.EmailAlreadyExistsException;
import com.akbar.ecommerce_backend.user.dto.user.UserRequestDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserResponseDTO;
import com.akbar.ecommerce_backend.user.entity.UserEntity;
import com.akbar.ecommerce_backend.user.enums.Role;
import com.akbar.ecommerce_backend.user.enums.UserStatus;
import com.akbar.ecommerce_backend.user.mapper.UserMapper;
import com.akbar.ecommerce_backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;



    @Test
    void registerUser_ShouldRegisterSuccessfully(){

        // Arrange

        UserRequestDTO requestDTO = UserRequestDTO.builder()
                .firstName("Muhammad")
                .lastName("Akbar")
                .email("akbar@gmail.com")
                .password("12345")
                .phoneNumber("9399169442").build();

        UserEntity user = UserEntity.builder()
                .firstName("Muhammad")
                .lastName("Akbar")
                .email("akbar@gmail.com")
                .password("12345")
                .phoneNumber("9399169442").build();

        UserEntity savedUser = UserEntity.builder()
                .id(1L)
                .firstName("Muhammad")
                .lastName("Akbar")
                .email("akbar@gmail.com")
                .password("12345")
                .phoneNumber("9399169442")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE).build();

        UserResponseDTO responseDTO = UserResponseDTO.builder()
                .id(1L)
                .firstName("Muhammad")
                .lastName("Akbar")
                .email("akbar@gmail.com")
                .phoneNumber("9399169442")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findByEmailIgnoreCase(requestDTO.getEmail()))
                .thenReturn(Optional.empty());

        when(userMapper.toEntity(requestDTO))
                .thenReturn(user);

        when(passwordEncoder.encode(requestDTO.getPassword()))
                .thenReturn("encodedPassword");

        when(userRepository.save(user))
                .thenReturn(savedUser);

        when(userMapper.toResponseDTO(savedUser))
                .thenReturn(responseDTO);

        // Act

        UserResponseDTO result = userService.registerUser(requestDTO);


        // Assert

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Muhammad", result.getFirstName());
        assertEquals("Akbar", result.getLastName());
        assertEquals("akbar@gmail.com", result.getEmail());
        assertEquals(Role.CUSTOMER, result.getRole());
        assertEquals(UserStatus.ACTIVE, result.getStatus());


        verify(userRepository).findByEmailIgnoreCase(requestDTO.getEmail());
        verify(userMapper).toEntity(requestDTO);
        verify(passwordEncoder).encode(requestDTO.getPassword());

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);

        verify(userRepository).save(userCaptor.capture());

        UserEntity capturedUser = userCaptor.getValue();

        assertEquals("encodedPassword", capturedUser.getPassword());
        assertEquals(Role.CUSTOMER, capturedUser.getRole());
        assertEquals(UserStatus.ACTIVE, capturedUser.getStatus());
        assertEquals("akbar@gmail.com", capturedUser.getEmail());

        verify(userMapper).toResponseDTO(savedUser);

    }













    @Test
    void registerUser_ShouldThrowException_WhenEmailAlreadyExists() {

        // Arrange

        UserRequestDTO requestDTO = UserRequestDTO.builder()
                .firstName("Muhammad")
                .lastName("Akbar")
                .email("akbar@gmail.com")
                .password("12345")
                .phoneNumber("9399169442")
                .build();

        UserEntity existingUser = UserEntity.builder()
                .id(1L)
                .firstName("Muhammad")
                .lastName("Akbar")
                .email("akbar@gmail.com")
                .password("encodedPassword")
                .phoneNumber("9399169442")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findByEmailIgnoreCase(requestDTO.getEmail()))
                .thenReturn(Optional.of(existingUser));

        // Act & Assert

        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.registerUser(requestDTO)
        );

        assertEquals("Email already exists.", exception.getMessage());

        // Verify

        verify(userRepository).findByEmailIgnoreCase(requestDTO.getEmail());

        verify(userMapper, never()).toEntity(any(UserRequestDTO.class));

        verify(passwordEncoder, never()).encode(anyString());

        verify(userRepository, never()).save(any(UserEntity.class));

        verify(userMapper, never()).toResponseDTO(any(UserEntity.class));
    }




}
