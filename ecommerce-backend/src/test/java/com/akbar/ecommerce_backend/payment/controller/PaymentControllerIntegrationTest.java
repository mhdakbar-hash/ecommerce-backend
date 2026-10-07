package com.akbar.ecommerce_backend.payment.controller;

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
import com.akbar.ecommerce_backend.payment.dto.PaymentResponseDTO;
import com.akbar.ecommerce_backend.payment.entity.PaymentEntity;
import com.akbar.ecommerce_backend.payment.enums.PaymentStatus;
import com.akbar.ecommerce_backend.payment.gateway.PaymentGateway;
import com.akbar.ecommerce_backend.payment.gateway.PaymentGatewayResponse;
import com.akbar.ecommerce_backend.payment.repository.PaymentRepository;
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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentControllerIntegrationTest {

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
    private PaymentRepository paymentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoSpyBean
    private PaymentGateway paymentGateway;


    private UserEntity createCustomer() {

        String email =
                "payment-customer-" + UUID.randomUUID() + "@gmail.com";

        UserEntity customer = UserEntity.builder()
                .firstName("Payment")
                .lastName("Customer")
                .email(email)
                .password(passwordEncoder.encode("Password@123"))
                .phoneNumber("888888" + String.format(
                        "%04d",
                        (int) (System.nanoTime() % 10000)
                ))
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        return userRepository.save(customer);
    }


    private String getCustomerToken(UserEntity customer)
            throws Exception {

        LoginRequestDTO loginRequest =
                LoginRequestDTO.builder()
                        .email(customer.getEmail())
                        .password("Password@123")
                        .build();

        String loginResponse =
                mockMvc.perform(
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

        return loginResponseDTO.getToken();
    }


    private CategoryEntity createCategory() {

        CategoryEntity category =
                CategoryEntity.builder()
                        .name(
                                "Payment-Category-"
                                        + UUID.randomUUID()
                        )
                        .description(
                                "Category used for payment integration testing"
                        )
                        .status(CategoryStatus.ACTIVE)
                        .build();

        return categoryRepository.save(category);
    }


    private ProductEntity createProduct(
            CategoryEntity category) {

        ProductEntity product =
                ProductEntity.builder()
                        .name(
                                "Payment-Product-"
                                        + UUID.randomUUID()
                        )
                        .description(
                                "Product used for payment integration testing"
                        )
                        .sku(
                                "PAY-TEST-"
                                        + UUID.randomUUID()
                        )
                        .price(new BigDecimal("100.00"))
                        .status(ProductStatus.ACTIVE)
                        .category(category)
                        .build();

        return productRepository.save(product);
    }


    private InventoryEntity createInventory(
            ProductEntity product) {

        InventoryEntity inventory =
                InventoryEntity.builder()
                        .product(product)
                        .availableQuantity(10)
                        .reservedQuantity(0)
                        .build();

        return inventoryRepository.save(inventory);
    }


    private void createCartItem(
            UserEntity customer,
            ProductEntity product,
            int quantity) {

        CartEntity cart =
                CartEntity.builder()
                        .user(customer)
                        .build();

        cart = cartRepository.save(cart);

        CartItemEntity cartItem =
                CartItemEntity.builder()
                        .cart(cart)
                        .product(product)
                        .quantity(quantity)
                        .build();

        cartItemRepository.save(cartItem);
    }


    @Test
    void shouldMarkPaymentFailedAndKeepOrderPending()
            throws Exception {

        UserEntity customer = createCustomer();

        String token = getCustomerToken(customer);

        CategoryEntity category = createCategory();

        ProductEntity product = createProduct(category);

        InventoryEntity inventory =
                createInventory(product);

        createCartItem(
                customer,
                product,
                2
        );

        String orderResponse =
                mockMvc.perform(
                                post("/api/v1/orders")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
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

        OrderEntity order =
                orderRepository.findById(createdOrder.getId())
                        .orElseThrow();

        assertThat(order.getStatus())
                .isEqualTo(OrderStatus.PENDING);

        String paymentRequest = """
                {
                    "orderId": %d,
                    "paymentMethod": "CARD"
                }
                """.formatted(order.getId());

        String paymentResponse =
                mockMvc.perform(
                                post("/api/v1/payments")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(paymentRequest)
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        PaymentResponseDTO payment =
                objectMapper.readValue(
                        paymentResponse,
                        PaymentResponseDTO.class
                );

        assertThat(payment.getStatus())
                .isEqualTo(PaymentStatus.FAILED);

        OrderEntity updatedOrder =
                orderRepository.findById(order.getId())
                        .orElseThrow();

        assertThat(updatedOrder.getStatus())
                .isEqualTo(OrderStatus.PENDING);

        InventoryEntity updatedInventory =
                inventoryRepository.findById(inventory.getId())
                        .orElseThrow();

        assertThat(updatedInventory.getAvailableQuantity())
                .isEqualTo(8);

        assertThat(updatedInventory.getReservedQuantity())
                .isEqualTo(2);
    }


    @Test
    void shouldMarkPaymentSuccessfulAndConfirmOrder()
            throws Exception {

        UserEntity customer = createCustomer();

        String token = getCustomerToken(customer);

        CategoryEntity category = createCategory();

        ProductEntity product = createProduct(category);

        createInventory(product);

        createCartItem(
                customer,
                product,
                2
        );

        String orderResponse =
                mockMvc.perform(
                                post("/api/v1/orders")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
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

        String paymentRequest = """
                {
                    "orderId": %d,
                    "paymentMethod": "UPI"
                }
                """.formatted(createdOrder.getId());

        String paymentResponse =
                mockMvc.perform(
                                post("/api/v1/payments")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(paymentRequest)
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        PaymentResponseDTO payment =
                objectMapper.readValue(
                        paymentResponse,
                        PaymentResponseDTO.class
                );

        assertThat(payment.getStatus())
                .isEqualTo(PaymentStatus.SUCCESS);

        OrderEntity updatedOrder =
                orderRepository.findById(createdOrder.getId())
                        .orElseThrow();

        assertThat(updatedOrder.getStatus())
                .isEqualTo(OrderStatus.CONFIRMED);
    }


    @Test
    void shouldRefundPaymentAndReleaseReservedInventory()
            throws Exception {

        UserEntity customer = createCustomer();

        String token = getCustomerToken(customer);

        CategoryEntity category = createCategory();

        ProductEntity product = createProduct(category);

        InventoryEntity inventory =
                createInventory(product);

        createCartItem(
                customer,
                product,
                3
        );

        String orderResponse =
                mockMvc.perform(
                                post("/api/v1/orders")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
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

        String paymentRequest = """
                {
                    "orderId": %d,
                    "paymentMethod": "UPI"
                }
                """.formatted(createdOrder.getId());

        String paymentResponse =
                mockMvc.perform(
                                post("/api/v1/payments")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(paymentRequest)
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        PaymentResponseDTO payment =
                objectMapper.readValue(
                        paymentResponse,
                        PaymentResponseDTO.class
                );

        assertThat(payment.getStatus())
                .isEqualTo(PaymentStatus.SUCCESS);

        InventoryEntity reservedInventory =
                inventoryRepository.findById(inventory.getId())
                        .orElseThrow();

        assertThat(reservedInventory.getAvailableQuantity())
                .isEqualTo(7);

        assertThat(reservedInventory.getReservedQuantity())
                .isEqualTo(3);

        mockMvc.perform(
                        post("/api/v1/payments/{id}/refund", payment.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());

        PaymentEntity refundedPayment =
                paymentRepository.findById(payment.getId())
                        .orElseThrow();

        assertThat(refundedPayment.getStatus())
                .isEqualTo(PaymentStatus.REFUNDED);

        OrderEntity refundedOrder =
                orderRepository.findById(createdOrder.getId())
                        .orElseThrow();

        assertThat(refundedOrder.getStatus())
                .isEqualTo(OrderStatus.REFUNDED);

        InventoryEntity releasedInventory =
                inventoryRepository.findById(inventory.getId())
                        .orElseThrow();

        assertThat(releasedInventory.getAvailableQuantity())
                .isEqualTo(10);

        assertThat(releasedInventory.getReservedQuantity())
                .isZero();
    }


    @Test
    void shouldNotChangeOrderPaymentOrInventoryWhenRefundFails()
            throws Exception {

        UserEntity customer = createCustomer();

        String token = getCustomerToken(customer);

        CategoryEntity category = createCategory();

        ProductEntity product = createProduct(category);

        InventoryEntity inventory =
                createInventory(product);

        createCartItem(
                customer,
                product,
                3
        );

        String orderResponse =
                mockMvc.perform(
                                post("/api/v1/orders")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
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

        String paymentRequest = """
                {
                    "orderId": %d,
                    "paymentMethod": "UPI"
                }
                """.formatted(createdOrder.getId());

        String paymentResponse =
                mockMvc.perform(
                                post("/api/v1/payments")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(paymentRequest)
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        PaymentResponseDTO payment =
                objectMapper.readValue(
                        paymentResponse,
                        PaymentResponseDTO.class
                );

        assertThat(payment.getStatus())
                .isEqualTo(PaymentStatus.SUCCESS);

        InventoryEntity reservedInventory =
                inventoryRepository.findById(inventory.getId())
                        .orElseThrow();

        assertThat(reservedInventory.getAvailableQuantity())
                .isEqualTo(7);

        assertThat(reservedInventory.getReservedQuantity())
                .isEqualTo(3);


        doReturn(
                new PaymentGatewayResponse(
                        PaymentStatus.FAILED,
                        "MOCK-FAILED-REFUND-" + UUID.randomUUID()
                )
        ).when(paymentGateway)
                .refundPayment(any(PaymentEntity.class));

        mockMvc.perform(
                        post("/api/v1/payments/{id}/refund", payment.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isConflict());


        PaymentEntity unchangedPayment =
                paymentRepository.findById(payment.getId())
                        .orElseThrow();

        assertThat(unchangedPayment.getStatus())
                .isEqualTo(PaymentStatus.SUCCESS);


        OrderEntity unchangedOrder =
                orderRepository.findById(createdOrder.getId())
                        .orElseThrow();

        assertThat(unchangedOrder.getStatus())
                .isEqualTo(OrderStatus.CONFIRMED);


        InventoryEntity unchangedInventory =
                inventoryRepository.findById(inventory.getId())
                        .orElseThrow();

        assertThat(unchangedInventory.getAvailableQuantity())
                .isEqualTo(7);

        assertThat(unchangedInventory.getReservedQuantity())
                .isEqualTo(3);
    }




    @Test
    void shouldPreventConcurrentDuplicatePaymentForSameOrder()
            throws Exception {

        UserEntity customer = createCustomer();

        String token = getCustomerToken(customer);

        CategoryEntity category = createCategory();

        ProductEntity product = createProduct(category);

        createInventory(product);

        createCartItem(
                customer,
                product,
                2
        );

        String orderResponse =
                mockMvc.perform(
                                post("/api/v1/orders")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
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

        String paymentRequest = """
            {
                "orderId": %d,
                "paymentMethod": "UPI"
            }
            """.formatted(orderId);

        int existingPaymentCount =
                paymentRepository
                        .findByOrderUserId(
                                customer.getId(),
                                org.springframework.data.domain.Pageable.unpaged()
                        )
                        .getContent()
                        .size();

        assertThat(existingPaymentCount)
                .isZero();

        ExecutorService executorService =
                Executors.newFixedThreadPool(2);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        try {

            Future<Integer> firstRequest =
                    executorService.submit(() -> {

                        startLatch.await();

                        return mockMvc.perform(
                                        post("/api/v1/payments")
                                                .header(
                                                        "Authorization",
                                                        "Bearer " + token
                                                )
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(paymentRequest)
                                )
                                .andReturn()
                                .getResponse()
                                .getStatus();
                    });

            Future<Integer> secondRequest =
                    executorService.submit(() -> {

                        startLatch.await();

                        return mockMvc.perform(
                                        post("/api/v1/payments")
                                                .header(
                                                        "Authorization",
                                                        "Bearer " + token
                                                )
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(paymentRequest)
                                )
                                .andReturn()
                                .getResponse()
                                .getStatus();
                    });


            startLatch.countDown();

            int firstStatus = firstRequest.get();
            int secondStatus = secondRequest.get();

            List<Integer> statuses =
                    new ArrayList<>();

            statuses.add(firstStatus);
            statuses.add(secondStatus);


            assertThat(statuses)
                    .containsExactlyInAnyOrder(
                            201,
                            409
                    );


            long paymentCount =
                    paymentRepository
                            .findByOrderUserId(
                                    customer.getId(),
                                    org.springframework.data.domain.Pageable.unpaged()
                            )
                            .getContent()
                            .stream()
                            .filter(payment ->
                                    payment.getOrder().getId().equals(orderId)
                            )
                            .count();

            assertThat(paymentCount)
                    .isEqualTo(1);

        } finally {
            executorService.shutdownNow();
        }
    }



    @Test
    void shouldRollbackPaymentWhenGatewayThrowsException()
            throws Exception {

        UserEntity customer = createCustomer();
        String token = getCustomerToken(customer);

        CategoryEntity category = createCategory();
        ProductEntity product = createProduct(category);
        createInventory(product);
        createCartItem(customer, product, 2);

        String orderResponse = mockMvc.perform(
                        post("/api/v1/orders")
                                .header("Authorization", "Bearer " + token)
                ).andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponseDTO createdOrder =
                objectMapper.readValue(orderResponse, OrderResponseDTO.class);

        Long orderId = createdOrder.getId();

        org.mockito.Mockito.doThrow(
                new RuntimeException("Payment gateway unavailable")
        ).when(paymentGateway).processPayment(any(PaymentEntity.class));

        mockMvc.perform(
                post("/api/v1/payments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "orderId": %d,
                                "paymentMethod": "UPI"
                            }
                            """.formatted(orderId))
        ).andExpect(status().isInternalServerError());

        OrderEntity order =
                orderRepository.findById(orderId).orElseThrow();

        assertThat(order.getStatus())
                .isEqualTo(OrderStatus.PENDING);

        assertThat(paymentRepository.existsByOrderId(orderId))
                .isFalse();
    }




    @Test
    void shouldRetryFailedPaymentAndConfirmOrder()
            throws Exception {

        UserEntity customer = createCustomer();
        String token = getCustomerToken(customer);

        CategoryEntity category = createCategory();
        ProductEntity product = createProduct(category);
        createInventory(product);
        createCartItem(customer, product, 2);

        String orderResponse =
                mockMvc.perform(
                                post("/api/v1/orders")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
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


        String paymentRequest = """
            {
                "orderId": %d,
                "paymentMethod": "CARD"
            }
            """.formatted(orderId);

        String paymentResponse =
                mockMvc.perform(
                                post("/api/v1/payments")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(paymentRequest)
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        PaymentResponseDTO failedPayment =
                objectMapper.readValue(
                        paymentResponse,
                        PaymentResponseDTO.class
                );

        assertThat(failedPayment.getStatus())
                .isEqualTo(PaymentStatus.FAILED);

        Long paymentId = failedPayment.getId();

        OrderEntity pendingOrder =
                orderRepository.findById(orderId)
                        .orElseThrow();

        assertThat(pendingOrder.getStatus())
                .isEqualTo(OrderStatus.PENDING);


        String retryRequest = """
            {
                "paymentMethod": "UPI"
            }
            """;

        String retryResponse =
                mockMvc.perform(
                                post("/api/v1/payments/{id}/retry", paymentId)
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(retryRequest)
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        PaymentResponseDTO retriedPayment =
                objectMapper.readValue(
                        retryResponse,
                        PaymentResponseDTO.class
                );

        assertThat(retriedPayment.getId())
                .isEqualTo(paymentId);

        assertThat(retriedPayment.getStatus())
                .isEqualTo(PaymentStatus.SUCCESS);

        assertThat(retriedPayment.getPaymentMethod())
                .isEqualTo(com.akbar.ecommerce_backend.payment.enums.PaymentMethod.UPI);


        OrderEntity confirmedOrder =
                orderRepository.findById(orderId)
                        .orElseThrow();

        assertThat(confirmedOrder.getStatus())
                .isEqualTo(OrderStatus.CONFIRMED);

        long paymentCount =
                paymentRepository
                        .findByOrderUserId(
                                customer.getId(),
                                org.springframework.data.domain.Pageable.unpaged()
                        )
                        .getContent()
                        .stream()
                        .filter(payment ->
                                payment.getOrder().getId().equals(orderId)
                        )
                        .count();

        assertThat(paymentCount)
                .isEqualTo(1);
    }




    @Test
    void shouldRejectRetryForSuccessfulPayment()
            throws Exception {

        UserEntity customer = createCustomer();
        String token = getCustomerToken(customer);

        CategoryEntity category = createCategory();
        ProductEntity product = createProduct(category);
        createInventory(product);
        createCartItem(customer, product, 2);

        String orderResponse =
                mockMvc.perform(
                                post("/api/v1/orders")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
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

        String paymentRequest = """
            {
                "orderId": %d,
                "paymentMethod": "UPI"
            }
            """.formatted(createdOrder.getId());

        String paymentResponse =
                mockMvc.perform(
                                post("/api/v1/payments")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(paymentRequest)
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        PaymentResponseDTO payment =
                objectMapper.readValue(
                        paymentResponse,
                        PaymentResponseDTO.class
                );

        assertThat(payment.getStatus())
                .isEqualTo(PaymentStatus.SUCCESS);

        mockMvc.perform(
                        post("/api/v1/payments/{id}/retry", payment.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "paymentMethod": "UPI"
                                    }
                                    """)
                )
                .andExpect(status().isConflict());

        PaymentEntity unchangedPayment =
                paymentRepository.findById(payment.getId())
                        .orElseThrow();

        assertThat(unchangedPayment.getStatus())
                .isEqualTo(PaymentStatus.SUCCESS);

        OrderEntity unchangedOrder =
                orderRepository.findById(createdOrder.getId())
                        .orElseThrow();

        assertThat(unchangedOrder.getStatus())
                .isEqualTo(OrderStatus.CONFIRMED);
    }




    @Test
    void shouldNotAllowAnotherUserToAccessPayment()
            throws Exception {

        UserEntity customerA = createCustomer();
        String tokenA = getCustomerToken(customerA);

        UserEntity customerB = createCustomer();
        String tokenB = getCustomerToken(customerB);

        CategoryEntity category = createCategory();
        ProductEntity product = createProduct(category);
        createInventory(product);
        createCartItem(customerA, product, 2);

        String orderResponse =
                mockMvc.perform(
                                post("/api/v1/orders")
                                        .header(
                                                "Authorization",
                                                "Bearer " + tokenA
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

        String paymentRequest = """
            {
                "orderId": %d,
                "paymentMethod": "UPI"
            }
            """.formatted(createdOrder.getId());

        String paymentResponse =
                mockMvc.perform(
                                post("/api/v1/payments")
                                        .header(
                                                "Authorization",
                                                "Bearer " + tokenA
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(paymentRequest)
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        PaymentResponseDTO payment =
                objectMapper.readValue(
                        paymentResponse,
                        PaymentResponseDTO.class
                );

        assertThat(payment.getStatus())
                .isEqualTo(PaymentStatus.SUCCESS);

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/v1/payments/{id}", payment.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenB
                                )
                )
                .andExpect(status().isNotFound());

        PaymentEntity unchangedPayment =
                paymentRepository.findById(payment.getId())
                        .orElseThrow();

        assertThat(unchangedPayment.getStatus())
                .isEqualTo(PaymentStatus.SUCCESS);
    }





    @Test
    void shouldRejectRefundForFailedPayment()
            throws Exception {

        UserEntity customer = createCustomer();
        String token = getCustomerToken(customer);

        CategoryEntity category = createCategory();
        ProductEntity product = createProduct(category);
        InventoryEntity inventory = createInventory(product);

        createCartItem(
                customer,
                product,
                2
        );

        String orderResponse =
                mockMvc.perform(
                                post("/api/v1/orders")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
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

        String paymentRequest = """
            {
                "orderId": %d,
                "paymentMethod": "CARD"
            }
            """.formatted(orderId);

        String paymentResponse =
                mockMvc.perform(
                                post("/api/v1/payments")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(paymentRequest)
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        PaymentResponseDTO payment =
                objectMapper.readValue(
                        paymentResponse,
                        PaymentResponseDTO.class
                );

        assertThat(payment.getStatus())
                .isEqualTo(PaymentStatus.FAILED);

        mockMvc.perform(
                        post("/api/v1/payments/{id}/refund", payment.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isConflict());

        PaymentEntity unchangedPayment =
                paymentRepository.findById(payment.getId())
                        .orElseThrow();

        assertThat(unchangedPayment.getStatus())
                .isEqualTo(PaymentStatus.FAILED);

        OrderEntity unchangedOrder =
                orderRepository.findById(orderId)
                        .orElseThrow();

        assertThat(unchangedOrder.getStatus())
                .isEqualTo(OrderStatus.PENDING);

        InventoryEntity unchangedInventory =
                inventoryRepository.findById(inventory.getId())
                        .orElseThrow();

        assertThat(unchangedInventory.getAvailableQuantity())
                .isEqualTo(8);

        assertThat(unchangedInventory.getReservedQuantity())
                .isEqualTo(2);
    }
}