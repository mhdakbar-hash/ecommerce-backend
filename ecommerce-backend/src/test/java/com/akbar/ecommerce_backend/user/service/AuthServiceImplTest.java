package com.akbar.ecommerce_backend.user.service;

import com.akbar.ecommerce_backend.exception.InvalidOldPasswordException;
import com.akbar.ecommerce_backend.exception.PasswordMismatchException;
import com.akbar.ecommerce_backend.exception.SamePasswordException;
import com.akbar.ecommerce_backend.security.JwtService;
import com.akbar.ecommerce_backend.user.dto.auth.ChangePasswordRequestDTO;
import com.akbar.ecommerce_backend.user.dto.auth.LoginRequestDTO;
import com.akbar.ecommerce_backend.user.dto.auth.LoginResponseDTO;
import com.akbar.ecommerce_backend.user.entity.UserEntity;
import com.akbar.ecommerce_backend.user.enums.Role;
import com.akbar.ecommerce_backend.user.enums.UserStatus;
import com.akbar.ecommerce_backend.user.repository.UserRepository;
import com.akbar.ecommerce_backend.user.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;


    @InjectMocks
    private AuthServiceImpl authService;

    
    
    private LoginRequestDTO loginRequestDTO;
    private UserEntity user;
    private ChangePasswordRequestDTO changePasswordRequestDTO;
    
    
    @BeforeEach
    void setUp(){

        loginRequestDTO = LoginRequestDTO.builder()
                .email("akbar@gmail.com")
                .password("12345")
                .build();

        user = UserEntity.builder().id(1L)
                .firstName("Muhammad")
                .lastName("Akbar")
                .email("akbar@gmail.com")
                .password("encodedOldPassword")
                .phoneNumber("9399169442")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
        
        changePasswordRequestDTO = ChangePasswordRequestDTO.builder()
                .oldPassword("12345")
                .newPassword("NewPassword@123")
                .confirmPassword("NewPassword@123")
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

        LoginResponseDTO result = authService.login(loginRequestDTO);


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
                        ()-> authService.login(loginRequestDTO)
                        );

        // Assert

        assertEquals("Invalid credentials", exception.getMessage());

        // Verify

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any());


    }


    @Test
    void changePassword_ShouldChangePasswordSuccessfully(){

        SecurityContext securityContext = mock(SecurityContext.class);

        Authentication authentication = mock(Authentication.class);

        CustomUserDetails customUserDetails = mock(CustomUserDetails.class);

        try(MockedStatic<SecurityContextHolder> mockedStaticSecurityContextHolder =
                mockStatic(SecurityContextHolder.class)){

            mockedStaticSecurityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);


            when(securityContext.getAuthentication())
                    .thenReturn(authentication);

            when(authentication.getPrincipal())
                    .thenReturn(customUserDetails);

            when(customUserDetails.getUser())
                    .thenReturn(user);

            when(passwordEncoder.matches(
                    changePasswordRequestDTO.getOldPassword(), user.getPassword()))
                    .thenReturn(true);

            when(passwordEncoder.matches(
                    changePasswordRequestDTO.getNewPassword(), user.getPassword()))
                    .thenReturn(false);

            when(passwordEncoder.encode(changePasswordRequestDTO.getNewPassword()))
                    .thenReturn("encodedNewPassword");

            when(userRepository.save(user))
                    .thenReturn(user);


            // Act

            authService.changePassword(changePasswordRequestDTO);

            // Verify

            verify(passwordEncoder).matches(
                    changePasswordRequestDTO.getOldPassword(),
                    "encodedOldPassword");

            verify(passwordEncoder).matches(
                    changePasswordRequestDTO.getNewPassword(),
                    "encodedOldPassword");

            verify(passwordEncoder)
                    .encode(changePasswordRequestDTO.getNewPassword());

            ArgumentCaptor<UserEntity> userCaptor =
                    ArgumentCaptor.forClass(UserEntity.class);

            verify(userRepository).save(userCaptor.capture());

            UserEntity savedUser = userCaptor.getValue();

            // Assert

            assertEquals("encodedNewPassword", savedUser.getPassword());
        }

    }



    @Test
    void changePassword_ShouldThrowException_WhenPasswordsDoNotMatch(){

        // Arrange

        changePasswordRequestDTO = ChangePasswordRequestDTO.builder()
                .oldPassword("12345")
                .newPassword("NewPassword@123")
                .confirmPassword("DifferentPassword@123")
                .build();


        SecurityContext securityContext = mock(SecurityContext.class);

        Authentication authentication = mock(Authentication.class);

        CustomUserDetails customUserDetails = mock(CustomUserDetails.class);

        try (MockedStatic<SecurityContextHolder> mockedStatic =
                     mockStatic(SecurityContextHolder.class)) {

            mockedStatic.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);

            when(securityContext.getAuthentication())
                    .thenReturn(authentication);

            when(authentication.getPrincipal())
                    .thenReturn(customUserDetails);

            when(customUserDetails.getUser())
                    .thenReturn(user);


            // Act & Assert

            PasswordMismatchException exception =
                    assertThrows(PasswordMismatchException.class,
                            ()-> authService.changePassword(changePasswordRequestDTO));


            assertEquals("New password and confirm password do not match.", exception.getMessage());


            // Verify

            verify(passwordEncoder, never()).matches(anyString(),anyString());
            verify(passwordEncoder,never()).encode(anyString());
            verify(userRepository,never()).save(any(UserEntity.class));


        }

    }



    @Test
    void changePassword_ShouldThrowException_WhenOldPasswordIsIncorrect() {

        // Arrange

        SecurityContext securityContext = mock(SecurityContext.class);

        Authentication authentication = mock(Authentication.class);

        CustomUserDetails customUserDetails = mock(CustomUserDetails.class);

        try (MockedStatic<SecurityContextHolder> mockedStaticSecurityContextHolder =
                     mockStatic(SecurityContextHolder.class)) {

            mockedStaticSecurityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);

            when(securityContext.getAuthentication())
                    .thenReturn(authentication);

            when(authentication.getPrincipal())
                    .thenReturn(customUserDetails);

            when(customUserDetails.getUser())
                    .thenReturn(user);

            when(passwordEncoder.matches(
                    changePasswordRequestDTO.getOldPassword(), "encodedOldPassword"))
                    .thenReturn(false);


            // Act & Assert

            InvalidOldPasswordException exception =
                    assertThrows(InvalidOldPasswordException.class,
                            ()-> authService.changePassword(changePasswordRequestDTO));


            assertEquals("Current password is incorrect.", exception.getMessage());


            // Verify

            verify(passwordEncoder).matches(
                    changePasswordRequestDTO.getOldPassword(), "encodedOldPassword");

            verify(passwordEncoder,never()).encode(anyString());
            verify(userRepository,never()).save(any(UserEntity.class));




        }
    }


    @Test
    void changePassword_ShouldThrowException_WhenNewPasswordIsSameAsOldPassword() {

        // Arrange

        SecurityContext securityContext = mock(SecurityContext.class);

        Authentication authentication = mock(Authentication.class);

        CustomUserDetails customUserDetails = mock(CustomUserDetails.class);

        try (MockedStatic<SecurityContextHolder> mockedStaticSecurityContextHolder =
                     mockStatic(SecurityContextHolder.class)) {

            mockedStaticSecurityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);

            when(securityContext.getAuthentication())
                    .thenReturn(authentication);

            when(authentication.getPrincipal())
                    .thenReturn(customUserDetails);

            when(customUserDetails.getUser())
                    .thenReturn(user);

            when(passwordEncoder.matches(
                    changePasswordRequestDTO.getOldPassword(), "encodedOldPassword"))
                    .thenReturn(true);

            when(passwordEncoder.matches(
                    changePasswordRequestDTO.getNewPassword(), "encodedOldPassword"))
                    .thenReturn(true);

            // Act & Assert

            SamePasswordException exception =
                    assertThrows(SamePasswordException.class,
                            ()-> authService.changePassword(changePasswordRequestDTO));


            assertEquals("New password must be different from current password.", exception.getMessage());


            // Verify

            verify(passwordEncoder).matches(
                    changePasswordRequestDTO.getOldPassword(), "encodedOldPassword");

            verify(passwordEncoder).matches(
                    changePasswordRequestDTO.getNewPassword(), "encodedOldPassword");


            verify(passwordEncoder,never()).encode(anyString());
            verify(userRepository,never()).save(any(UserEntity.class));



        }
    }


}
