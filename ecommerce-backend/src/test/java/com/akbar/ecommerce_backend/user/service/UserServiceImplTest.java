package com.akbar.ecommerce_backend.user.service;

import com.akbar.ecommerce_backend.exception.EmailAlreadyExistsException;
import com.akbar.ecommerce_backend.exception.ResourceNotFoundException;
import com.akbar.ecommerce_backend.common.PageResponseDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserRequestDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserResponseDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserUpdateRequestDTO;
import com.akbar.ecommerce_backend.user.entity.UserEntity;
import com.akbar.ecommerce_backend.user.enums.Role;
import com.akbar.ecommerce_backend.user.enums.UserStatus;
import com.akbar.ecommerce_backend.user.mapper.UserMapper;
import com.akbar.ecommerce_backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
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


    private UserRequestDTO requestDTO;
    private UserEntity user;
    private UserEntity savedUser;
    private UserResponseDTO responseDTO;


    @BeforeEach
    void setUp(){

        requestDTO = UserRequestDTO.builder()
                .firstName("Muhammad")
                .lastName("Akbar")
                .email("akbar@gmail.com")
                .password("12345")
                .phoneNumber("9399169442")
                .build();

        user = UserEntity.builder()
                .firstName("Muhammad")
                .lastName("Akbar")
                .email("akbar@gmail.com")
                .password("12345")
                .phoneNumber("9399169442")
                .build();

        savedUser = UserEntity.builder()
                .id(1L)
                .firstName("Muhammad")
                .lastName("Akbar")
                .email("akbar@gmail.com")
                .password("12345")
                .phoneNumber("9399169442")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        responseDTO = UserResponseDTO.builder()
                .id(1L)
                .firstName("Muhammad")
                .lastName("Akbar")
                .email("akbar@gmail.com")
                .phoneNumber("9399169442")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();


    }


    @Test
    void registerUser_ShouldRegisterSuccessfully(){

        // Arrange

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

        when(userRepository.findByEmailIgnoreCase(requestDTO.getEmail()))
                .thenReturn(Optional.of(savedUser));

        // Act & Assert

        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.registerUser(requestDTO)
        );


        // Assert & Verify

        assertEquals("Email already exists.", exception.getMessage());

        verify(userRepository).findByEmailIgnoreCase(requestDTO.getEmail());

        verify(userMapper, never()).toEntity(any(UserRequestDTO.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(userMapper, never()).toResponseDTO(any(UserEntity.class));
    }



    @Test
    void getUserById_ShouldReturnUserSuccessfully(){

        // Arrange

        Long userId = 1L;

        when(userRepository.findByIdAndStatusNot(userId, UserStatus.DELETED))
                .thenReturn(Optional.of(savedUser));

        when(userMapper.toResponseDTO(savedUser))
                .thenReturn(responseDTO);


        // Act

        UserResponseDTO result = userService.getUserById(userId);


        // Assert

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Muhammad", result.getFirstName());
        assertEquals("Akbar", result.getLastName());
        assertEquals("akbar@gmail.com", result.getEmail());
        assertEquals(Role.CUSTOMER, result.getRole());
        assertEquals(UserStatus.ACTIVE, result.getStatus());

        // Verify

        verify(userRepository)
                .findByIdAndStatusNot(userId, UserStatus.DELETED);

        verify(userMapper)
                .toResponseDTO(savedUser);

    }



    @Test
    void getUserById_ShouldThrowException_WhenUserNotFound(){

        // Arrange

        Long userId = 9L;

        when(userRepository.findByIdAndStatusNot(userId, UserStatus.DELETED))
                    .thenReturn(Optional.empty());

        // Act & Assert

        ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    ()-> userService.getUserById(userId)
        );

        // Verify

        assertEquals("No user found with id: 9", exception.getMessage());
        verify(userMapper, never()).toResponseDTO(any(UserEntity.class));

    }



    @Test
    void updateUser_ShouldUpdateSuccessfully() {

        // Arrange

        Long userId = 1L;

        UserUpdateRequestDTO updateRequestDTO = UserUpdateRequestDTO.builder()
                .firstName("Arman")
                .lastName("khan")
                .phoneNumber("22222222222").build();

        when(userRepository.findByIdAndStatusNot(userId, UserStatus.DELETED))
                .thenReturn(Optional.of(savedUser));

        when(userMapper.toResponseDTO(savedUser))
                .thenReturn(responseDTO);

        // Act

        UserResponseDTO result = userService.updateUser(userId, updateRequestDTO);

        // Assert

        assertNotNull(result);
        assertEquals(responseDTO, result);

        // Argument Captor

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);

        verify(userRepository).save(userCaptor.capture());

        UserEntity capturedUser = userCaptor.getValue();

        assertEquals("Arman", capturedUser.getFirstName());
        assertEquals("khan", capturedUser.getLastName());
        assertEquals("22222222222", capturedUser.getPhoneNumber());

        // Verify

        verify(userRepository).findByIdAndStatusNot(userId, UserStatus.DELETED);

        verify(userMapper).toResponseDTO(savedUser);

    }



    @Test
    void updateUser_ShouldThrowException_WhenUserNotFound() {

        // Arrange

        Long userId = 8L;

        when(userRepository.findByIdAndStatusNot(userId,UserStatus.DELETED))
                .thenReturn(Optional.empty());

        // Act & Assert

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                ()-> userService.updateUser(userId, new UserUpdateRequestDTO())
        );


        // Assert

        assertEquals("No user found with id: 8", exception.getMessage());

        // Verify

        verify(userRepository)
                .findByIdAndStatusNot(userId, UserStatus.DELETED);

        verify(userRepository, never()).save(any(UserEntity.class));
        
        verify(userMapper, never()).toResponseDTO(any(UserEntity.class));

    }


    @Test
    void deleteUser_ShouldDeleteUserSuccessfully(){

        // Arrange

        Long userId = 5L;

        when(userRepository.findByIdAndStatusNot(userId, UserStatus.DELETED))
                .thenReturn(Optional.of(savedUser));

        // Act

        userService.deleteUser(userId);

        // Argument Captor

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);

        verify(userRepository).save(userCaptor.capture());

        UserEntity captureduser = userCaptor.getValue();

        // Assert

        assertEquals(UserStatus.DELETED,captureduser.getStatus());

        // Verify

        verify(userRepository)
                .findByIdAndStatusNot(userId, UserStatus.DELETED);

    }


    @Test
    void deleteUser_ShouldThrowException_WhenUserNotFound() {

        // Arrange

        Long userId = 6L;

        when(userRepository.findByIdAndStatusNot(userId, UserStatus.DELETED))
                .thenReturn(Optional.empty());

        // Act

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                ()-> userService.deleteUser(userId)
        );

        // Assert

        assertEquals("No user found with id: 6", exception.getMessage());

        // Verify

        verify(userRepository)
                .findByIdAndStatusNot(userId, UserStatus.DELETED);

        verify(userRepository, never()).save(any(UserEntity.class));


    }


    @Test
    void restoreUser_ShouldRestoreUserSuccessfully(){

        // Arrange

        Long userId = 2L;

        when(userRepository.findByIdAndStatus(userId, UserStatus.DELETED))
                .thenReturn(Optional.of(savedUser));

        when(userRepository.save(savedUser))
                .thenReturn(savedUser);

        when(userMapper.toResponseDTO(savedUser))
                .thenReturn(responseDTO);

        // Act

        UserResponseDTO result = userService.restoreUser(userId);

        // Assert

        assertNotNull(result);
        assertEquals(responseDTO, result);
        assertEquals(UserStatus.ACTIVE, savedUser.getStatus());

        // Verify

        verify(userRepository)
                .findByIdAndStatus(userId, UserStatus.DELETED);

        verify(userRepository)
                .save(savedUser);

        verify(userMapper)
                .toResponseDTO(savedUser);

    }


    @Test
    void restoreUser_ShouldThrowException_WhenUserNotFound (){

        // Arrange

        Long userId = 3L;

        when(userRepository.findByIdAndStatus(userId, UserStatus.DELETED))
                .thenReturn(Optional.empty());

        // Act

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                ()-> userService.restoreUser(userId)
        );

        // Assert

        assertEquals("No user found with id: 3", exception.getMessage());

        // Verify

        verify(userRepository)
                .findByIdAndStatus(userId, UserStatus.DELETED);

        verify(userRepository, never()).save(any(UserEntity.class));

        verify(userMapper, never())
                .toResponseDTO(any(UserEntity.class));


    }


    @Test
    void getAllUsers_ShouldReturnUsersSuccessfully() {

        // Arrange

        Pageable pageable = PageRequest.of(0,10);

        Page<UserEntity> userPage = new PageImpl<>(
                List.of(savedUser),
                pageable,
                1
        );

        when(userRepository.findAll(any(Specification.class),
                eq(pageable)))
                .thenReturn(userPage);

        when(userMapper.toResponseDTO(savedUser))
                .thenReturn(responseDTO);

        // Act

        PageResponseDTO<UserResponseDTO> result =
                userService.getAllUsers(null, null, pageable);

        // Assert

        assertNotNull(result);

        assertEquals(List.of(responseDTO), result.getContent());

        assertEquals(0, result.getCurrentPage());
        assertEquals(10, result.getPageSize());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());

        assertTrue(result.isFirst());
        assertTrue(result.isLast());

        // Verify

        verify(userRepository)
                .findAll(any(Specification.class),
               eq(pageable));

        verify(userMapper)
                .toResponseDTO(savedUser);

    }


    @Test
    void getAllUsers_ShouldReturnUsers_WhenKeywordProvided() {

        // Arrange

        String keyword = "Akbar";

        Pageable pageable = PageRequest.of(0, 10);

        Page<UserEntity> userPage = new PageImpl<>(
                List.of(savedUser),
                pageable,
                1
        );

        when(userRepository.findAll(
                any(Specification.class),
                eq(pageable)))
                .thenReturn(userPage);

        when(userMapper.toResponseDTO(savedUser))
                .thenReturn(responseDTO);

        // Act

        PageResponseDTO<UserResponseDTO> result =
                userService.getAllUsers(keyword, null, pageable);

        // Assert

        assertNotNull(result);

        assertEquals(
                List.of(responseDTO),
                result.getContent()
        );

        assertEquals(0, result.getCurrentPage());
        assertEquals(10, result.getPageSize());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());

        assertTrue(result.isFirst());
        assertTrue(result.isLast());

        // Verify

        verify(userRepository)
                .findAll(any(Specification.class),
                        eq(pageable));

        verify(userMapper)
                .toResponseDTO(savedUser);

    }


    @Test
    void getAllUsers_ShouldReturnUsers_WhenStatusProvided() {

        // Arrange

        UserStatus status = UserStatus.ACTIVE;

        Pageable pageable = PageRequest.of(0, 10);

        Page<UserEntity> userPage =
                new PageImpl<>(
                        List.of(savedUser),
                        pageable,
                        1
                );

        when(userRepository.findAll(
                any(Specification.class),
                eq(pageable)))
                .thenReturn(userPage);

        when(userMapper.toResponseDTO(savedUser))
                .thenReturn(responseDTO);


        // Act

        PageResponseDTO<UserResponseDTO> result =
                userService.getAllUsers(
                        null,
                        status,
                        pageable
                );


        // Assert

        assertNotNull(result);

        assertEquals(
                List.of(responseDTO),
                result.getContent()
        );

        assertEquals(0, result.getCurrentPage());
        assertEquals(10, result.getPageSize());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());

        assertTrue(result.isFirst());
        assertTrue(result.isLast());


        // Verify

        verify(userRepository)
                .findAll(
                        any(Specification.class),
                        eq(pageable)
                );

        verify(userMapper)
                .toResponseDTO(savedUser);
    }


    @Test
    void getAllUsers_ShouldReturnUsers_WhenKeywordAndStatusProvided(){

        // Arrange

        String keyword = "Akbar";
        UserStatus status = UserStatus.ACTIVE;

        Pageable pageable = PageRequest.of(0, 10);

        Page<UserEntity> userPage =
                new PageImpl<>(
                        List.of(savedUser),
                        pageable,
                        1
                );

        when(userRepository.findAll(
                any(Specification.class),
                eq(pageable)))
                .thenReturn(userPage);

        when(userMapper.toResponseDTO(savedUser))
                .thenReturn(responseDTO);


        // Act

        PageResponseDTO<UserResponseDTO> result =
                userService.getAllUsers(
                        keyword,
                        status,
                        pageable
                );


        // Assert

        assertNotNull(result);

        assertEquals(
                List.of(responseDTO),
                result.getContent()
        );

        assertEquals(0, result.getCurrentPage());
        assertEquals(10, result.getPageSize());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());

        assertTrue(result.isFirst());
        assertTrue(result.isLast());


        // Verify

        verify(userRepository)
                .findAll(
                        any(Specification.class),
                        eq(pageable)
                );

        verify(userMapper)
                .toResponseDTO(savedUser);


    }



    }
