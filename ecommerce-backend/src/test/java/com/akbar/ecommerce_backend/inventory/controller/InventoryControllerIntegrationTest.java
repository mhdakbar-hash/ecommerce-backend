package com.akbar.ecommerce_backend.inventory.controller;

import com.akbar.ecommerce_backend.category.entity.CategoryEntity;
import com.akbar.ecommerce_backend.category.enums.CategoryStatus;
import com.akbar.ecommerce_backend.category.repository.CategoryRepository;
import com.akbar.ecommerce_backend.inventory.dto.InventoryRequestDTO;
import com.akbar.ecommerce_backend.inventory.dto.InventoryResponseDTO;
import com.akbar.ecommerce_backend.inventory.dto.InventoryStockRequestDTO;
import com.akbar.ecommerce_backend.inventory.dto.InventoryUpdateRequestDTO;
import com.akbar.ecommerce_backend.inventory.entity.InventoryEntity;
import com.akbar.ecommerce_backend.inventory.repository.InventoryRepository;
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
class InventoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    private String getAdminToken() throws Exception {

        String adminEmail =
                "inventory-admin-" + UUID.randomUUID() + "@gmail.com";

        UserEntity admin = UserEntity.builder()
                .firstName("Inventory")
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


    private String getCustomerToken() throws Exception {

        String customerEmail =
                "inventory-customer-" + UUID.randomUUID() + "@gmail.com";

        UserEntity customer = UserEntity.builder()
                .firstName("Inventory")
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

        return loginResponse.getToken();
    }


    private CategoryEntity createCategory() {

        CategoryEntity category = CategoryEntity.builder()
                .name("Inventory-Category-" + UUID.randomUUID())
                .description("Category used for inventory integration testing")
                .status(CategoryStatus.ACTIVE)
                .build();

        return categoryRepository.save(category);
    }


    private ProductEntity createProduct(CategoryEntity category) {

        ProductEntity product = ProductEntity.builder()
                .name("Inventory-Test-Product-" + UUID.randomUUID())
                .description("Product used for inventory integration testing")
                .sku("INV-TEST-" + UUID.randomUUID())
                .price(new BigDecimal("999.00"))
                .status(ProductStatus.ACTIVE)
                .category(category)
                .build();

        return productRepository.save(product);
    }


    private InventoryEntity createInventory(
            ProductEntity product,
            int quantity) {

        InventoryEntity inventory = InventoryEntity.builder()
                .product(product)
                .availableQuantity(quantity)
                .reservedQuantity(0)
                .build();

        return inventoryRepository.save(inventory);
    }


