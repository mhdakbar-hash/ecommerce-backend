package com.akbar.ecommerce_backend.user.controller;

import com.akbar.ecommerce_backend.user.dto.auth.ChangePasswordRequestDTO;
import com.akbar.ecommerce_backend.user.dto.auth.LoginRequestDTO;
import com.akbar.ecommerce_backend.user.dto.auth.LoginResponseDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserResponseDTO;
import com.akbar.ecommerce_backend.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO requestDTO){

        return ResponseEntity.ok(authService.login(requestDTO));

    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(){

        return ResponseEntity.ok(authService.getCurrentUser());
    }


    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO requestDTO){

        authService.changePassword(requestDTO);

        return ResponseEntity.ok("Password changed successfully.");


    }

}
