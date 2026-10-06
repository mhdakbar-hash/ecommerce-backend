package com.akbar.ecommerce_backend.category.controller;

import com.akbar.ecommerce_backend.category.dto.CategoryRequestDTO;
import com.akbar.ecommerce_backend.category.dto.CategoryResponseDTO;
import com.akbar.ecommerce_backend.category.dto.CategoryUpdateRequestDTO;
import com.akbar.ecommerce_backend.category.enums.CategoryStatus;
import com.akbar.ecommerce_backend.category.entity.CategoryEntity;
import com.akbar.ecommerce_backend.category.repository.CategoryRepository;
import com.akbar.ecommerce_backend.product.entity.ProductEntity;
import com.akbar.ecommerce_backend.product.enums.ProductStatus;
import com.akbar.ecommerce_backend.product.repository.ProductRepository;
import com.akbar.ecommerce_backend.user.dto.auth.LoginRequestDTO;
import com.akbar.ecommerce_backend.user.dto.auth.LoginResponseDTO;
import com.akbar.ecommerce_backend.user.entity.UserEntity;
import com.akbar.ecommerce_backend.user.enums.Role;
import com.akbar.ecommerce_backend.user.enums.UserStatus;
import com.akbar.ecommerce_backend.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProductRepository productRepository;


    private String getAdminToken() throws Exception {

        String adminEmail =
                "category-admin-" + UUID.randomUUID() + "@gmail.com";

        UserEntity admin = UserEntity.builder()
                .firstName("Category")
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
                                .content(
                                        objectMapper.writeValueAsString(loginRequest)
                                )
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
    void shouldCreateCategorySuccessfully() throws Exception {

        String adminToken = getAdminToken();

        String categoryName =
                "Electronics-" + UUID.randomUUID();

        CategoryRequestDTO requestDTO = CategoryRequestDTO.builder()
                .name(categoryName)
                .description("Electronic devices and accessories")
                .build();

        String response = mockMvc.perform(
                        post("/api/v1/categories")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(requestDTO)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(categoryName))
                .andExpect(jsonPath("$.description")
                        .value("Electronic devices and accessories"))
                .andExpect(jsonPath("$.status")
                        .value(CategoryStatus.ACTIVE.name()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CategoryResponseDTO createdCategory =
                objectMapper.readValue(response, CategoryResponseDTO.class);

        CategoryEntity savedCategory = categoryRepository
                .findById(createdCategory.getId())
                .orElseThrow();

        assertThat(savedCategory.getStatus())
                .isEqualTo(CategoryStatus.ACTIVE);

        assertThat(savedCategory.getName())
                .isEqualTo(categoryName);

        assertThat(savedCategory.getDescription())
                .isEqualTo("Electronic devices and accessories");
    }





    @Test
    void shouldRejectCategoryCreationWhenRequestIsInvalid() throws Exception {

        String adminToken = getAdminToken();

        CategoryRequestDTO requestDTO = CategoryRequestDTO.builder()
                .name("")
                .description("Invalid category request")
                .build();

        long categoryCountBefore = categoryRepository.count();

        mockMvc.perform(
                        post("/api/v1/categories")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(requestDTO)
                                )
                )
                .andExpect(status().isBadRequest());

        long categoryCountAfter = categoryRepository.count();

        assertThat(categoryCountAfter)
                .isEqualTo(categoryCountBefore);
    }





    @Test
    void shouldRejectCategoryCreationWhenNameAlreadyExists() throws Exception {

        String adminToken = getAdminToken();

        String categoryName =
                "Electronics-" + UUID.randomUUID();

        CategoryRequestDTO firstRequest = CategoryRequestDTO.builder()
                .name(categoryName)
                .description("Electronic devices and accessories")
                .build();

        mockMvc.perform(
                        post("/api/v1/categories")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(firstRequest)
                                )
                )
                .andExpect(status().isCreated());

        long categoryCountAfterFirstCreate =
                categoryRepository.count();

        CategoryRequestDTO duplicateRequest = CategoryRequestDTO.builder()
                .name(categoryName.toLowerCase())
                .description("Duplicate category name test")
                .build();

        mockMvc.perform(
                        post("/api/v1/categories")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(duplicateRequest)
                                )
                )
                .andExpect(status().isConflict());

        long categoryCountAfterDuplicateAttempt =
                categoryRepository.count();

        assertThat(categoryCountAfterDuplicateAttempt)
                .isEqualTo(categoryCountAfterFirstCreate);
    }





    @Test
    void shouldGetCategoryByIdSuccessfully() throws Exception {

        CategoryEntity category = CategoryEntity.builder()
                .name("Get-Electronics-" + UUID.randomUUID())
                .description("Category used to verify get functionality")
                .status(CategoryStatus.ACTIVE)
                .build();

        category = categoryRepository.save(category);

        String adminToken = getAdminToken();

        mockMvc.perform(
                        get("/api/v1/categories/" + category.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId()))
                .andExpect(jsonPath("$.name").value(category.getName()))
                .andExpect(jsonPath("$.description")
                        .value(category.getDescription()))
                .andExpect(jsonPath("$.status")
                        .value(CategoryStatus.ACTIVE.name()))
                .andExpect(jsonPath("$.createdAt").exists());
    }




    @Test
    void shouldUpdateCategorySuccessfully() throws Exception {

        CategoryEntity category = CategoryEntity.builder()
                .name("Update-Electronics-" + UUID.randomUUID())
                .description("Original category description")
                .status(CategoryStatus.ACTIVE)
                .build();

        category = categoryRepository.save(category);

        String adminToken = getAdminToken();

        String updatedName =
                "Updated-Electronics-" + UUID.randomUUID();

        CategoryUpdateRequestDTO requestDTO =
                CategoryUpdateRequestDTO.builder()
                        .name("  " + updatedName + "  ")
                        .description("Updated category description")
                        .build();

        mockMvc.perform(
                        put("/api/v1/categories/" + category.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(requestDTO)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId()))
                .andExpect(jsonPath("$.name").value(updatedName))
                .andExpect(jsonPath("$.description")
                        .value("Updated category description"))
                .andExpect(jsonPath("$.status")
                        .value(CategoryStatus.ACTIVE.name()));

        CategoryEntity updatedCategory = categoryRepository
                .findById(category.getId())
                .orElseThrow();

        assertThat(updatedCategory.getName())
                .isEqualTo(updatedName);

        assertThat(updatedCategory.getDescription())
                .isEqualTo("Updated category description");
    }






    @Test
    void shouldRejectCategoryDeletionWhenProductsExist() throws Exception {

        CategoryEntity category = CategoryEntity.builder()
                .name("Category-In-Use-" + UUID.randomUUID())
                .description("Category associated with a product")
                .status(CategoryStatus.ACTIVE)
                .build();

        category = categoryRepository.save(category);

        ProductEntity product = ProductEntity.builder()
                .name("Test Product")
                .description("Product associated with category")
                .sku("CATEGORY-TEST-" + UUID.randomUUID())
                .price(new BigDecimal("19999.00"))
                .status(ProductStatus.ACTIVE)
                .category(category)
                .build();

        productRepository.save(product);

        String adminToken = getAdminToken();

        mockMvc.perform(
                        delete("/api/v1/categories/" + category.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isConflict());

        CategoryEntity categoryAfterDeleteAttempt =
                categoryRepository
                        .findById(category.getId())
                        .orElseThrow();

        assertThat(categoryAfterDeleteAttempt.getStatus())
                .isEqualTo(CategoryStatus.ACTIVE);
    }





    @Test
    void shouldNotGetCategoryAfterSoftDelete() throws Exception {

        CategoryEntity category = CategoryEntity.builder()
                .name("Deleted-Category-" + UUID.randomUUID())
                .description("Category used to verify soft delete")
                .status(CategoryStatus.ACTIVE)
                .build();

        category = categoryRepository.save(category);

        String adminToken = getAdminToken();

        mockMvc.perform(
                        delete("/api/v1/categories/" + category.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isNoContent());

        CategoryEntity deletedCategory = categoryRepository
                .findById(category.getId())
                .orElseThrow();

        assertThat(deletedCategory.getStatus())
                .isEqualTo(CategoryStatus.DELETED);

        mockMvc.perform(
                        get("/api/v1/categories/" + category.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isNotFound());
    }





    @Test
    void shouldRestoreDeletedCategorySuccessfully() throws Exception {

        CategoryEntity category = CategoryEntity.builder()
                .name("Restorable-Category-" + UUID.randomUUID())
                .description("Category used to verify restore functionality")
                .status(CategoryStatus.DELETED)
                .build();

        category = categoryRepository.save(category);

        String adminToken = getAdminToken();

        mockMvc.perform(
                        patch("/api/v1/categories/"
                                + category.getId()
                                + "/restore")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId()))
                .andExpect(jsonPath("$.name").value(category.getName()))
                .andExpect(jsonPath("$.status")
                        .value(CategoryStatus.ACTIVE.name()));

        CategoryEntity restoredCategory = categoryRepository
                .findById(category.getId())
                .orElseThrow();

        assertThat(restoredCategory.getStatus())
                .isEqualTo(CategoryStatus.ACTIVE);
    }






    @Test
    void shouldRejectCategoryCreationForCustomer() throws Exception {

        String customerEmail =
                "category-customer-" + UUID.randomUUID() + "@gmail.com";

        UserEntity customer = UserEntity.builder()
                .firstName("Category")
                .lastName("Customer")
                .email(customerEmail)
                .password(passwordEncoder.encode("Password@123"))
                .phoneNumber("8888888888")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(customer);

        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .email(customerEmail)
                .password("Password@123")
                .build();

        String response = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(loginRequest)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoginResponseDTO loginResponse =
                objectMapper.readValue(response, LoginResponseDTO.class);

        String customerToken = loginResponse.getToken();

        CategoryRequestDTO requestDTO = CategoryRequestDTO.builder()
                .name("Customer-Category-" + UUID.randomUUID())
                .description("Customer should not create category")
                .build();

        long categoryCountBefore = categoryRepository.count();

        mockMvc.perform(
                        post("/api/v1/categories")
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(requestDTO)
                                )
                )
                .andExpect(status().isForbidden());

        long categoryCountAfter = categoryRepository.count();

        assertThat(categoryCountAfter)
                .isEqualTo(categoryCountBefore);
    }



}
