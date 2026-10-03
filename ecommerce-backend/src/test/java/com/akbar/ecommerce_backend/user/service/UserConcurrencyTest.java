package com.akbar.ecommerce_backend.user.service;

import com.akbar.ecommerce_backend.exception.EmailAlreadyExistsException;
import com.akbar.ecommerce_backend.user.dto.user.UserRequestDTO;
import com.akbar.ecommerce_backend.user.entity.UserEntity;
import com.akbar.ecommerce_backend.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserConcurrencyTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private String testEmail;

    @BeforeEach
    void setUp() {

        testEmail = "concurrency-" + UUID.randomUUID() + "@example.com";
    }

    @AfterEach
    void tearDown() {

        userRepository.findByEmailIgnoreCase(testEmail)
                .ifPresent(userRepository::delete);
    }

    @Test
    void registerUser_shouldPreventDuplicateUserDuringConcurrentRequests()
            throws Exception {

        ExecutorService executorService =
                Executors.newFixedThreadPool(2);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        UserRequestDTO requestDTO = UserRequestDTO.builder()
                .firstName("Concurrency")
                .lastName("Test")
                .email(testEmail)
                .password("Password123")
                .phoneNumber("9876543210")
                .build();

        Future<String> firstRequest =
                executorService.submit(() -> {

                    startLatch.await();

                    try {
                        userService.registerUser(requestDTO);

                        return "SUCCESS";
                    } catch (EmailAlreadyExistsException |
                             DataIntegrityViolationException exception) {

                        return "DUPLICATE";
                    }
                });

        Future<String> secondRequest =
                executorService.submit(() -> {

                    startLatch.await();

                    try {
                        userService.registerUser(requestDTO);

                        return "SUCCESS";
                    } catch (EmailAlreadyExistsException |
                             DataIntegrityViolationException exception) {

                        return "DUPLICATE";
                    }
                });

        startLatch.countDown();

        List<String> results = List.of(
                firstRequest.get(),
                secondRequest.get()
        );

        executorService.shutdown();

        assertEquals(
                1,
                results.stream()
                        .filter("SUCCESS"::equals)
                        .count()
        );

        assertEquals(
                1,
                results.stream()
                        .filter("DUPLICATE"::equals)
                        .count()
        );

        List<UserEntity> users =
                userRepository.findAll()
                        .stream()
                        .filter(user -> testEmail.equalsIgnoreCase(user.getEmail()))
                        .toList();

        assertEquals(
                1,
                users.size(),
                "Only one user should exist for the same email"
        );
    }
}