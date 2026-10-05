package com.akbar.ecommerce_backend.product.controller;

import com.akbar.ecommerce_backend.category.entity.CategoryEntity;
import com.akbar.ecommerce_backend.category.enums.CategoryStatus;
import com.akbar.ecommerce_backend.category.repository.CategoryRepository;
import com.akbar.ecommerce_backend.product.dto.ProductRequestDTO;
import com.akbar.ecommerce_backend.product.dto.ProductResponseDTO;
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
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    private CategoryEntity createActiveCategory() {

        CategoryEntity category = CategoryEntity.builder()
                .name("Electronics-" + UUID.randomUUID())
                .description("Test electronics category")
                .status(CategoryStatus.ACTIVE)
                .build();

        return categoryRepository.save(category);
    }


    private String getAdminToken() throws Exception {

        String adminEmail =
                "product-admin-" + UUID.randomUUID() + "@gmail.com";

        UserEntity admin = UserEntity.builder()
                .firstName("Product")
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
    void shouldCreateProductSuccessfully() throws Exception {

        CategoryEntity category = createActiveCategory();

        String adminToken = getAdminToken();

        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .name("iPhone 15")
                .description("Apple iPhone 15 with 128GB storage.")
                .sku("IPHONE-" + UUID.randomUUID())
                .price(new BigDecimal("69999.00"))
                .imageUrl("https://example.com/iphone15.jpg")
                .categoryId(category.getId())
                .build();

        String response = mockMvc.perform(
                        post("/api/v1/products")
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
                .andExpect(jsonPath("$.name").value("iPhone 15"))
                .andExpect(jsonPath("$.description")
                        .value("Apple iPhone 15 with 128GB storage."))
                .andExpect(jsonPath("$.sku").value(requestDTO.getSku().toUpperCase()))
                .andExpect(jsonPath("$.price").value(69999.00))
                .andExpect(jsonPath("$.status")
                        .value(ProductStatus.ACTIVE.name()))
                .andExpect(jsonPath("$.categoryName")
                        .value(category.getName()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ProductResponseDTO createdProduct =
                objectMapper.readValue(response, ProductResponseDTO.class);

        ProductEntity savedProduct = productRepository
                .findById(createdProduct.getId())
                .orElseThrow();

        assertThat(savedProduct.getStatus())
                .isEqualTo(ProductStatus.ACTIVE);

        assertThat(savedProduct.getSku())
                .isEqualTo(requestDTO.getSku().toUpperCase());

        assertThat(savedProduct.getCategory().getId())
                .isEqualTo(category.getId());
    }




    @Test
    void shouldRejectProductCreationWhenRequestIsInvalid() throws Exception {

        String adminToken = getAdminToken();

        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .name("")
                .description("")
                .sku("")
                .price(BigDecimal.ZERO)
                .categoryId(null)
                .build();

        long productCountBefore = productRepository.count();

        mockMvc.perform(
                        post("/api/v1/products")
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

        long productCountAfter = productRepository.count();

        assertThat(productCountAfter)
                .isEqualTo(productCountBefore);
    }





    @Test
    void shouldRejectProductCreationWhenSkuAlreadyExists() throws Exception {

        CategoryEntity category = createActiveCategory();

        String adminToken = getAdminToken();

        String sku = "IPHONE-" + UUID.randomUUID();

        ProductRequestDTO firstRequest = ProductRequestDTO.builder()
                .name("iPhone 15")
                .description("Apple iPhone 15 with 128GB storage.")
                .sku(sku)
                .price(new BigDecimal("69999.00"))
                .imageUrl("https://example.com/iphone15.jpg")
                .categoryId(category.getId())
                .build();

        mockMvc.perform(
                        post("/api/v1/products")
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

        long productCountAfterFirstCreate = productRepository.count();

        ProductRequestDTO duplicateRequest = ProductRequestDTO.builder()
                .name("iPhone 15 Duplicate")
                .description("Duplicate product using the same SKU.")
                .sku(sku.toLowerCase())
                .price(new BigDecimal("69999.00"))
                .imageUrl("https://example.com/duplicate.jpg")
                .categoryId(category.getId())
                .build();

        mockMvc.perform(
                        post("/api/v1/products")
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

        long productCountAfterDuplicateAttempt = productRepository.count();

        assertThat(productCountAfterDuplicateAttempt)
                .isEqualTo(productCountAfterFirstCreate);
    }





    @Test
    void shouldRejectProductCreationWhenCategoryIsInactive() throws Exception {

        CategoryEntity category = CategoryEntity.builder()
                .name("Inactive-Electronics-" + UUID.randomUUID())
                .description("Inactive test category")
                .status(CategoryStatus.INACTIVE)
                .build();

        category = categoryRepository.save(category);

        String adminToken = getAdminToken();

        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .name("iPhone 15")
                .description("Apple iPhone 15 with 128GB storage.")
                .sku("IPHONE-" + UUID.randomUUID())
                .price(new BigDecimal("69999.00"))
                .imageUrl("https://example.com/iphone15.jpg")
                .categoryId(category.getId())
                .build();

        long productCountBefore = productRepository.count();

        mockMvc.perform(
                        post("/api/v1/products")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(requestDTO)
                                )
                )
                .andExpect(status().isNotFound());

        long productCountAfter = productRepository.count();

        assertThat(productCountAfter)
                .isEqualTo(productCountBefore);
    }






    @Test
    void shouldRejectProductCreationForCustomer() throws Exception {

        CategoryEntity category = createActiveCategory();

        String customerEmail =
                "product-customer-" + UUID.randomUUID() + "@gmail.com";

        UserEntity customer = UserEntity.builder()
                .firstName("Product")
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

        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .name("iPhone 15")
                .description("Apple iPhone 15 with 128GB storage.")
                .sku("IPHONE-" + UUID.randomUUID())
                .price(new BigDecimal("69999.00"))
                .imageUrl("https://example.com/iphone15.jpg")
                .categoryId(category.getId())
                .build();

        long productCountBefore = productRepository.count();

        mockMvc.perform(
                        post("/api/v1/products")
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

        long productCountAfter = productRepository.count();

        assertThat(productCountAfter)
                .isEqualTo(productCountBefore);
    }





    @Test
    void shouldGetProductByIdSuccessfully() throws Exception {

        CategoryEntity category = createActiveCategory();

        ProductEntity product = ProductEntity.builder()
                .name("Samsung Galaxy S24")
                .description("Samsung Galaxy S24 with 256GB storage.")
                .sku("SAMSUNG-" + UUID.randomUUID())
                .price(new BigDecimal("74999.00"))
                .imageUrl("https://example.com/s24.jpg")
                .status(ProductStatus.ACTIVE)
                .category(category)
                .build();

        product = productRepository.save(product);

        String adminToken = getAdminToken();

        mockMvc.perform(
                        get("/api/v1/products/" + product.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.name").value("Samsung Galaxy S24"))
                .andExpect(jsonPath("$.description")
                        .value("Samsung Galaxy S24 with 256GB storage."))
                .andExpect(jsonPath("$.sku").value(product.getSku()))
                .andExpect(jsonPath("$.price").value(74999.00))
                .andExpect(jsonPath("$.status")
                        .value(ProductStatus.ACTIVE.name()))
                .andExpect(jsonPath("$.categoryName")
                        .value(category.getName()))
                .andExpect(jsonPath("$.createdAt").exists());
    }






    @Test
    void shouldNotGetProductAfterSoftDelete() throws Exception {

        CategoryEntity category = createActiveCategory();

        ProductEntity product = ProductEntity.builder()
                .name("Deleted Product")
                .description("Product used to verify soft delete behavior.")
                .sku("DELETED-" + UUID.randomUUID())
                .price(new BigDecimal("19999.00"))
                .imageUrl("https://example.com/deleted.jpg")
                .status(ProductStatus.ACTIVE)
                .category(category)
                .build();

        product = productRepository.save(product);

        String adminToken = getAdminToken();

        mockMvc.perform(
                        delete("/api/v1/products/" + product.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isNoContent());

        ProductEntity deletedProduct = productRepository
                .findById(product.getId())
                .orElseThrow();

        assertThat(deletedProduct.getStatus())
                .isEqualTo(ProductStatus.DELETED);

        mockMvc.perform(
                        get("/api/v1/products/" + product.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isNotFound());
    }





    @Test
    void shouldRestoreDeletedProductSuccessfully() throws Exception {

        CategoryEntity category = createActiveCategory();

        ProductEntity product = ProductEntity.builder()
                .name("Restorable Product")
                .description("Product used to verify restore functionality.")
                .sku("RESTORE-" + UUID.randomUUID())
                .price(new BigDecimal("29999.00"))
                .imageUrl("https://example.com/restore.jpg")
                .status(ProductStatus.DELETED)
                .category(category)
                .build();

        product = productRepository.save(product);

        String adminToken = getAdminToken();

        mockMvc.perform(
                        patch("/api/v1/products/" + product.getId() + "/restore")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.name").value("Restorable Product"))
                .andExpect(jsonPath("$.status")
                        .value(ProductStatus.ACTIVE.name()))
                .andExpect(jsonPath("$.categoryName")
                        .value(category.getName()));

        ProductEntity restoredProduct = productRepository
                .findById(product.getId())
                .orElseThrow();

        assertThat(restoredProduct.getStatus())
                .isEqualTo(ProductStatus.ACTIVE);
    }





    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {

        String adminToken = getAdminToken();

        long nonExistentProductId = 999999L;

        mockMvc.perform(
                        get("/api/v1/products/" + nonExistentProductId)
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isNotFound());
    }


}