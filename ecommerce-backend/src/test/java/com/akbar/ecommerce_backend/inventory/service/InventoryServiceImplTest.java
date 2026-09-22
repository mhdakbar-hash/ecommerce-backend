package com.akbar.ecommerce_backend.inventory.service;

import com.akbar.ecommerce_backend.exception.ResourceNotFoundException;
import com.akbar.ecommerce_backend.inventory.entity.InventoryEntity;
import com.akbar.ecommerce_backend.exception.InventoryStockInsufficientException;
import com.akbar.ecommerce_backend.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {


    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private InventoryEntity inventory;


    @BeforeEach
    void setUp() {

        inventory = InventoryEntity.builder()
                .id(1L)
                .availableQuantity(10)
                .reservedQuantity(0)
                .build();
    }

    @Test
    void reserveStock_shouldReserveStockSuccessfully() {

        when(inventoryRepository.findByProductIdForUpdate(1L))
                .thenReturn(Optional.of(inventory));


        inventoryService.reserveStock(1L, 3);


        assertEquals(7, inventory.getAvailableQuantity());

        assertEquals(3, inventory.getReservedQuantity());


        verify(inventoryRepository).findByProductIdForUpdate(1L);

        verify(inventoryRepository).save(inventory);
    }




    @Test
    void reserveStock_shouldThrowExceptionWhenStockIsInsufficient() {

        inventory.setAvailableQuantity(2);

        when(inventoryRepository.findByProductIdForUpdate(1L))
                .thenReturn(Optional.of(inventory));


        InventoryStockInsufficientException exception =
                assertThrows(
                        InventoryStockInsufficientException.class,
                        () -> inventoryService.reserveStock(1L, 5)
                );

        assertEquals("Insufficient stock for product id: 1. Available: 2, Requested: 5",
                exception.getMessage());


        assertEquals(2, inventory.getAvailableQuantity());

        assertEquals(0, inventory.getReservedQuantity());


        verify(inventoryRepository, never()).save(any());

    }





    @Test
    void reserveStock_shouldThrowExceptionWhenInventoryDoesNotExist() {

        when(inventoryRepository.findByProductIdForUpdate(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> inventoryService.reserveStock(1L, 3)
                );

        assertEquals("Inventory not found for product id: 1",
                exception.getMessage());

        verify(inventoryRepository, never()).save(any());

    }




    @Test
    void reserveStock_shouldThrowExceptionWhenQuantityIsZero() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> inventoryService.reserveStock(1L, 0)
                );

        assertEquals("Reservation quantity must be greater than 0",
                exception.getMessage());

        verify(inventoryRepository, never()).findByProductIdForUpdate(anyLong());

        verify(inventoryRepository, never()).save(any());

    }




    @Test
    void reserveStock_shouldThrowExceptionWhenQuantityIsNegative() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> inventoryService.reserveStock(1L, -2)
                );

        assertEquals("Reservation quantity must be greater than 0",
                exception.getMessage());

        verify(inventoryRepository, never()).findByProductIdForUpdate(anyLong());

        verify(inventoryRepository, never()).save(any());

    }




    @Test
    void releaseReservedStock_shouldReleaseStockSuccessfully() {

        inventory.setAvailableQuantity(7);
        inventory.setReservedQuantity(3);

        when(inventoryRepository.findByProductIdForUpdate(1L))
                .thenReturn(Optional.of(inventory));

        inventoryService.releaseReservedStock(1L, 3);

        assertEquals(10, inventory.getAvailableQuantity());

        assertEquals(0, inventory.getReservedQuantity());

        verify(inventoryRepository).findByProductIdForUpdate(1L);

        verify(inventoryRepository).save(inventory);

    }




    @Test
    void releaseReservedStock_shouldThrowExceptionWhenReservedStockIsInsufficient() {

        inventory.setAvailableQuantity(7);
        inventory.setReservedQuantity(2);

        when(inventoryRepository.findByProductIdForUpdate(1L))
                .thenReturn(Optional.of(inventory));

        InventoryStockInsufficientException exception =
                assertThrows(
                        InventoryStockInsufficientException.class,
                        () -> inventoryService.releaseReservedStock(1L, 5));


        assertEquals("Reserved stock is insufficient for product id: 1. Reserved: 2, Requested release: 5",
                exception.getMessage());


        assertEquals(7, inventory.getAvailableQuantity());

        assertEquals(2, inventory.getReservedQuantity());

        verify(inventoryRepository, never()).save(any());

    }



    @Test
    void releaseReservedStock_shouldThrowExceptionWhenQuantityIsInvalid() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> inventoryService.releaseReservedStock(1L, 0));


        assertEquals("Release quantity must be greater than 0",
                exception.getMessage());


        verify(inventoryRepository, never()).findByProductIdForUpdate(anyLong());

        verify(inventoryRepository, never()).save(any());

    }





}