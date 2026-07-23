package com.akbar.ecommerce_backend.user.controller;

import com.akbar.ecommerce_backend.user.dto.common.PageResponseDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserRequestDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserResponseDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserUpdateRequestDTO;
import com.akbar.ecommerce_backend.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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
    public ResponseEntity<PageResponseDTO<UserResponseDTO>> getAllUsers(

            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )

            Pageable pageable){

        return ResponseEntity.ok(userService.getAllUsers(pageable));
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



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }



}
