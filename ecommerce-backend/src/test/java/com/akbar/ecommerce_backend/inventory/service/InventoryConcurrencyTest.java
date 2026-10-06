package com.akbar.ecommerce_backend.inventory.service;

import com.akbar.ecommerce_backend.category.entity.CategoryEntity;
import com.akbar.ecommerce_backend.category.enums.CategoryStatus;
import com.akbar.ecommerce_backend.category.repository.CategoryRepository;
import com.akbar.ecommerce_backend.exception.InventoryStockInsufficientException;
import com.akbar.ecommerce_backend.inventory.dto.InventoryResponseDTO;
import com.akbar.ecommerce_backend.inventory.dto.InventoryStockRequestDTO;
import com.akbar.ecommerce_backend.inventory.dto.InventoryUpdateRequestDTO;
import com.akbar.ecommerce_backend.inventory.entity.InventoryEntity;
import com.akbar.ecommerce_backend.inventory.repository.InventoryRepository;
import com.akbar.ecommerce_backend.product.entity.ProductEntity;
import com.akbar.ecommerce_backend.product.enums.ProductStatus;
import com.akbar.ecommerce_backend.product.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class InventoryConcurrencyTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private CategoryEntity category;
    private ProductEntity product;
    private InventoryEntity inventory;

    @BeforeEach
    void setUp() {

        String uniqueId = UUID.randomUUID().toString();

        category = CategoryEntity.builder()
                .name("Concurrency Test Category-" + uniqueId)
                .description("Temporary category for concurrency testing")
                .status(CategoryStatus.ACTIVE)
                .build();

        category = categoryRepository.save(category);

        product = ProductEntity.builder()
                .name("Concurrency Test Product-" + uniqueId)
                .description("Temporary product for concurrency testing")
                .sku("CONCURRENCY-" + uniqueId)
                .price(BigDecimal.valueOf(100))
                .status(ProductStatus.ACTIVE)
                .category(category)
                .build();

        product = productRepository.save(product);

        inventory = InventoryEntity.builder()
                .product(product)
                .availableQuantity(5)
                .reservedQuantity(0)
                .build();

        inventory = inventoryRepository.save(inventory);
    }

    @AfterEach
    void tearDown() {

        if (inventory != null) {
            inventoryRepository.delete(inventory);
        }

        if (product != null) {
            productRepository.delete(product);
        }

        if (category != null) {
            categoryRepository.delete(category);
        }
    }


    @Test
    void reserveStock_shouldPreventOversellingDuringConcurrentRequests()
            throws Exception {

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        try {

            Future<String> firstRequest = executorService.submit(() -> {
                startLatch.await();

                try {
                    inventoryService.reserveStock(product.getId(), 4);
                    return "SUCCESS";
                } catch (InventoryStockInsufficientException exception) {
                    return "INSUFFICIENT";
                }
            });

            Future<String> secondRequest = executorService.submit(() -> {
                startLatch.await();

                try {
                    inventoryService.reserveStock(product.getId(), 3);
                    return "SUCCESS";
                } catch (InventoryStockInsufficientException exception) {
                    return "INSUFFICIENT";
                }
            });

            startLatch.countDown();

            List<String> results = List.of(
                    firstRequest.get(),
                    secondRequest.get()
            );

            assertEquals(
                    1,
                    results.stream()
                            .filter("SUCCESS"::equals)
                            .count()
            );

            assertEquals(
                    1,
                    results.stream()
                            .filter("INSUFFICIENT"::equals)
                            .count()
            );

            InventoryEntity finalInventory = inventoryRepository
                    .findByProductId(product.getId())
                    .orElseThrow();

            int available = finalInventory.getAvailableQuantity();
            int reserved = finalInventory.getReservedQuantity();

            assertTrue(
                    available >= 0,
                    "Available stock must never become negative"
            );

            assertTrue(
                    reserved <= 5,
                    "Reserved stock must never exceed initial stock"
            );

            assertEquals(
                    5,
                    available + reserved,
                    "Available + reserved stock must equal initial stock"
            );

        } finally {
            executorService.shutdown();
        }
    }





    @Test
    void addStock_shouldPreventLostUpdatesDuringConcurrentRequests()
            throws Exception {

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        try {

            Future<String> firstRequest = executorService.submit(() -> {
                startLatch.await();

                inventoryService.addStock(
                        inventory.getId(),
                        new InventoryStockRequestDTO(10)
                );

                return "SUCCESS";
            });

            Future<String> secondRequest = executorService.submit(() -> {
                startLatch.await();

                inventoryService.addStock(
                        inventory.getId(),
                        new InventoryStockRequestDTO(20)
                );

                return "SUCCESS";
            });

            startLatch.countDown();

            assertEquals("SUCCESS", firstRequest.get());
            assertEquals("SUCCESS", secondRequest.get());

            InventoryEntity finalInventory = inventoryRepository
                    .findById(inventory.getId())
                    .orElseThrow();

            assertEquals(
                    35,
                    finalInventory.getAvailableQuantity(),
                    "Concurrent stock additions must not lose updates"
            );

        } finally {
            executorService.shutdown();
        }
    }






    @Test
    void removeStock_shouldPreventNegativeStockDuringConcurrentRequests()
            throws Exception {

        inventory.setAvailableQuantity(10);
        inventoryRepository.save(inventory);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        try {

            Future<String> firstRequest = executorService.submit(() -> {
                startLatch.await();

                try {
                    inventoryService.removeStock(
                            inventory.getId(),
                            new InventoryStockRequestDTO(6)
                    );

                    return "SUCCESS";

                } catch (InventoryStockInsufficientException exception) {
                    return "INSUFFICIENT";
                }
            });

            Future<String> secondRequest = executorService.submit(() -> {
                startLatch.await();

                try {
                    inventoryService.removeStock(
                            inventory.getId(),
                            new InventoryStockRequestDTO(5)
                    );

                    return "SUCCESS";

                } catch (InventoryStockInsufficientException exception) {
                    return "INSUFFICIENT";
                }
            });

            startLatch.countDown();

            List<String> results = List.of(
                    firstRequest.get(),
                    secondRequest.get()
            );

            assertEquals(
                    1,
                    results.stream()
                            .filter("SUCCESS"::equals)
                            .count()
            );

            assertEquals(
                    1,
                    results.stream()
                            .filter("INSUFFICIENT"::equals)
                            .count()
            );

            InventoryEntity finalInventory = inventoryRepository
                    .findById(inventory.getId())
                    .orElseThrow();

            int available = finalInventory.getAvailableQuantity();

            assertTrue(
                    available >= 0,
                    "Available stock must never become negative"
            );

            assertTrue(
                    available == 4 || available == 5,
                    "Final stock must reflect exactly one successful removal"
            );

        } finally {
            executorService.shutdown();
        }
    }





    @Test
    void updateInventory_shouldHandleConcurrentUpdatesSafely()
            throws Exception {

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        try {

            Future<Integer> firstRequest = executorService.submit(() -> {
                startLatch.await();

                InventoryResponseDTO response =
                        inventoryService.updateInventory(
                                inventory.getId(),
                                new InventoryUpdateRequestDTO(20)
                        );

                return response.getAvailableQuantity();
            });

            Future<Integer> secondRequest = executorService.submit(() -> {
                startLatch.await();

                InventoryResponseDTO response =
                        inventoryService.updateInventory(
                                inventory.getId(),
                                new InventoryUpdateRequestDTO(30)
                        );

                return response.getAvailableQuantity();
            });

            startLatch.countDown();

            Integer firstResult = firstRequest.get();
            Integer secondResult = secondRequest.get();

            assertTrue(
                    firstResult == 20 || firstResult == 30,
                    "First update must complete with a valid quantity"
            );

            assertTrue(
                    secondResult == 20 || secondResult == 30,
                    "Second update must complete with a valid quantity"
            );

            InventoryEntity finalInventory = inventoryRepository
                    .findById(inventory.getId())
                    .orElseThrow();

            int finalQuantity = finalInventory.getAvailableQuantity();

            assertTrue(
                    finalQuantity == 20 || finalQuantity == 30,
                    "Final quantity must be one of the successfully requested values"
            );

            assertTrue(
                    finalQuantity >= 0,
                    "Available stock must never become negative"
            );

        } finally {
            executorService.shutdown();
        }
    }


}