    @Test
    void shouldCreateInventorySuccessfully() throws Exception {

        String adminToken = getAdminToken();

        CategoryEntity category = createCategory();
        ProductEntity product = createProduct(category);

        InventoryRequestDTO requestDTO = InventoryRequestDTO.builder()
                .productId(product.getId())
                .availableQuantity(50)
                .build();

        String response = mockMvc.perform(
                        post("/api/v1/inventory")
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
                .andExpect(jsonPath("$.productId").value(product.getId()))
                .andExpect(jsonPath("$.availableQuantity").value(50))
                .andExpect(jsonPath("$.reservedQuantity").value(0))
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        InventoryResponseDTO responseDTO =
                objectMapper.readValue(
                        response,
                        InventoryResponseDTO.class
                );

        InventoryEntity savedInventory =
                inventoryRepository.findById(responseDTO.getId())
                        .orElseThrow();

        assertThat(savedInventory.getProduct().getId())
                .isEqualTo(product.getId());

        assertThat(savedInventory.getAvailableQuantity())
                .isEqualTo(50);

        assertThat(savedInventory.getReservedQuantity())
                .isZero();
    }




    @Test
    void shouldRejectInventoryCreationWhenQuantityIsInvalid()
            throws Exception {

        String adminToken = getAdminToken();

        CategoryEntity category = createCategory();
        ProductEntity product = createProduct(category);

        InventoryRequestDTO requestDTO = InventoryRequestDTO.builder()
                .productId(product.getId())
                .availableQuantity(-10)
                .build();

        long inventoryCountBefore =
                inventoryRepository.count();

        mockMvc.perform(
                        post("/api/v1/inventory")
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

        long inventoryCountAfter =
                inventoryRepository.count();

        assertThat(inventoryCountAfter)
                .isEqualTo(inventoryCountBefore);
    }





    @Test
    void shouldRejectInventoryCreationWhenProductAlreadyHasInventory()
            throws Exception {

        String adminToken = getAdminToken();

        CategoryEntity category = createCategory();
        ProductEntity product = createProduct(category);

        createInventory(product, 50);

        InventoryRequestDTO requestDTO = InventoryRequestDTO.builder()
                .productId(product.getId())
                .availableQuantity(100)
                .build();

        long inventoryCountBefore =
                inventoryRepository.count();

        mockMvc.perform(
                        post("/api/v1/inventory")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(requestDTO)
                                )
                )
                .andExpect(status().isConflict());

        long inventoryCountAfter =
                inventoryRepository.count();

        assertThat(inventoryCountAfter)
                .isEqualTo(inventoryCountBefore);
    }




    @Test
    void shouldGetInventoryByIdSuccessfully() throws Exception {

        String adminToken = getAdminToken();

        CategoryEntity category = createCategory();
        ProductEntity product = createProduct(category);
        InventoryEntity inventory =
                createInventory(product, 50);

        mockMvc.perform(
                        get("/api/v1/inventory/" + inventory.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(inventory.getId()))
                .andExpect(jsonPath("$.productId")
                        .value(product.getId()))
                .andExpect(jsonPath("$.availableQuantity")
                        .value(50))
                .andExpect(jsonPath("$.reservedQuantity")
                        .value(0))
                .andExpect(jsonPath("$.createdAt").exists());
    }





    @Test
    void shouldGetInventoryByProductIdSuccessfully()
            throws Exception {

        String adminToken = getAdminToken();

        CategoryEntity category = createCategory();
        ProductEntity product = createProduct(category);
        InventoryEntity inventory =
                createInventory(product, 75);

        mockMvc.perform(
                        get("/api/v1/inventory/product/"
                                + product.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(inventory.getId()))
                .andExpect(jsonPath("$.productId")
                        .value(product.getId()))
                .andExpect(jsonPath("$.availableQuantity")
                        .value(75))
                .andExpect(jsonPath("$.reservedQuantity")
                        .value(0));
    }





    @Test
    void shouldUpdateInventorySuccessfully()
            throws Exception {

        String adminToken = getAdminToken();

        CategoryEntity category = createCategory();
        ProductEntity product = createProduct(category);
        InventoryEntity inventory =
                createInventory(product, 50);

        InventoryUpdateRequestDTO requestDTO =
                InventoryUpdateRequestDTO.builder()
                        .availableQuantity(120)
                        .build();

        mockMvc.perform(
                        put("/api/v1/inventory/" + inventory.getId())
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
                .andExpect(jsonPath("$.id")
                        .value(inventory.getId()))
                .andExpect(jsonPath("$.availableQuantity")
                        .value(120))
                .andExpect(jsonPath("$.reservedQuantity")
                        .value(0));

        InventoryEntity updatedInventory =
                inventoryRepository.findById(inventory.getId())
                        .orElseThrow();

        assertThat(updatedInventory.getAvailableQuantity())
                .isEqualTo(120);
    }





    @Test
    void shouldAddStockSuccessfully()
            throws Exception {

        String adminToken = getAdminToken();

        CategoryEntity category = createCategory();
        ProductEntity product = createProduct(category);
        InventoryEntity inventory =
                createInventory(product, 50);

        InventoryStockRequestDTO requestDTO =
                InventoryStockRequestDTO.builder()
                        .quantity(25)
                        .build();

        mockMvc.perform(
                        patch("/api/v1/inventory/"
                                + inventory.getId()
                                + "/add-stock")
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
                .andExpect(jsonPath("$.id")
                        .value(inventory.getId()))
                .andExpect(jsonPath("$.availableQuantity")
                        .value(75))
                .andExpect(jsonPath("$.reservedQuantity")
                        .value(0));

        InventoryEntity updatedInventory =
                inventoryRepository.findById(inventory.getId())
                        .orElseThrow();

        assertThat(updatedInventory.getAvailableQuantity())
                .isEqualTo(75);
    }





    @Test
    void shouldRemoveStockSuccessfully()
            throws Exception {

        String adminToken = getAdminToken();

        CategoryEntity category = createCategory();
        ProductEntity product = createProduct(category);
        InventoryEntity inventory =
                createInventory(product, 50);

        InventoryStockRequestDTO requestDTO =
                InventoryStockRequestDTO.builder()
                        .quantity(20)
                        .build();

        mockMvc.perform(
                        patch("/api/v1/inventory/"
                                + inventory.getId()
                                + "/remove-stock")
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
                .andExpect(jsonPath("$.id")
                        .value(inventory.getId()))
                .andExpect(jsonPath("$.availableQuantity")
                        .value(30))
                .andExpect(jsonPath("$.reservedQuantity")
                        .value(0));

        InventoryEntity updatedInventory =
                inventoryRepository.findById(inventory.getId())
                        .orElseThrow();

        assertThat(updatedInventory.getAvailableQuantity())
                .isEqualTo(30);
    }





    @Test
    void shouldRejectRemoveStockWhenInsufficientStock()
            throws Exception {

        String adminToken = getAdminToken();

        CategoryEntity category = createCategory();
        ProductEntity product = createProduct(category);
        InventoryEntity inventory =
                createInventory(product, 10);

        InventoryStockRequestDTO requestDTO =
                InventoryStockRequestDTO.builder()
                        .quantity(20)
                        .build();

        mockMvc.perform(
                        patch("/api/v1/inventory/"
                                + inventory.getId()
                                + "/remove-stock")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(requestDTO)
                                )
                )
                .andExpect(status().isConflict());

        InventoryEntity unchangedInventory =
                inventoryRepository.findById(inventory.getId())
                        .orElseThrow();

        assertThat(unchangedInventory.getAvailableQuantity())
                .isEqualTo(10);

        assertThat(unchangedInventory.getReservedQuantity())
                .isZero();
    }





    @Test
    void shouldRejectInventoryCreationForCustomer()
            throws Exception {

        String customerToken = getCustomerToken();

        CategoryEntity category = createCategory();
        ProductEntity product = createProduct(category);

        InventoryRequestDTO requestDTO = InventoryRequestDTO.builder()
                .productId(product.getId())
                .availableQuantity(50)
                .build();

        long inventoryCountBefore =
                inventoryRepository.count();

        mockMvc.perform(
                        post("/api/v1/inventory")
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

        long inventoryCountAfter =
                inventoryRepository.count();

        assertThat(inventoryCountAfter)
                .isEqualTo(inventoryCountBefore);
    }





    @Test
    void shouldRejectInventoryUpdateForCustomer()
            throws Exception {

        String customerToken = getCustomerToken();

        CategoryEntity category = createCategory();
        ProductEntity product = createProduct(category);
        InventoryEntity inventory =
                createInventory(product, 50);

        InventoryUpdateRequestDTO requestDTO =
                InventoryUpdateRequestDTO.builder()
                        .availableQuantity(100)
                        .build();

        mockMvc.perform(
                        put("/api/v1/inventory/" + inventory.getId())
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

        InventoryEntity unchangedInventory =
                inventoryRepository.findById(inventory.getId())
                        .orElseThrow();

        assertThat(unchangedInventory.getAvailableQuantity())
                .isEqualTo(50);
    }





    @Test
    void shouldGetAllInventoriesSuccessfully() throws Exception {

        String adminToken = getAdminToken();

        CategoryEntity category = createCategory();

        ProductEntity product1 = createProduct(category);
        ProductEntity product2 = createProduct(category);

        createInventory(product1, 50);
        createInventory(product2, 75);

        mockMvc.perform(
                        get("/api/v1/inventory")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").isBoolean());
    }


}