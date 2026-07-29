package com.akbar.ecommerce_backend.user.service;

import com.akbar.ecommerce_backend.exception.InvalidOldPasswordException;
import com.akbar.ecommerce_backend.exception.PasswordMismatchException;
import com.akbar.ecommerce_backend.exception.SamePasswordException;
import com.akbar.ecommerce_backend.security.JwtService;
import com.akbar.ecommerce_backend.user.dto.auth.ChangePasswordRequestDTO;
import com.akbar.ecommerce_backend.user.dto.auth.LoginRequestDTO;
import com.akbar.ecommerce_backend.user.dto.auth.LoginResponseDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserResponseDTO;
import com.akbar.ecommerce_backend.user.entity.UserEntity;
import com.akbar.ecommerce_backend.user.mapper.UserMapper;
import com.akbar.ecommerce_backend.user.repository.UserRepository;
import com.akbar.ecommerce_backend.user.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtService jwtService,
                           UserMapper userMapper, PasswordEncoder passwordEncoder, UserRepository userRepository){
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }


    @Override
    public LoginResponseDTO login(LoginRequestDTO requestDTO) {

        log.info("Login attempt for email: {}", requestDTO.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestDTO.getEmail(),
                        requestDTO.getPassword()
                ));

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        UserEntity user = customUserDetails.getUser();

        String token = jwtService.generateToken(customUserDetails);

        log.info("User logged in successfully. User ID: {}, Email: {}",
                user.getId(),
                user.getEmail());

        return LoginResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }




    @Override
    public UserResponseDTO getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        UserEntity user = customUserDetails.getUser();

        return userMapper.toResponseDTO(user);
    }




    @Override
    public void changePassword(ChangePasswordRequestDTO requestDTO) {

        //Step 1 - get current logged-in user
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        UserEntity user = customUserDetails.getUser();

        log.info("Password change requested. User ID: {}, Email: {}",
                user.getId(),
                user.getEmail());

        // Step 2 -new password & confirm password match
        if (!requestDTO.getNewPassword()
                .equals(requestDTO.getConfirmPassword())){

            throw new PasswordMismatchException("New password and confirm password do not match.");
        }

        // Step 3 - verify current password
        if (!passwordEncoder.matches(requestDTO.getOldPassword(), user.getPassword())){

            throw new InvalidOldPasswordException("Current password is incorrect.");
        }

        //Step 4 - new password must be different

        if(passwordEncoder.matches(requestDTO.getNewPassword(), user.getPassword())){

            throw new SamePasswordException("New password must be different from current password.");
        }

        //Step 5-  encode new password
        user.setPassword(passwordEncoder.encode(requestDTO.getNewPassword()));

        //Step 6 - save updated password
        userRepository.save(user);

        log.info("Password changed successfully. User ID: {}, Email: {}",
                user.getId(),
                user.getEmail());

    }



}
