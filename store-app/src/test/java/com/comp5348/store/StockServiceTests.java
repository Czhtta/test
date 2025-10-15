package com.comp5348.store;

import com.comp5348.store.entity.Stock;
import com.comp5348.store.exception.InsufficientStockException;
import com.comp5348.store.repository.StockRepository;
import com.comp5348.store.service.StockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class StockServiceTests {

    @Autowired
    private StockService stockService;
    @Autowired
    private StockRepository stockRepository;
    private final Long PRODUCT_ID_LAPTOP = 1L;
    private final Long WAREHOUSE_ID_SYDNEY = 101L;
    private final Long WAREHOUSE_ID_MELBOURNE = 102L;

    // 辅助方法，查询某个仓库的最新库存数量
    private Optional<Stock> findStock(Long warehouseId, Long productId){;
        return stockRepository.findByWarehouseIdAndProductId(warehouseId, productId);
    }

    @Test
    @Transactional
    void testDecreaseStock_Success(){
        Map<Long,Integer> allocation = new HashMap<>();
        allocation.put(WAREHOUSE_ID_SYDNEY, 3);
        allocation.put(WAREHOUSE_ID_MELBOURNE, 2);

        stockService.decreaseStock(allocation,PRODUCT_ID_LAPTOP);

        // 悉尼仓库：10 - 3 = 7
        assertEquals(7, findStock(WAREHOUSE_ID_SYDNEY, PRODUCT_ID_LAPTOP).get().getQuantity());
        // 墨尔本仓库：5 - 2 = 3
        assertEquals(3, findStock(WAREHOUSE_ID_MELBOURNE, PRODUCT_ID_LAPTOP).get().getQuantity());
    }

    @Test
    @Transactional
    void testDecreaseStock_Insufficient(){
        Map<Long,Integer> allocation = new HashMap<>();
        allocation.put(WAREHOUSE_ID_SYDNEY, 11); // 悉尼仓库只有10个

        assertThrows(InsufficientStockException.class, () -> {
            stockService.decreaseStock(allocation, PRODUCT_ID_LAPTOP);
        });

        assertEquals(10, findStock(WAREHOUSE_ID_SYDNEY, PRODUCT_ID_LAPTOP).get().getQuantity());
    }

    @Test
    @Transactional
    void testIncreaseStock_Success(){
        Map<Long,Integer> allocation = new HashMap<>();
        allocation.put(WAREHOUSE_ID_SYDNEY, 5);
        allocation.put(WAREHOUSE_ID_MELBOURNE, 3);

        stockService.increaseStock(allocation,PRODUCT_ID_LAPTOP);

        assertEquals(15, findStock(WAREHOUSE_ID_SYDNEY, PRODUCT_ID_LAPTOP).get().getQuantity());

        assertEquals(8, findStock(WAREHOUSE_ID_MELBOURNE, PRODUCT_ID_LAPTOP).get().getQuantity());
    }

    @Test
    @Transactional
    void testUpdateStockQuantity_AddToExistingStock() {
        int quantityToRestock = 5;

        stockService.updateStockQuantity(WAREHOUSE_ID_SYDNEY, PRODUCT_ID_LAPTOP, quantityToRestock);

        assertEquals(15, findStock(WAREHOUSE_ID_SYDNEY, PRODUCT_ID_LAPTOP).get().getQuantity());
    }

    @Test
    @Transactional
    void testUpdateStockQuantity_CreateNewStockRecord() {

        // 商品 ID=3 是 Mouse，它在悉尼仓库 (ID=101) 没有记录 (初始只有 Laptop 和 Keyboard)
        Long PRODUCT_ID_MOUSE = 3L;
        int initialRestockQuantity = 50;

        stockService.updateStockQuantity(WAREHOUSE_ID_SYDNEY, PRODUCT_ID_MOUSE, initialRestockQuantity);

        Optional<Stock> newStock = findStock(WAREHOUSE_ID_SYDNEY, PRODUCT_ID_MOUSE);

        assertTrue(newStock.isPresent());

        assertEquals(initialRestockQuantity, newStock.get().getQuantity());
    }
}
