package com.akbar.ecommerce_backend.user.controller;

import com.akbar.ecommerce_backend.user.dto.user.UserRequestDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserResponseDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserUpdateRequestDTO;
import com.akbar.ecommerce_backend.user.entity.UserEntity;
import com.akbar.ecommerce_backend.user.repository.UserRepository;
import com.akbar.ecommerce_backend.user.enums.Role;
import com.akbar.ecommerce_backend.user.enums.UserStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;

import com.akbar.ecommerce_backend.user.dto.auth.LoginRequestDTO;
import com.akbar.ecommerce_backend.user.dto.auth.LoginResponseDTO;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    private String getAdminToken() throws Exception {

        String adminEmail = "testadmin-" + UUID.randomUUID() + "@gmail.com";

        UserEntity admin = UserEntity.builder()
                .firstName("Test")
                .lastName("Admin")
                .email(adminEmail)
                .password(passwordEncoder.encode("Password@123"))
                .phoneNumber("9999999999")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(admin);

        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .email(adminEmail)
                .password("Password@123")
                .build();

        String response = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoginResponseDTO loginResponse =
                objectMapper.readValue(response, LoginResponseDTO.class);

        return loginResponse.getToken();
    }



    @Test
    void shouldRegisterUserSuccessfully() throws Exception {

        UserRequestDTO requestDTO = UserRequestDTO.builder()
                .firstName("Test")
                .lastName("User")
                .email("testuser@gmail.com")
                .password("Password@123")
                .phoneNumber("9876543210")
                .build();

        mockMvc.perform(
                        post("/api/v1/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.email").value("testuser@gmail.com"))
                .andExpect(jsonPath("$.phoneNumber").value("9876543210"))
                .andExpect(jsonPath("$.role").value(Role.CUSTOMER.name()))
                .andExpect(jsonPath("$.status").value(UserStatus.ACTIVE.name()))
                .andExpect(jsonPath("$.createdAt").exists());

        UserEntity savedUser = userRepository
                .findByEmailIgnoreCase("testuser@gmail.com")
                .orElseThrow();

        assertThat(savedUser.getFirstName())
                .isEqualTo("Test");

        assertThat(savedUser.getRole())
                .isEqualTo(Role.CUSTOMER);

        assertThat(savedUser.getStatus())
                .isEqualTo(UserStatus.ACTIVE);

        assertThat(savedUser.getPassword())
                .isNotEqualTo("Password@123");
    }




    @Test
    void shouldRejectRegistrationWhenRequestIsInvalid() throws Exception {

        UserRequestDTO requestDTO = UserRequestDTO.builder()
                .firstName("")
                .lastName("User")
                .email("invalid-email")
                .password("123")
                .phoneNumber("987654321")
                .build();

        mockMvc.perform(
                        post("/api/v1/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors").exists());
    }



    @Test
    void shouldRejectRegistrationWhenEmailAlreadyExists() throws Exception {

        UserRequestDTO firstRequest = UserRequestDTO.builder()
                .firstName("Existing")
                .lastName("User")
                .email("duplicate@gmail.com")
                .password("Password@123")
                .phoneNumber("9876543210")
                .build();

        mockMvc.perform(
                        post("/api/v1/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(firstRequest))
                )
                .andExpect(status().isCreated());

        UserRequestDTO duplicateRequest = UserRequestDTO.builder()
                .firstName("Another")
                .lastName("User")
                .email("duplicate@gmail.com")
                .password("Password@456")
                .phoneNumber("9876543211")
                .build();

        mockMvc.perform(
                        post("/api/v1/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(duplicateRequest))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").exists());
    }



    @Test
    void shouldGetUserByIdSuccessfully() throws Exception {

        UserRequestDTO requestDTO = UserRequestDTO.builder()
                .firstName("Get")
                .lastName("User")
                .email("getuser@gmail.com")
                .password("Password@123")
                .phoneNumber("9876543210")
                .build();

        String response = mockMvc.perform(
                        post("/api/v1/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponseDTO registeredUser =
                objectMapper.readValue(response, UserResponseDTO.class);

        String adminToken = getAdminToken();

        mockMvc.perform(
                        get("/api/v1/users/{id}", registeredUser.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(registeredUser.getId()))
                .andExpect(jsonPath("$.firstName").value("Get"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.email").value("getuser@gmail.com"))
                .andExpect(jsonPath("$.phoneNumber").value("9876543210"))
                .andExpect(jsonPath("$.role").value(Role.CUSTOMER.name()))
                .andExpect(jsonPath("$.status").value(UserStatus.ACTIVE.name()))
                .andExpect(jsonPath("$.createdAt").exists());
    }



    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {

        String adminToken = getAdminToken();

        mockMvc.perform(
                        get("/api/v1/users/{id}", 999999L)
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }



    @Test
    void shouldUpdateUserSuccessfully() throws Exception {

        UserRequestDTO createRequest = UserRequestDTO.builder()
                .firstName("Before")
                .lastName("Update")
                .email("updateuser@gmail.com")
                .password("Password@123")
                .phoneNumber("9876543210")
                .build();

        String response = mockMvc.perform(
                        post("/api/v1/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponseDTO registeredUser =
                objectMapper.readValue(response, UserResponseDTO.class);

        String adminToken = getAdminToken();

        UserUpdateRequestDTO updateRequest = UserUpdateRequestDTO.builder()
                .firstName("After")
                .lastName("Updated")
                .phoneNumber("9999999999")
                .build();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders

                                .put("/api/v1/users/{id}", registeredUser.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(registeredUser.getId()))
                .andExpect(jsonPath("$.firstName").value("After"))
                .andExpect(jsonPath("$.lastName").value("Updated"))
                .andExpect(jsonPath("$.email").value("updateuser@gmail.com"))
                .andExpect(jsonPath("$.phoneNumber").value("9999999999"))
                .andExpect(jsonPath("$.role").value(Role.CUSTOMER.name()))
                .andExpect(jsonPath("$.status").value(UserStatus.ACTIVE.name()));
    }



    @Test
    void shouldRejectUpdateWhenRequestIsInvalid() throws Exception {

        UserRequestDTO createRequest = UserRequestDTO.builder()
                .firstName("Invalid")
                .lastName("Update")
                .email("invalidupdate@gmail.com")
                .password("Password@123")
                .phoneNumber("9876543210")
                .build();

        String response = mockMvc.perform(
                        post("/api/v1/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponseDTO registeredUser =
                objectMapper.readValue(response, UserResponseDTO.class);

        String adminToken = getAdminToken();

        UserUpdateRequestDTO updateRequest = UserUpdateRequestDTO.builder()
                .firstName("")
                .lastName("U")
                .phoneNumber("123")
                .build();

        mockMvc.perform(
                put("/api/v1/users/{id}", registeredUser.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors").exists());
    }



    @Test
    void shouldDeleteUserSuccessfully() throws Exception {

        UserRequestDTO createRequest = UserRequestDTO.builder()
                .firstName("Delete")
                .lastName("User")
                .email("deleteuser@gmail.com")
                .password("Password@123")
                .phoneNumber("9876543210")
                .build();

        String response = mockMvc.perform(
                        post("/api/v1/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponseDTO registeredUser =
                objectMapper.readValue(response, UserResponseDTO.class);

        String adminToken = getAdminToken();

        mockMvc.perform(
                delete("/api/v1/users/{id}", registeredUser.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isNoContent());

        UserEntity deletedUser = userRepository
                .findById(registeredUser.getId())
                .orElseThrow();

        assertThat(deletedUser.getStatus())
                .isEqualTo(UserStatus.DELETED);
    }



    @Test
    void shouldReturnNotFoundWhenDeletingAlreadyDeletedUser() throws Exception {

        UserRequestDTO createRequest = UserRequestDTO.builder()
                .firstName("Already")
                .lastName("Deleted")
                .email("alreadydeleted@gmail.com")
                .password("Password@123")
                .phoneNumber("9876543210")
                .build();

        String response = mockMvc.perform(
                        post("/api/v1/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponseDTO registeredUser =
                objectMapper.readValue(response, UserResponseDTO.class);

        String adminToken = getAdminToken();

        mockMvc.perform(
                        delete("/api/v1/users/{id}", registeredUser.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        delete("/api/v1/users/{id}", registeredUser.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());

        UserEntity deletedUser = userRepository
                .findById(registeredUser.getId())
                .orElseThrow();

        assertThat(deletedUser.getStatus())
                .isEqualTo(UserStatus.DELETED);
    }




    @Test
    void shouldRestoreDeletedUserSuccessfully() throws Exception {

        UserRequestDTO createRequest = UserRequestDTO.builder()
                .firstName("Restore")
                .lastName("User")
                .email("restoreuser@gmail.com")
                .password("Password@123")
                .phoneNumber("9876543210")
                .build();

        String response = mockMvc.perform(
                        post("/api/v1/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponseDTO registeredUser =
                objectMapper.readValue(response, UserResponseDTO.class);

        String adminToken = getAdminToken();

        mockMvc.perform(
                        delete("/api/v1/users/{id}", registeredUser.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        patch("/api/v1/users/{id}/restore", registeredUser.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(registeredUser.getId()))
                .andExpect(jsonPath("$.firstName").value("Restore"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.email").value("restoreuser@gmail.com"))
                .andExpect(jsonPath("$.status").value(UserStatus.ACTIVE.name()));

        UserEntity restoredUser = userRepository
                .findById(registeredUser.getId())
                .orElseThrow();

        assertThat(restoredUser.getStatus())
                .isEqualTo(UserStatus.ACTIVE);
    }




    @Test
    void shouldReturnNotFoundWhenRestoringActiveUser() throws Exception {

        UserRequestDTO createRequest = UserRequestDTO.builder()
                .firstName("Active")
                .lastName("User")
                .email("activeuser@gmail.com")
                .password("Password@123")
                .phoneNumber("9876543210")
                .build();

        String response = mockMvc.perform(
                        post("/api/v1/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponseDTO registeredUser =
                objectMapper.readValue(response, UserResponseDTO.class);

        String adminToken = getAdminToken();

        mockMvc.perform(
                        patch(
                                        "/api/v1/users/{id}/restore",
                                        registeredUser.getId()
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());

        UserEntity user = userRepository
                .findById(registeredUser.getId())
                .orElseThrow();

        assertThat(user.getStatus())
                .isEqualTo(UserStatus.ACTIVE);
    }




    @Test
    void shouldForbidCustomerFromAccessingUserById() throws Exception {

        UserRequestDTO createRequest = UserRequestDTO.builder()
                .firstName("Customer")
                .lastName("User")
                .email("securitycustomer@gmail.com")
                .password("Password@123")
                .phoneNumber("9876543210")
                .build();

        String response = mockMvc.perform(
                        post("/api/v1/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponseDTO registeredUser =
                objectMapper.readValue(response, UserResponseDTO.class);

        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .email("securitycustomer@gmail.com")
                .password("Password@123")
                .build();

        String loginResponse = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoginResponseDTO loginResponseDTO =
                objectMapper.readValue(loginResponse, LoginResponseDTO.class);

        mockMvc.perform(
                        get("/api/v1/users/{id}", registeredUser.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + loginResponseDTO.getToken()
                                )
                )
                .andExpect(status().isForbidden());
    }




    @Test
    void shouldReturnUnauthorizedWhenTokenIsMissing() throws Exception {

        mockMvc.perform(
                        get("/api/v1/users/{id}", 1L)
                )
                .andExpect(status().isUnauthorized());
    }




    @Test
    void shouldAllowAdminToAccessUserById() throws Exception {

        UserRequestDTO createRequest = UserRequestDTO.builder()
                .firstName("Admin")
                .lastName("Target")
                .email("admintarget@gmail.com")
                .password("Password@123")
                .phoneNumber("9876543210")
                .build();

        String response = mockMvc.perform(
                        post("/api/v1/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponseDTO registeredUser =
                objectMapper.readValue(response, UserResponseDTO.class);

        String adminToken = getAdminToken();

        mockMvc.perform(
                        get("/api/v1/users/{id}", registeredUser.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(registeredUser.getId()))
                .andExpect(jsonPath("$.email").value("admintarget@gmail.com"))
                .andExpect(jsonPath("$.role").value(Role.CUSTOMER.name()))
                .andExpect(jsonPath("$.status").value(UserStatus.ACTIVE.name()));
    }





    @Test
    void shouldReturnNotFoundWhenGettingDeletedUser() throws Exception {

        UserRequestDTO createRequest = UserRequestDTO.builder()
                .firstName("Deleted")
                .lastName("Get")
                .email("deletedget@gmail.com")
                .password("Password@123")
                .phoneNumber("9876543210")
                .build();

        String response = mockMvc.perform(
                        post("/api/v1/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponseDTO registeredUser =
                objectMapper.readValue(response, UserResponseDTO.class);

        String adminToken = getAdminToken();

        mockMvc.perform(
                        delete("/api/v1/users/{id}", registeredUser.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get("/api/v1/users/{id}", registeredUser.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }
}





