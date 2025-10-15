package com.comp5348.store.service.impl;

import com.comp5348.store.entity.Stock;
import com.comp5348.store.exception.InsufficientStockException;
import com.comp5348.store.repository.StockRepository;
import com.comp5348.store.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WarehouseServiceImpl implements WarehouseService {
    private final StockRepository stockRepository;

    @Autowired
    public WarehouseServiceImpl(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
    public Map<Long, Integer> findWarehousesForOrder(Long productId, int requiredQuantity) {
        List<Stock> availableStocks = stockRepository.findByProductIdAndQuantityGreaterThanOrderByQuantityDesc(productId, 0);

        Map<Long, Integer> allocation = new HashMap<>();
        int remainingQuantity = requiredQuantity;

        for (Stock stock : availableStocks) {
            Long warehouseId = stock.getWarehouse().getId();
            int availableInWarehouse = stock.getQuantity();

            if (availableInWarehouse >= remainingQuantity) {
                allocation.put(warehouseId, remainingQuantity);
                remainingQuantity = 0; // 需求已完全满足
                break;
            } else {
                allocation.put(warehouseId, availableInWarehouse);
                remainingQuantity -= availableInWarehouse;
            }
        }
        if (remainingQuantity > 0) {
            // 遍历了所有仓库后，仍然无法满足需求，表示库存不足
            // 抛出一个异常，gei上层调用者（OrderService）处理
            throw new InsufficientStockException("Insufficient stock for product ID: " + productId);
        }
        return allocation;
    }

}
