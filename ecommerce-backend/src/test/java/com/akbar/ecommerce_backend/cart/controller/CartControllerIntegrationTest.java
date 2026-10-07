package com.akbar.ecommerce_backend.cart.controller;

import com.akbar.ecommerce_backend.cart.dto.CartItemRequestDTO;
import com.akbar.ecommerce_backend.cart.dto.CartItemUpdateRequestDTO;
import com.akbar.ecommerce_backend.cart.entity.CartEntity;
import com.akbar.ecommerce_backend.cart.entity.CartItemEntity;
import com.akbar.ecommerce_backend.cart.repository.CartItemRepository;
import com.akbar.ecommerce_backend.cart.repository.CartRepository;
import com.akbar.ecommerce_backend.category.entity.CategoryEntity;
import com.akbar.ecommerce_backend.category.enums.CategoryStatus;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CartControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    private UserEntity createCustomer() {

        UserEntity customer = UserEntity.builder()
                .firstName("Cart")
                .lastName("Customer")
                .email(
                        "cart-customer-"
                                + UUID.randomUUID()
                                + "@gmail.com"
                )
                .password(
                        passwordEncoder.encode("Password@123")
                )
                .phoneNumber(
                        "7"
                                + String.valueOf(
                                System.currentTimeMillis()
                        ).substring(3)
                )
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        return userRepository.save(customer);
    }


    private UserEntity createSecondCustomer() {

        UserEntity customer = UserEntity.builder()
                .firstName("Second")
                .lastName("Customer")
                .email(
                        "cart-second-customer-"
                                + UUID.randomUUID()
                                + "@gmail.com"
                )
                .password(
                        passwordEncoder.encode("Password@123")
                )
                .phoneNumber(
                        "6"
                                + String.valueOf(
                                System.currentTimeMillis()
                        ).substring(3)
                )
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        return userRepository.save(customer);
    }


    private String getLoginToken(UserEntity user)
            throws Exception {

        LoginRequestDTO loginRequest =
                LoginRequestDTO.builder()
                        .email(user.getEmail())
                        .password("Password@123")
                        .build();

        String response = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                loginRequest
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(
                        jsonPath("$.tokenType")
                                .value("Bearer")
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoginResponseDTO loginResponse =
                objectMapper.readValue(
                        response,
                        LoginResponseDTO.class
                );

        return loginResponse.getToken();
    }


    private String getCustomerToken()
            throws Exception {

        UserEntity customer = createCustomer();

        return getLoginToken(customer);
    }


    private CategoryEntity createCategory() {

        CategoryEntity category = CategoryEntity.builder()
                .name(
                        "Cart-Category-"
                                + UUID.randomUUID()
                )
                .description(
                        "Category used for cart integration testing"
                )
                .status(CategoryStatus.ACTIVE)
                .build();

        return categoryRepository.save(category);
    }


    private ProductEntity createProduct(
            CategoryEntity category) {

        ProductEntity product = ProductEntity.builder()
                .name(
                        "Cart-Test-Product-"
                                + UUID.randomUUID()
                )
                .description(
                        "Product used for cart integration testing"
                )
                .sku(
                        "CART-TEST-"
                                + UUID.randomUUID()
                )
                .price(new BigDecimal("999.00"))
                .status(ProductStatus.ACTIVE)
                .category(category)
                .build();

        return productRepository.save(product);
    }


    private ProductEntity createInactiveProduct(
            CategoryEntity category) {

        ProductEntity product = ProductEntity.builder()
                .name(
                        "Cart-Inactive-Product-"
                                + UUID.randomUUID()
                )
                .description(
                        "Inactive product used for cart integration testing"
                )
                .sku(
                        "CART-INACTIVE-"
                                + UUID.randomUUID()
                )
                .price(new BigDecimal("999.00"))
                .status(ProductStatus.INACTIVE)
                .category(category)
                .build();

        return productRepository.save(product);
    }


    private ProductEntity createDeletedProduct(
            CategoryEntity category) {

        ProductEntity product = ProductEntity.builder()
                .name(
                        "Cart-Deleted-Product-"
                                + UUID.randomUUID()
                )
                .description(
                        "Deleted product used for cart integration testing"
                )
                .sku(
                        "CART-DELETED-"
                                + UUID.randomUUID()
                )
                .price(new BigDecimal("999.00"))
                .status(ProductStatus.DELETED)
                .category(category)
                .build();

        return productRepository.save(product);
    }


    private CartEntity createCart(UserEntity user) {

        CartEntity cart = CartEntity.builder()
                .user(user)
                .build();

        return cartRepository.save(cart);
    }


    private CartItemEntity createCartItem(
            CartEntity cart,
            ProductEntity product,
            int quantity) {

        CartItemEntity cartItem = CartItemEntity.builder()
                .cart(cart)
                .product(product)
                .quantity(quantity)
                .build();

        return cartItemRepository.save(cartItem);
    }



    @Test
    void shouldAddProductToCartSuccessfully()
            throws Exception {

        UserEntity customer = createCustomer();
        String customerToken = getLoginToken(customer);

        CategoryEntity category = createCategory();
        ProductEntity product = createProduct(category);

        CartItemRequestDTO requestDTO =
                CartItemRequestDTO.builder()
                        .productId(product.getId())
                        .quantity(3)
                        .build();

        mockMvc.perform(
                        post("/api/v1/cart/items")
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                requestDTO
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(
                        jsonPath("$.productId")
                                .value(product.getId())
                )
                .andExpect(
                        jsonPath("$.quantity")
                                .value(3)
                );

        CartEntity cart =
                cartRepository.findByUserId(customer.getId())
                        .orElseThrow();

        CartItemEntity savedCartItem =
                cartItemRepository
                        .findByCartIdAndProductId(
                                cart.getId(),
                                product.getId()
                        )
                        .orElseThrow();

        assertThat(savedCartItem.getQuantity())
                .isEqualTo(3);
    }




    @Test
    void shouldIncreaseQuantityWhenSameProductIsAddedAgain()
            throws Exception {

        UserEntity customer = createCustomer();
        String customerToken = getLoginToken(customer);

        CategoryEntity category = createCategory();
        ProductEntity product = createProduct(category);

        CartItemRequestDTO firstRequest =
                CartItemRequestDTO.builder()
                        .productId(product.getId())
                        .quantity(3)
                        .build();

        mockMvc.perform(
                        post("/api/v1/cart/items")
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                firstRequest
                                        )
                                )
                )
                .andExpect(status().isOk());

        CartItemRequestDTO secondRequest =
                CartItemRequestDTO.builder()
                        .productId(product.getId())
                        .quantity(2)
                        .build();

        mockMvc.perform(
                        post("/api/v1/cart/items")
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                secondRequest
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.quantity")
                                .value(5)
                );

        CartEntity cart =
                cartRepository.findByUserId(customer.getId())
                        .orElseThrow();

        CartItemEntity cartItem =
                cartItemRepository
                        .findByCartIdAndProductId(
                                cart.getId(),
                                product.getId()
                        )
                        .orElseThrow();

        assertThat(cartItem.getQuantity())
                .isEqualTo(5);
    }




    @Test
    void shouldRejectAddingInvalidQuantity()
            throws Exception {

        String customerToken = getCustomerToken();

        CategoryEntity category = createCategory();
        ProductEntity product = createProduct(category);

        CartItemRequestDTO requestDTO =
                CartItemRequestDTO.builder()
                        .productId(product.getId())
                        .quantity(0)
                        .build();

        long cartItemCountBefore =
                cartItemRepository.count();

        mockMvc.perform(
                        post("/api/v1/cart/items")
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                requestDTO
                                        )
                                )
                )
                .andExpect(status().isBadRequest());

        long cartItemCountAfter =
                cartItemRepository.count();

        assertThat(cartItemCountAfter)
                .isEqualTo(cartItemCountBefore);
    }




    @Test
    void shouldRejectAddingNegativeQuantity()
            throws Exception {

        String customerToken = getCustomerToken();

        CategoryEntity category = createCategory();
        ProductEntity product = createProduct(category);

        CartItemRequestDTO requestDTO =
                CartItemRequestDTO.builder()
                        .productId(product.getId())
                        .quantity(-5)
                        .build();

        long cartItemCountBefore =
                cartItemRepository.count();

        mockMvc.perform(
                        post("/api/v1/cart/items")
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                requestDTO
                                        )
                                )
                )
                .andExpect(status().isBadRequest());

        long cartItemCountAfter =
                cartItemRepository.count();

        assertThat(cartItemCountAfter)
                .isEqualTo(cartItemCountBefore);
    }




    @Test
    void shouldRejectAddingInactiveProduct()
            throws Exception {

        String customerToken = getCustomerToken();

        CategoryEntity category = createCategory();
        ProductEntity product =
                createInactiveProduct(category);

        CartItemRequestDTO requestDTO =
                CartItemRequestDTO.builder()
                        .productId(product.getId())
                        .quantity(2)
                        .build();

        long cartItemCountBefore =
                cartItemRepository.count();

        mockMvc.perform(
                        post("/api/v1/cart/items")
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                requestDTO
                                        )
                                )
                )
                .andExpect(status().isNotFound());

        long cartItemCountAfter =
                cartItemRepository.count();

        assertThat(cartItemCountAfter)
                .isEqualTo(cartItemCountBefore);
    }




    @Test
    void shouldRejectAddingDeletedProduct()
            throws Exception {

        String customerToken = getCustomerToken();

        CategoryEntity category = createCategory();
        ProductEntity product =
                createDeletedProduct(category);

        CartItemRequestDTO requestDTO =
                CartItemRequestDTO.builder()
                        .productId(product.getId())
                        .quantity(2)
                        .build();

        long cartItemCountBefore =
                cartItemRepository.count();

        mockMvc.perform(
                        post("/api/v1/cart/items")
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                requestDTO
                                        )
                                )
                )
                .andExpect(status().isNotFound());

        long cartItemCountAfter =
                cartItemRepository.count();

        assertThat(cartItemCountAfter)
                .isEqualTo(cartItemCountBefore);
    }




    @Test
    void shouldGetMyCartSuccessfully()
            throws Exception {

        UserEntity customer = createCustomer();
        String customerToken = getLoginToken(customer);

        CategoryEntity category = createCategory();
        ProductEntity product = createProduct(category);

        CartItemRequestDTO requestDTO =
                CartItemRequestDTO.builder()
                        .productId(product.getId())
                        .quantity(4)
                        .build();

        mockMvc.perform(
                        post("/api/v1/cart/items")
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                requestDTO
                                        )
                                )
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/api/v1/cart")
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(
                        jsonPath("$.cartItems")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.cartItems[0].productId")
                                .value(product.getId())
                )
                .andExpect(
                        jsonPath("$.cartItems[0].quantity")
                                .value(4)
                );
    }




    @Test
    void shouldUpdateCartItemSuccessfully()
            throws Exception {

        UserEntity customer = createCustomer();
        String customerToken = getLoginToken(customer);

        CategoryEntity category = createCategory();
        ProductEntity product = createProduct(category);

        CartItemRequestDTO addRequest =
                CartItemRequestDTO.builder()
                        .productId(product.getId())
                        .quantity(3)
                        .build();

        mockMvc.perform(
                        post("/api/v1/cart/items")
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                addRequest
                                        )
                                )
                )
                .andExpect(status().isOk());

        CartEntity cart =
                cartRepository.findByUserId(customer.getId())
                        .orElseThrow();

        CartItemEntity cartItem =
                cartItemRepository
                        .findByCartIdAndProductId(
                                cart.getId(),
                                product.getId()
                        )
                        .orElseThrow();

        CartItemUpdateRequestDTO updateRequest =
                CartItemUpdateRequestDTO.builder()
                        .quantity(10)
                        .build();

        mockMvc.perform(
                        put(
                                "/api/v1/cart/items/"
                                        + cartItem.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                updateRequest
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(cartItem.getId())
                )
                .andExpect(
                        jsonPath("$.productId")
                                .value(product.getId())
                )
                .andExpect(
                        jsonPath("$.quantity")
                                .value(10)
                );

        CartItemEntity updatedCartItem =
                cartItemRepository
                        .findById(cartItem.getId())
                        .orElseThrow();

        assertThat(updatedCartItem.getQuantity())
                .isEqualTo(10);
    }




    @Test
    void shouldRejectUpdatingCartItemWithInvalidQuantity()
            throws Exception {

        UserEntity customer = createCustomer();
        String customerToken = getLoginToken(customer);

        CategoryEntity category = createCategory();
        ProductEntity product = createProduct(category);

        CartEntity cart = createCart(customer);
        CartItemEntity cartItem =
                createCartItem(
                        cart,
                        product,
                        3
                );

        CartItemUpdateRequestDTO updateRequest =
                CartItemUpdateRequestDTO.builder()
                        .quantity(0)
                        .build();

        mockMvc.perform(
                        put(
                                "/api/v1/cart/items/"
                                        + cartItem.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                updateRequest
                                        )
                                )
                )
                .andExpect(status().isBadRequest());

        CartItemEntity unchangedCartItem =
                cartItemRepository
                        .findById(cartItem.getId())
                        .orElseThrow();

        assertThat(unchangedCartItem.getQuantity())
                .isEqualTo(3);
    }


    @Test
    void shouldRemoveCartItemSuccessfully()
            throws Exception {

        UserEntity customer = createCustomer();
        String customerToken = getLoginToken(customer);

        CategoryEntity category = createCategory();
        ProductEntity product = createProduct(category);

        CartEntity cart = createCart(customer);

        CartItemEntity cartItem =
                createCartItem(
                        cart,
                        product,
                        3
                );

        mockMvc.perform(
                        delete(
                                "/api/v1/cart/items/"
                                        + cartItem.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                )
                .andExpect(status().isNoContent());

        assertThat(
                cartItemRepository.findById(
                        cartItem.getId()
                )
        )
                .isEmpty();

        assertThat(
                cartRepository.findById(
                        cart.getId()
                )
        )
                .isPresent();
    }




    @Test
    void shouldClearCartSuccessfully()
            throws Exception {

        UserEntity customer = createCustomer();
        String customerToken = getLoginToken(customer);

        CategoryEntity category = createCategory();

        ProductEntity product1 =
                createProduct(category);

        ProductEntity product2 =
                createProduct(category);

        CartEntity cart =
                createCart(customer);

        CartItemEntity cartItem1 =
                createCartItem(
                        cart,
                        product1,
                        2
                );

        CartItemEntity cartItem2 =
                createCartItem(
                        cart,
                        product2,
                        3
                );

        mockMvc.perform(
                        delete("/api/v1/cart")
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                )
                .andExpect(status().isNoContent());

        assertThat(
                cartItemRepository.findById(
                        cartItem1.getId()
                )
        )
                .isEmpty();

        assertThat(
                cartItemRepository.findById(
                        cartItem2.getId()
                )
        )
                .isEmpty();

        assertThat(
                cartRepository.findById(
                        cart.getId()
                )
        )
                .isPresent();
    }




    @Test
    void shouldGetCartCountSuccessfully()
            throws Exception {

        UserEntity customer = createCustomer();
        String customerToken = getLoginToken(customer);

        CategoryEntity category = createCategory();

        ProductEntity product1 =
                createProduct(category);

        ProductEntity product2 =
                createProduct(category);

        CartEntity cart =
                createCart(customer);

        createCartItem(
                cart,
                product1,
                2
        );

        createCartItem(
                cart,
                product2,
                3
        );

        mockMvc.perform(
                        get("/api/v1/cart/count")
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.totalItems")
                                .value(5)
                );
    }




    @Test
    void shouldNotAllowCustomerToAccessAnotherUsersCartItem()
            throws Exception {

        UserEntity firstCustomer =
                createCustomer();

        UserEntity secondCustomer =
                createSecondCustomer();

        CategoryEntity category =
                createCategory();

        ProductEntity product =
                createProduct(category);

        CartEntity firstCustomerCart =
                createCart(firstCustomer);

        CartItemEntity cartItem =
                createCartItem(
                        firstCustomerCart,
                        product,
                        5
                );

        String secondCustomerToken =
                getLoginToken(secondCustomer);

        CartItemUpdateRequestDTO updateRequest =
                CartItemUpdateRequestDTO.builder()
                        .quantity(10)
                        .build();

        mockMvc.perform(
                        put(
                                "/api/v1/cart/items/"
                                        + cartItem.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + secondCustomerToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                updateRequest
                                        )
                                )
                )
                .andExpect(status().isNotFound());

        mockMvc.perform(
                        delete(
                                "/api/v1/cart/items/"
                                        + cartItem.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + secondCustomerToken
                                )
                )
                .andExpect(status().isNotFound());

        CartItemEntity unchangedCartItem =
                cartItemRepository
                        .findById(cartItem.getId())
                        .orElseThrow();

        assertThat(unchangedCartItem.getQuantity())
                .isEqualTo(5);
    }




    @Test
    void shouldRejectCartAccessWithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/cart")
                )
                .andExpect(status().isUnauthorized());
    }




    @Test
    void shouldRejectCartModificationWithoutAuthentication()
            throws Exception {

        CategoryEntity category =
                createCategory();

        ProductEntity product =
                createProduct(category);

        CartItemRequestDTO requestDTO =
                CartItemRequestDTO.builder()
                        .productId(product.getId())
                        .quantity(2)
                        .build();

        mockMvc.perform(
                        post("/api/v1/cart/items")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                requestDTO
                                        )
                                )
                )
                .andExpect(status().isUnauthorized());
    }


    @Test
    void shouldRejectUpdatingNonExistingCartItem()
            throws Exception {

        String customerToken =
                getCustomerToken();

        CartItemUpdateRequestDTO requestDTO =
                CartItemUpdateRequestDTO.builder()
                        .quantity(5)
                        .build();

        mockMvc.perform(
                        put(
                                "/api/v1/cart/items/999999999"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                requestDTO
                                        )
                                )
                )
                .andExpect(status().isNotFound());
    }




    @Test
    void shouldRejectRemovingNonExistingCartItem()
            throws Exception {

        String customerToken =
                getCustomerToken();

        mockMvc.perform(
                        delete(
                                "/api/v1/cart/items/999999999"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                )
                .andExpect(status().isNotFound());
    }

}