package com.comp5348.store;

import com.comp5348.store.service.WarehouseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class WarehouseServiceTests {

    @Autowired
    private WarehouseService warehouseService;
    @Test
    void testFindWarehouse_SingleWarehouse(){
        long productId = 1L;
        int requiredQuantity = 8;

        Map<Long, Integer> allocation = warehouseService.findWarehousesForOrder(productId, requiredQuantity);
        assertEquals(1, allocation.size());
        assertEquals(requiredQuantity, allocation.get(101L));
    }
    @Test
    void testFindWarehouse_MultipleWarehouses(){
        long productId = 1L;
        int requiredQuantity = 12;

        Map<Long, Integer> allocation = warehouseService.findWarehousesForOrder(productId, requiredQuantity);

        assertEquals(2, allocation.size());
        assertEquals(10, allocation.get(101L));
        assertEquals(2, allocation.get(102L));
    }

    @Test
    void testFindWarehouse_InsufficientStock(){
        long productId = 1L;
        int requiredQuantity = 20;

        assertThrows(RuntimeException.class, () -> {
            warehouseService.findWarehousesForOrder(productId, requiredQuantity);
        });
    }
}
