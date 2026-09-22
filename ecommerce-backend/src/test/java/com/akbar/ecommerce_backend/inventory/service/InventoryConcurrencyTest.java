package com.akbar.ecommerce_backend.inventory.service;

import com.akbar.ecommerce_backend.category.entity.CategoryEntity;
import com.akbar.ecommerce_backend.category.enums.CategoryStatus;
import com.akbar.ecommerce_backend.category.repository.CategoryRepository;
import com.akbar.ecommerce_backend.exception.InventoryStockInsufficientException;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InventoryConcurrencyTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private ProductEntity product;
    private InventoryEntity inventory;

    @BeforeEach
    void setUp() {

        CategoryEntity category = CategoryEntity.builder()
                .name("Concurrency Test Category")
                .description("Temporary category for concurrency testing")
                .status(CategoryStatus.ACTIVE)
                .build();

        category = categoryRepository.save(category);

        product = ProductEntity.builder()
                .name("Concurrency Test Product")
                .description("Temporary product for concurrency testing")
                .sku("CONCURRENCY-TEST-SKU")
                .price(BigDecimal.valueOf(100))
                .stockQuantity(5)
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


    }

    @Test
    void reserveStock_shouldPreventOversellingDuringConcurrentRequests()
            throws Exception {

        ExecutorService executorService =
                Executors.newFixedThreadPool(2);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        Future<String> firstRequest =
                executorService.submit(() -> {

                    startLatch.await();

                    try {
                        inventoryService.reserveStock(
                                product.getId(),
                                4
                        );

                        return "SUCCESS";
                    } catch (InventoryStockInsufficientException exception) {
                        return "INSUFFICIENT";
                    }
                });

        Future<String> secondRequest =
                executorService.submit(() -> {

                    startLatch.await();

                    try {
                        inventoryService.reserveStock(
                                product.getId(),
                                3
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
                        .filter("INSUFFICIENT"::equals)
                        .count()
        );

        InventoryEntity finalInventory =
                inventoryRepository
                        .findByProductId(product.getId())
                        .orElseThrow();

        int available =
                finalInventory.getAvailableQuantity();

        int reserved =
                finalInventory.getReservedQuantity();

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
    }
}