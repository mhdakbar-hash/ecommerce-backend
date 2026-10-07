/*
package com.akbar.ecommerce_backend.cart.service;

import com.akbar.ecommerce_backend.cart.dto.CartItemRequestDTO;
import com.akbar.ecommerce_backend.cart.dto.CartItemResponseDTO;
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
import com.akbar.ecommerce_backend.user.entity.UserEntity;
import com.akbar.ecommerce_backend.user.enums.Role;
import com.akbar.ecommerce_backend.user.enums.UserStatus;
import com.akbar.ecommerce_backend.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class CartConcurrencyTest {

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
    private TransactionTemplate transactionTemplate;

    @Autowired
    private CartService cartService;

    private UserEntity user;
    private ProductEntity product;
    private CategoryEntity category;

    @BeforeEach
    void setUp() {

        category = categoryRepository.save(
                CategoryEntity.builder()
                        .name("Concurrency Category " + UUID.randomUUID())
                        .description("Concurrency test category")
                        .status(CategoryStatus.ACTIVE)
                        .build()
        );

        product = productRepository.save(
                ProductEntity.builder()
                        .name("Concurrency Product " + UUID.randomUUID())
                        .description("Concurrency test product")
                        .sku("CON-" + UUID.randomUUID())
                        .price(new BigDecimal("100.00"))
                        .status(ProductStatus.ACTIVE)
                        .category(category)
                        .build()
        );

        user = userRepository.save(
                UserEntity.builder()
                        .firstName("Concurrency")
                        .lastName("Test")
                        .email(
                                "cart-concurrency-"
                                        + UUID.randomUUID()
                                        + "@test.com"
                        )
                        .password("test-password")
                        .phoneNumber(
                                "9" + String.valueOf(
                                        System.currentTimeMillis()
                                ).substring(3)
                        )
                        .role(Role.CUSTOMER)
                        .status(UserStatus.ACTIVE)
                        .build()
        );
    }

    @AfterEach
    void tearDown() {

        cartRepository.findByUserId(user.getId())
                .ifPresent(cartRepository::delete);

        productRepository.delete(product);
        categoryRepository.delete(category);
        userRepository.delete(user);
    }

    @Test
    void shouldPreventLostUpdateWhenAddingSameProductConcurrently()
            throws Exception {

        CartEntity cart = cartRepository.save(
                CartEntity.builder()
                        .user(user)
                        .build()
        );

        cartItemRepository.save(
                CartItemEntity.builder()
                        .cart(cart)
                        .product(product)
                        .quantity(5)
                        .build()
        );

        CountDownLatch startLatch =
                new CountDownLatch(1);

        ExecutorService executorService =
                Executors.newFixedThreadPool(2);

        Future<Void> firstRequest =
                executorService.submit(() -> {

                    startLatch.await();

                    SecurityContextHolder.getContext()
                            .setAuthentication(
                                    new UsernamePasswordAuthenticationToken(
                                            user.getEmail(),
                                            null,
                                            List.of()
                                    )
                            );

                    try {

                        transactionTemplate.executeWithoutResult(
                                status -> {

                                    CartEntity lockedCart =
                                            cartRepository
                                                    .findByIdForUpdate(
                                                            cart.getId()
                                                    )
                                                    .orElseThrow();

                                    CartItemEntity cartItem =
                                            cartItemRepository
                                                    .findByCartIdAndProductId(
                                                            lockedCart.getId(),
                                                            product.getId()
                                                    )
                                                    .orElseThrow();

                                    cartItem.setQuantity(
                                            cartItem.getQuantity() + 2
                                    );

                                    cartItemRepository.save(cartItem);
                                }
                        );

                    } finally {
                        SecurityContextHolder.clearContext();
                    }

                    return null;
                });

        Future<Void> secondRequest =
                executorService.submit(() -> {

                    startLatch.await();

                    SecurityContextHolder.getContext()
                            .setAuthentication(
                                    new UsernamePasswordAuthenticationToken(
                                            user.getEmail(),
                                            null,
                                            List.of()
                                    )
                            );

                    try {

                        transactionTemplate.executeWithoutResult(
                                status -> {

                                    CartEntity lockedCart =
                                            cartRepository
                                                    .findByIdForUpdate(
                                                            cart.getId()
                                                    )
                                                    .orElseThrow();

                                    CartItemEntity cartItem =
                                            cartItemRepository
                                                    .findByCartIdAndProductId(
                                                            lockedCart.getId(),
                                                            product.getId()
                                                    )
                                                    .orElseThrow();

                                    cartItem.setQuantity(
                                            cartItem.getQuantity() + 3
                                    );

                                    cartItemRepository.save(cartItem);
                                }
                        );

                    } finally {
                        SecurityContextHolder.clearContext();
                    }

                    return null;
                });

        startLatch.countDown();

        firstRequest.get();
        secondRequest.get();

        executorService.shutdown();

        CartItemEntity finalCartItem =
                cartItemRepository.findByCartIdAndProductId(
                                cart.getId(),
                                product.getId()
                        )
                        .orElseThrow();

        assertNotNull(finalCartItem);
        assertEquals(
                10,
                finalCartItem.getQuantity()
        );
    }




    @Test
    void shouldCreateOnlyOneCartWhenAddingConcurrently()
            throws Exception {

        ExecutorService executorService =
                Executors.newFixedThreadPool(2);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        Callable<CartItemResponseDTO> task1 =
                () -> {

                    SecurityContextHolder.getContext()
                            .setAuthentication(
                                    new UsernamePasswordAuthenticationToken(
                                            user.getEmail(),
                                            null,
                                            List.of()
                                    )
                            );

                    try {

                        startLatch.await();

                        return cartService.addItemToCart(
                                CartItemRequestDTO.builder()
                                        .productId(product.getId())
                                        .quantity(2)
                                        .build()
                        );

                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                };

        Callable<CartItemResponseDTO> task2 =
                () -> {

                    SecurityContextHolder.getContext()
                            .setAuthentication(
                                    new UsernamePasswordAuthenticationToken(
                                            user.getEmail(),
                                            null,
                                            List.of()
                                    )
                            );

                    try {

                        startLatch.await();

                        return cartService.addItemToCart(
                                CartItemRequestDTO.builder()
                                        .productId(product.getId())
                                        .quantity(3)
                                        .build()
                        );

                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                };

        Future<CartItemResponseDTO> future1 =
                executorService.submit(task1);

        Future<CartItemResponseDTO> future2 =
                executorService.submit(task2);

        startLatch.countDown();

        CartItemResponseDTO response1 =
                future1.get();

        CartItemResponseDTO response2 =
                future2.get();

        executorService.shutdown();

        assertNotNull(response1);
        assertNotNull(response2);

        CartEntity cart =
                cartRepository.findByUserIdWithItems(user.getId())
                        .orElseThrow();

        assertNotNull(cart);

        assertEquals(
                1,
                cart.getCartItems().size()
        );

        CartItemEntity cartItem =
                cart.getCartItems().get(0);

        assertEquals(
                product.getId(),
                cartItem.getProduct().getId()
        );

        assertEquals(
                5,
                cartItem.getQuantity()
        );
    }

}*/
