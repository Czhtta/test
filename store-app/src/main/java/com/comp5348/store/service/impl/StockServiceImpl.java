package com.comp5348.store.service.impl;

import com.comp5348.store.entity.Product;
import com.comp5348.store.entity.Stock;
import com.comp5348.store.entity.Warehouse;
import com.comp5348.store.exception.InsufficientStockException;
import com.comp5348.store.repository.ProductRepository;
import com.comp5348.store.repository.StockRepository;
import com.comp5348.store.repository.WarehouseRepository;
import com.comp5348.store.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.dialect.lock.OptimisticEntityLockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class StockServiceImpl implements StockService {
    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    @Autowired
    public StockServiceImpl(StockRepository stockRepository, ProductRepository productRepository, WarehouseRepository warehouseRepository) {
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }


    @Override
    @Transactional
    public void updateStockQuantity(Long warehouseId, Long productId, int quantityToUpdate) {
        if(quantityToUpdate <= 0) {
            throw new IllegalArgumentException("Wrong Quantity!!!");
        }
        // Check whether corresponding stock is available
        Optional<Stock> existingStockOpt = stockRepository.findByWarehouseIdAndProductId(warehouseId, productId);
        // If present, update the quantity
        if (existingStockOpt.isPresent()) {
            Stock existingStock = existingStockOpt.get();
            existingStock.setQuantity(existingStock.getQuantity() + quantityToUpdate);
            stockRepository.save(existingStock);
        }else{
            // If it does not exist, create a new inventory record.
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

            Warehouse warehouse = warehouseRepository.findById(warehouseId)
                    .orElseThrow(() -> new RuntimeException("Warehouse not found with ID: " + warehouseId));

            Stock newStock = new Stock();

            newStock.setWarehouse(warehouse);
            newStock.setProduct(product);
            newStock.setQuantity(quantityToUpdate);

            stockRepository.save(newStock);
        }
    }


    //    decreaseStock:
    //   Upon receiving the ‘payment successful’ notification from the bank-app, the PaymentResultListener is triggered.
    //    Within this listener, call stockService.decreaseStock() to deduct the stock.
    @Override
    @Transactional
    public void decreaseStock(Map<Long, Integer> warehouseAllocation, Long productId) {
        for (Map.Entry<Long, Integer> entry : warehouseAllocation.entrySet()) {
            Long warehouseId = entry.getKey();
            Integer quantityToDecrease = entry.getValue();
            Stock stock = stockRepository.findByWarehouseIdAndProductId(warehouseId,productId)
                    .orElseThrow(() -> new RuntimeException(
                            "Stock not found for product ID: " + productId + " in warehouse ID: " + warehouseId));


            if (stock.getQuantity() < quantityToDecrease) {
                throw new InsufficientStockException("Insufficient stock in warehouse " + warehouseId + " for product " + productId);
            }

            stock.setQuantity(stock.getQuantity() - quantityToDecrease);
            try {
                stockRepository.save(stock);
            } catch (OptimisticEntityLockException | ObjectOptimisticLockingFailureException e)  {
                // 捕获乐观锁异常
                log.warn("Optimistic locking failure while decreasing stock for product {} in warehouse {}: {}", productId, warehouseId, e.getMessage());
                // 重新抛出，让调用者知道发生了并发冲突
                throw new RuntimeException("Stock update conflict for product ID: " + productId + ", warehouse ID: " + warehouseId, e);
                // 或者抛出一个自定义的业务异常，如 ConcurrentStockUpdateException
            }
        }
    }


    //    increaseStock :
    //   Within this method, stockService.increaseStock() is called to replenish the inventory.
    @Override
    @Transactional
    public void increaseStock(Map<Long, Integer> warehouseAllocation, Long productId) {
        for (Map.Entry<Long, Integer> entry : warehouseAllocation.entrySet()) {
            Long warehouseId = entry.getKey();
            Integer quantityToIncrease = entry.getValue();
            Stock stock = stockRepository.findByWarehouseIdAndProductId(warehouseId, productId)
                    .orElseThrow(() -> new RuntimeException(
                            "Stock not found for product ID: " + productId + " in warehouse ID: " + warehouseId));
            stock.setQuantity(stock.getQuantity() + quantityToIncrease);
            try {
                stockRepository.save(stock);
            } catch (OptimisticEntityLockException | ObjectOptimisticLockingFailureException e)  {
                // 捕获乐观锁异常
                log.warn("Optimistic locking failure while increasing stock for product {} in warehouse {}: {}", productId, warehouseId, e.getMessage());
                // 重新抛出，让调用者知道发生了并发冲突
                throw new RuntimeException("Stock update conflict for product ID: " + productId + ", warehouse ID: " + warehouseId, e);
                // 或者抛出一个自定义的业务异常，如 ConcurrentStockUpdateException
            }
        }
    }

    @Override
    public int getTotalStockByProductId(Long productId) {
        Integer sum = stockRepository.sumQuantityByProductId(productId);
        return sum == null ? 0 : sum;
    }
}
