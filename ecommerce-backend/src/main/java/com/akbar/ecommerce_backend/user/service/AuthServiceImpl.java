package com.akbar.ecommerce_backend.user.service;

import com.akbar.ecommerce_backend.exception.InvalidOldPasswordException;
import com.akbar.ecommerce_backend.exception.InvalidRefreshTokenException;
import com.akbar.ecommerce_backend.exception.PasswordMismatchException;
import com.akbar.ecommerce_backend.exception.SamePasswordException;
import com.akbar.ecommerce_backend.security.JwtService;
import com.akbar.ecommerce_backend.security.RefreshTokenService;
import com.akbar.ecommerce_backend.user.dto.auth.ChangePasswordRequestDTO;
import com.akbar.ecommerce_backend.user.dto.auth.LoginRequestDTO;
import com.akbar.ecommerce_backend.user.dto.auth.LoginResponseDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserResponseDTO;
import com.akbar.ecommerce_backend.user.entity.RefreshTokenEntity;
import com.akbar.ecommerce_backend.user.entity.UserEntity;
import com.akbar.ecommerce_backend.user.mapper.UserMapper;
import com.akbar.ecommerce_backend.user.repository.RefreshTokenRepository;
import com.akbar.ecommerce_backend.user.repository.UserRepository;
import com.akbar.ecommerce_backend.user.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {


    @Value("${jwt.refresh-token-expiration-days}")
    private Long refreshTokenExpirationDays;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;



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

        String refreshToken = refreshTokenService.generateToken();

        String refreshTokenHash = refreshTokenService.hashToken(refreshToken);

        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
                .tokenHash(refreshTokenHash)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(refreshTokenExpirationDays))
                .revoked(false)
                .build();


        refreshTokenRepository.save(refreshTokenEntity);

        log.info("User logged in successfully. User ID: {}, Email: {}",
                user.getId(),
                user.getEmail());

        return LoginResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .refreshToken(refreshToken)
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





    @Override
    public LoginResponseDTO refreshToken(String refreshToken) {

        log.info("Refresh token request received.");

        String tokenHash = refreshTokenService.hashToken(refreshToken);

        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findByTokenHash(tokenHash)
                        .orElseThrow(() -> new InvalidRefreshTokenException(
                                "Invalid refresh token."));


        if (refreshTokenEntity.isRevoked()) {

            throw new InvalidRefreshTokenException(
                    "Refresh token has been revoked.");
        }


        if (refreshTokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {

            throw new InvalidRefreshTokenException(
                    "Refresh token has expired.");
        }


        UserEntity user = refreshTokenEntity.getUser();


        CustomUserDetails customUserDetails = new CustomUserDetails(user);


        String newAccessToken =
                jwtService.generateToken(customUserDetails);


        String newRefreshToken =
                refreshTokenService.generateToken();


        String newRefreshTokenHash =
                refreshTokenService.hashToken(newRefreshToken);


        refreshTokenEntity.setRevoked(true);


        refreshTokenRepository.save(refreshTokenEntity);


        RefreshTokenEntity newRefreshTokenEntity =
                RefreshTokenEntity.builder()
                        .tokenHash(newRefreshTokenHash)
                        .user(user)
                        .expiresAt(LocalDateTime.now().plusDays(refreshTokenExpirationDays))
                        .revoked(false)
                        .build();


        refreshTokenRepository.save(newRefreshTokenEntity);

        log.info("Access token refreshed successfully. User ID: {}, Email: {}",
                user.getId(),
                user.getEmail());


        return LoginResponseDTO.builder()
                .token(newAccessToken)
                .tokenType("Bearer")
                .refreshToken(newRefreshToken)
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }






    @Override
    public void logout(String refreshToken) {

        log.info("Logout request received.");


        String tokenHash = refreshTokenService.hashToken(refreshToken);


        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findByTokenHash(tokenHash)
                        .orElseThrow(() -> new InvalidRefreshTokenException(
                                "Invalid refresh token."));


        if (refreshTokenEntity.isRevoked()) {

            throw new InvalidRefreshTokenException(
                    "Refresh token has already been revoked.");
        }


        refreshTokenEntity.setRevoked(true);


        refreshTokenRepository.save(refreshTokenEntity);

        log.info("User logged out successfully. User ID: {}, Email: {}",
                refreshTokenEntity.getUser().getId(),
                refreshTokenEntity.getUser().getEmail());

    }



}
