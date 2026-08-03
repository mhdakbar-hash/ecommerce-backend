package com.akbar.ecommerce_backend.user.service;

import com.akbar.ecommerce_backend.security.JwtService;
import com.akbar.ecommerce_backend.user.dto.auth.LoginRequestDTO;
import com.akbar.ecommerce_backend.user.dto.auth.LoginResponseDTO;
import com.akbar.ecommerce_backend.user.entity.UserEntity;
import com.akbar.ecommerce_backend.user.enums.Role;
import com.akbar.ecommerce_backend.user.enums.UserStatus;
import com.akbar.ecommerce_backend.user.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private LoginRequestDTO requestDTO;
    private UserEntity user;
    
    @BeforeEach
    void setUp(){
        
        requestDTO = LoginRequestDTO.builder()
                .email("akbar@gmail.com")
                .password("12345")
                .build();

        user = UserEntity.builder().id(1L)
                .firstName("Muhammad")
                .lastName("Akbar")
                .email("akbar@gmail.com")
                .password("12345")
                .phoneNumber("9399169442")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
    }


    @Test
    void login_ShouldLoginSuccessfully(){

        // Arrange

        Authentication authentication = mock(Authentication.class);

        CustomUserDetails customUserDetails = mock(CustomUserDetails.class);

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(customUserDetails);

        when(customUserDetails.getUser())
                .thenReturn(user);

        when(jwtService.generateToken(customUserDetails))
                .thenReturn("jwt-token");


        // Act

        LoginResponseDTO result = authService.login(requestDTO);


        // Assert

        assertNotNull(result);

        assertEquals("jwt-token", result.getToken());
        assertEquals("Bearer", result.getTokenType());

        assertEquals(user.getId(), result.getId());
        assertEquals(user.getFirstName(), result.getFirstName());
        assertEquals(user.getLastName(), result.getLastName());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getRole(), result.getRole());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(customUserDetails);


    }


    @Test
    void login_ShouldThrowException_WhenCredentialsAreInvalid() {

        // Arrange

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert

        BadCredentialsException exception =
                assertThrows(BadCredentialsException.class,
                        ()-> authService.login(requestDTO)
                        );

        // Assert

        assertEquals("Invalid credentials", exception.getMessage());

        // Verify

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any());


    }


}
