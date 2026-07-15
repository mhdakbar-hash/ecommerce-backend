package com.akbar.ecommerce_backend.user.controller;

import com.akbar.ecommerce_backend.user.dto.UserRequestDTO;
import com.akbar.ecommerce_backend.user.dto.UserResponseDTO;
import com.akbar.ecommerce_backend.user.dto.UserUpdateRequestDTO;
import com.akbar.ecommerce_backend.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }


    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody UserRequestDTO requestDTO){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.registerUser(requestDTO));
    }


    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }


    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id){
        return ResponseEntity.ok(userService.getUserById(id));
    }


    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser
            (@PathVariable Long id,
             @Valid @RequestBody UserUpdateRequestDTO requestDTO){

        return ResponseEntity.ok(userService.updateUser(id, requestDTO));

    }



}
