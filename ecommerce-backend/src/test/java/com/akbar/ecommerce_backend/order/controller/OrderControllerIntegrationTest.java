package com.akbar.ecommerce_backend.order.controller;

import com.akbar.ecommerce_backend.cart.entity.CartEntity;
import com.akbar.ecommerce_backend.cart.entity.CartItemEntity;
import com.akbar.ecommerce_backend.cart.repository.CartItemRepository;
import com.akbar.ecommerce_backend.cart.repository.CartRepository;
import com.akbar.ecommerce_backend.category.entity.CategoryEntity;
import com.akbar.ecommerce_backend.category.enums.CategoryStatus;
import com.akbar.ecommerce_backend.category.repository.CategoryRepository;
import com.akbar.ecommerce_backend.inventory.entity.InventoryEntity;
import com.akbar.ecommerce_backend.inventory.repository.InventoryRepository;
import com.akbar.ecommerce_backend.order.dto.OrderResponseDTO;
import com.akbar.ecommerce_backend.order.entity.OrderEntity;
import com.akbar.ecommerce_backend.order.enums.OrderStatus;
import com.akbar.ecommerce_backend.order.repository.OrderRepository;
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
import org.springframework.data.domain.PageRequest;
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
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    private CategoryEntity createCategory() {

        CategoryEntity category = CategoryEntity.builder()
                .name("Order-Category-" + UUID.randomUUID())
                .description("Category used for order integration testing")
                .status(CategoryStatus.ACTIVE)
                .build();

        return categoryRepository.save(category);
    }


    private ProductEntity createProduct(
            CategoryEntity category,
            String productName) {

        ProductEntity product = ProductEntity.builder()
                .name(productName + "-" + UUID.randomUUID())
                .description("Product used for order integration testing")
                .sku("ORD-TEST-" + UUID.randomUUID())
                .price(new BigDecimal("100.00"))
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
    void shouldRollbackOrderAndInventoryWhenOneItemHasInsufficientStock()
            throws Exception {

        String customerEmail =
                "order-customer-" + UUID.randomUUID() + "@gmail.com";

        UserEntity customer = UserEntity.builder()
                .firstName("Order")
                .lastName("Customer")
                .email(customerEmail)
                .password(passwordEncoder.encode("Password@123"))
                .phoneNumber("888888" + String.format(
                        "%04d",
                        (int) (System.nanoTime() % 10000)
                ))
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(customer);

        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .email(customerEmail)
                .password("Password@123")
                .build();

        String loginResponse = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                loginRequest
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoginResponseDTO loginResponseDTO =
                objectMapper.readValue(
                        loginResponse,
                        LoginResponseDTO.class
                );

        String customerToken = loginResponseDTO.getToken();

        CategoryEntity category = createCategory();

        ProductEntity productA =
                createProduct(category, "Order-Product-A");

        ProductEntity productB =
                createProduct(category, "Order-Product-B");

        InventoryEntity inventoryA =
                createInventory(productA, 10);

        InventoryEntity inventoryB =
                createInventory(productB, 2);

        CartEntity cart = CartEntity.builder()
                .user(customer)
                .build();

        cart = cartRepository.save(cart);

        CartItemEntity cartItemA = CartItemEntity.builder()
                .cart(cart)
                .product(productA)
                .quantity(2)
                .build();

        CartItemEntity cartItemB = CartItemEntity.builder()
                .cart(cart)
                .product(productB)
                .quantity(5)
                .build();

        cartItemRepository.save(cartItemA);
        cartItemRepository.save(cartItemB);

        mockMvc.perform(
                        post("/api/v1/orders")
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                )
                .andExpect(status().isConflict());

        InventoryEntity updatedInventoryA =
                inventoryRepository.findById(inventoryA.getId())
                        .orElseThrow();

        InventoryEntity updatedInventoryB =
                inventoryRepository.findById(inventoryB.getId())
                        .orElseThrow();

        // First reservation must be rolled back.
        assertThat(updatedInventoryA.getAvailableQuantity())
                .isEqualTo(10);

        assertThat(updatedInventoryA.getReservedQuantity())
                .isZero();

        // Failed reservation must leave second inventory unchanged.
        assertThat(updatedInventoryB.getAvailableQuantity())
                .isEqualTo(2);

        assertThat(updatedInventoryB.getReservedQuantity())
                .isZero();

        // No order should exist for this customer.
        assertThat(
                orderRepository.findByUserId(
                        customer.getId(),
                        PageRequest.of(0, 10)
                ).getTotalElements()
        ).isZero();

        // Cart must remain unchanged after transaction rollback.
        CartEntity unchangedCart =
                cartRepository.findByUserId(customer.getId())
                        .orElseThrow();

        assertThat(
                cartItemRepository.findByCartIdAndProductId(
                        unchangedCart.getId(),
                        productA.getId()
                )
        ).isPresent();

        assertThat(
                cartItemRepository.findByCartIdAndProductId(
                        unchangedCart.getId(),
                        productB.getId()
                )
        ).isPresent();

        assertThat(
                cartItemRepository.findByCartIdAndProductId(
                        unchangedCart.getId(),
                        productA.getId()
                ).orElseThrow().getQuantity()
        ).isEqualTo(2);

        assertThat(
                cartItemRepository.findByCartIdAndProductId(
                        unchangedCart.getId(),
                        productB.getId()
                ).orElseThrow().getQuantity()
        ).isEqualTo(5);
    }




    @Test
    void shouldCancelPendingOrderAndReleaseReservedInventory()
            throws Exception {

        String customerEmail =
                "order-cancel-" + UUID.randomUUID() + "@gmail.com";

        UserEntity customer = UserEntity.builder()
                .firstName("Cancel")
                .lastName("Customer")
                .email(customerEmail)
                .password(passwordEncoder.encode("Password@123"))
                .phoneNumber("888888" + String.format(
                        "%04d",
                        (int) (System.nanoTime() % 10000)
                ))
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(customer);

        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .email(customerEmail)
                .password("Password@123")
                .build();

        String loginResponse = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                loginRequest
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoginResponseDTO loginResponseDTO =
                objectMapper.readValue(
                        loginResponse,
                        LoginResponseDTO.class
                );

        String customerToken = loginResponseDTO.getToken();

        CategoryEntity category = createCategory();

        ProductEntity product =
                createProduct(category, "Cancel-Product");

        InventoryEntity inventory =
                createInventory(product, 10);

        CartEntity cart = CartEntity.builder()
                .user(customer)
                .build();

        cart = cartRepository.save(cart);

        CartItemEntity cartItem = CartItemEntity.builder()
                .cart(cart)
                .product(product)
                .quantity(3)
                .build();

        cartItemRepository.save(cartItem);

        // Create order. This should reserve 3 units.
        String orderResponse = mockMvc.perform(
                        post("/api/v1/orders")
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponseDTO createdOrder =
                objectMapper.readValue(
                        orderResponse,
                        OrderResponseDTO.class
                );

        InventoryEntity reservedInventory =
                inventoryRepository.findById(inventory.getId())
                        .orElseThrow();

        assertThat(reservedInventory.getAvailableQuantity())
                .isEqualTo(7);

        assertThat(reservedInventory.getReservedQuantity())
                .isEqualTo(3);

        // Cancel the pending order.
        mockMvc.perform(
                        patch("/api/v1/orders/{id}/cancel", createdOrder.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                )
                .andExpect(status().isOk());

        InventoryEntity releasedInventory =
                inventoryRepository.findById(inventory.getId())
                        .orElseThrow();

        // Reserved stock must be released back to available stock.
        assertThat(releasedInventory.getAvailableQuantity())
                .isEqualTo(10);

        assertThat(releasedInventory.getReservedQuantity())
                .isZero();

        // Verify order status changed to CANCELLED.
        OrderEntity cancelledOrder =
                orderRepository.findById(createdOrder.getId())
                        .orElseThrow();

        assertThat(cancelledOrder.getStatus())
                .isEqualTo(OrderStatus.CANCELLED);
    }




    @Test
    void shouldNotAllowUserToCancelAnotherUsersOrder()
            throws Exception {

        String ownerEmail =
                "order-owner-" + UUID.randomUUID() + "@gmail.com";

        UserEntity owner = UserEntity.builder()
                .firstName("Order")
                .lastName("Owner")
                .email(ownerEmail)
                .password(passwordEncoder.encode("Password@123"))
                .phoneNumber("777777" + String.format(
                        "%04d",
                        (int) (System.nanoTime() % 10000)
                ))
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(owner);

        String attackerEmail =
                "order-attacker-" + UUID.randomUUID() + "@gmail.com";

        UserEntity attacker = UserEntity.builder()
                .firstName("Order")
                .lastName("Attacker")
                .email(attackerEmail)
                .password(passwordEncoder.encode("Password@123"))
                .phoneNumber("666666" + String.format(
                        "%04d",
                        (int) (System.nanoTime() % 10000)
                ))
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(attacker);

        String ownerLoginResponse = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        LoginRequestDTO.builder()
                                                .email(ownerEmail)
                                                .password("Password@123")
                                                .build()
                                ))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoginResponseDTO ownerLogin =
                objectMapper.readValue(
                        ownerLoginResponse,
                        LoginResponseDTO.class
                );

        String attackerLoginResponse = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        LoginRequestDTO.builder()
                                                .email(attackerEmail)
                                                .password("Password@123")
                                                .build()
                                ))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoginResponseDTO attackerLogin =
                objectMapper.readValue(
                        attackerLoginResponse,
                        LoginResponseDTO.class
                );

        CategoryEntity category = createCategory();

        ProductEntity product =
                createProduct(category, "IDOR-Order-Product");

        createInventory(product, 10);

        CartEntity cart = CartEntity.builder()
                .user(owner)
                .build();

        cart = cartRepository.save(cart);

        CartItemEntity cartItem = CartItemEntity.builder()
                .cart(cart)
                .product(product)
                .quantity(2)
                .build();

        cartItemRepository.save(cartItem);

        String orderResponse = mockMvc.perform(
                        post("/api/v1/orders")
                                .header(
                                        "Authorization",
                                        "Bearer " + ownerLogin.getToken()
                                )
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponseDTO createdOrder =
                objectMapper.readValue(
                        orderResponse,
                        OrderResponseDTO.class
                );

        // Attacker tries to cancel owner's order.
        mockMvc.perform(
                        patch("/api/v1/orders/{id}/cancel", createdOrder.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + attackerLogin.getToken()
                                )
                )
                .andExpect(status().isNotFound());

        OrderEntity order =
                orderRepository.findById(createdOrder.getId())
                        .orElseThrow();

        // Order must remain untouched.
        assertThat(order.getStatus())
                .isEqualTo(OrderStatus.PENDING);
    }





    @Test
    void shouldRejectCancellingAlreadyCancelledOrder()
            throws Exception {

        String customerEmail =
                "order-double-cancel-" + UUID.randomUUID() + "@gmail.com";

        UserEntity customer = UserEntity.builder()
                .firstName("Double")
                .lastName("Cancel")
                .email(customerEmail)
                .password(passwordEncoder.encode("Password@123"))
                .phoneNumber("555555" + String.format(
                        "%04d",
                        (int) (System.nanoTime() % 10000)
                ))
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(customer);

        String loginResponse = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        LoginRequestDTO.builder()
                                                .email(customerEmail)
                                                .password("Password@123")
                                                .build()
                                ))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoginResponseDTO login =
                objectMapper.readValue(
                        loginResponse,
                        LoginResponseDTO.class
                );

        CategoryEntity category = createCategory();

        ProductEntity product =
                createProduct(category, "Double-Cancel-Product");

        InventoryEntity inventory =
                createInventory(product, 10);

        CartEntity cart = CartEntity.builder()
                .user(customer)
                .build();

        cart = cartRepository.save(cart);

        CartItemEntity cartItem = CartItemEntity.builder()
                .cart(cart)
                .product(product)
                .quantity(3)
                .build();

        cartItemRepository.save(cartItem);

        String orderResponse = mockMvc.perform(
                        post("/api/v1/orders")
                                .header(
                                        "Authorization",
                                        "Bearer " + login.getToken()
                                )
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponseDTO createdOrder =
                objectMapper.readValue(
                        orderResponse,
                        OrderResponseDTO.class
                );

        // First cancellation.
        mockMvc.perform(
                        patch("/api/v1/orders/{id}/cancel", createdOrder.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + login.getToken()
                                )
                )
                .andExpect(status().isOk());

        InventoryEntity afterFirstCancel =
                inventoryRepository.findById(inventory.getId())
                        .orElseThrow();

        assertThat(afterFirstCancel.getAvailableQuantity())
                .isEqualTo(10);

        assertThat(afterFirstCancel.getReservedQuantity())
                .isZero();

        // Second cancellation must fail.
        mockMvc.perform(
                        patch("/api/v1/orders/{id}/cancel", createdOrder.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + login.getToken()
                                )
                )
                .andExpect(status().isConflict());

        // Inventory must NOT be released again.
        InventoryEntity afterSecondCancel =
                inventoryRepository.findById(inventory.getId())
                        .orElseThrow();

        assertThat(afterSecondCancel.getAvailableQuantity())
                .isEqualTo(10);

        assertThat(afterSecondCancel.getReservedQuantity())
                .isZero();

        OrderEntity order =
                orderRepository.findById(createdOrder.getId())
                        .orElseThrow();

        assertThat(order.getStatus())
                .isEqualTo(OrderStatus.CANCELLED);
    }




    @Test
    void shouldReturnMyOrdersWithCorrectPagination() throws Exception {

        String customerEmail =
                "order-pagination-" + UUID.randomUUID() + "@gmail.com";

        UserEntity customer = UserEntity.builder()
                .firstName("Pagination")
                .lastName("Customer")
                .email(customerEmail)
                .password(passwordEncoder.encode("Password@123"))
                .phoneNumber("444444" + String.format(
                        "%04d",
                        (int) (System.nanoTime() % 10000)
                ))
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(customer);

        String loginResponse = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        LoginRequestDTO.builder()
                                                .email(customerEmail)
                                                .password("Password@123")
                                                .build()
                                ))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoginResponseDTO login =
                objectMapper.readValue(
                        loginResponse,
                        LoginResponseDTO.class
                );

        CategoryEntity category = createCategory();

        ProductEntity product =
                createProduct(category, "Pagination-Product");

        createInventory(product, 20);

        CartEntity cart = CartEntity.builder()
                .user(customer)
                .build();

        cart = cartRepository.save(cart);

        for (int i = 0; i < 3; i++) {

            CartItemEntity cartItem = CartItemEntity.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(1)
                    .build();

            cartItemRepository.save(cartItem);

            mockMvc.perform(
                            post("/api/v1/orders")
                                    .header(
                                            "Authorization",
                                            "Bearer " + login.getToken()
                                    )
                    )
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(
                        get("/api/v1/orders")
                                .param("page", "0")
                                .param("size", "2")
                                .header(
                                        "Authorization",
                                        "Bearer " + login.getToken()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));
    }




    @Test
    void shouldGetMyOrderWithCorrectSnapshotDetails() throws Exception {

        String customerEmail =
                "order-detail-" + UUID.randomUUID() + "@gmail.com";

        UserEntity customer = UserEntity.builder()
                .firstName("Order")
                .lastName("Detail")
                .email(customerEmail)
                .password(passwordEncoder.encode("Password@123"))
                .phoneNumber("555555" + String.format(
                        "%04d",
                        (int) (System.nanoTime() % 10000)
                ))
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(customer);

        String loginResponse = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        LoginRequestDTO.builder()
                                                .email(customerEmail)
                                                .password("Password@123")
                                                .build()
                                ))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoginResponseDTO login =
                objectMapper.readValue(
                        loginResponse,
                        LoginResponseDTO.class
                );

        CategoryEntity category = createCategory();

        ProductEntity product =
                createProduct(category, "Snapshot-Product");

        createInventory(product, 10);

        CartEntity cart = CartEntity.builder()
                .user(customer)
                .build();

        cart = cartRepository.save(cart);

        CartItemEntity cartItem = CartItemEntity.builder()
                .cart(cart)
                .product(product)
                .quantity(2)
                .build();

        cartItemRepository.save(cartItem);

        String orderResponse = mockMvc.perform(
                        post("/api/v1/orders")
                                .header(
                                        "Authorization",
                                        "Bearer " + login.getToken()
                                )
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponseDTO createdOrder =
                objectMapper.readValue(
                        orderResponse,
                        OrderResponseDTO.class
                );

        Long orderId = createdOrder.getId();

        mockMvc.perform(
                        get("/api/v1/orders/" + orderId)
                                .header(
                                        "Authorization",
                                        "Bearer " + login.getToken()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(200.00))
                .andExpect(jsonPath("$.orderItems.length()").value(1))
                .andExpect(jsonPath("$.orderItems[0].productId")
                        .value(product.getId()))
                .andExpect(jsonPath("$.orderItems[0].productName")
                        .value(product.getName()))
                .andExpect(jsonPath("$.orderItems[0].sku")
                        .value(product.getSku()))
                .andExpect(jsonPath("$.orderItems[0].quantity")
                        .value(2))
                .andExpect(jsonPath("$.orderItems[0].unitPrice")
                        .value(100.00))
                .andExpect(jsonPath("$.orderItems[0].subtotal")
                        .value(200.00));
    }



    @Test
    void shouldNotAllowUserToAccessAnotherUsersOrder() throws Exception {

        String ownerEmail =
                "order-owner-" + UUID.randomUUID() + "@gmail.com";

        UserEntity owner = UserEntity.builder()
                .firstName("Order")
                .lastName("Owner")
                .email(ownerEmail)
                .password(passwordEncoder.encode("Password@123"))
                .phoneNumber("666666" + String.format(
                        "%04d",
                        (int) (System.nanoTime() % 10000)
                ))
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(owner);

        String ownerLoginResponse = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        LoginRequestDTO.builder()
                                                .email(ownerEmail)
                                                .password("Password@123")
                                                .build()
                                ))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoginResponseDTO ownerLogin =
                objectMapper.readValue(
                        ownerLoginResponse,
                        LoginResponseDTO.class
                );

        CategoryEntity category = createCategory();

        ProductEntity product =
                createProduct(category, "Ownership-Product");

        createInventory(product, 10);

        CartEntity cart = CartEntity.builder()
                .user(owner)
                .build();

        cart = cartRepository.save(cart);

        CartItemEntity cartItem = CartItemEntity.builder()
                .cart(cart)
                .product(product)
                .quantity(1)
                .build();

        cartItemRepository.save(cartItem);

        String orderResponse = mockMvc.perform(
                        post("/api/v1/orders")
                                .header(
                                        "Authorization",
                                        "Bearer " + ownerLogin.getToken()
                                )
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponseDTO createdOrder =
                objectMapper.readValue(
                        orderResponse,
                        OrderResponseDTO.class
                );

        Long orderId = createdOrder.getId();

        String attackerEmail =
                "order-attacker-" + UUID.randomUUID() + "@gmail.com";

        UserEntity attacker = UserEntity.builder()
                .firstName("Order")
                .lastName("Attacker")
                .email(attackerEmail)
                .password(passwordEncoder.encode("Password@123"))
                .phoneNumber("777777" + String.format(
                        "%04d",
                        (int) (System.nanoTime() % 10000)
                ))
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(attacker);

        String attackerLoginResponse = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        LoginRequestDTO.builder()
                                                .email(attackerEmail)
                                                .password("Password@123")
                                                .build()
                                ))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoginResponseDTO attackerLogin =
                objectMapper.readValue(
                        attackerLoginResponse,
                        LoginResponseDTO.class
                );

        mockMvc.perform(
                        get("/api/v1/orders/" + orderId)
                                .header(
                                        "Authorization",
                                        "Bearer " + attackerLogin.getToken()
                                )
                )
                .andExpect(status().isNotFound());
    }

}