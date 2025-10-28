package com.comp5348.store.service.impl;

import com.comp5348.store.entity.Product;
import com.comp5348.store.entity.Stock;
import com.comp5348.store.entity.Warehouse;
import com.comp5348.store.exception.InsufficientStockException;
import com.comp5348.store.repository.ProductRepository;
import com.comp5348.store.repository.StockRepository;
import com.comp5348.store.repository.WarehouseRepository;
import com.comp5348.store.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
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

            // This is a pessimistic check
            // In practice, reaching this point indicates that inventory has already been allocated previously.
            // Should insufficient stock exist, `warehouseService.findWarehousesForOrder` would throw an exception
            // However, testing revealed errors occurring due to concurrent orders causing overselling
            // For instance: with only 5 units in stock, Order A allocates 5 units and Order B also allocates 5 units
            // Both sequentially call warehouseService.findWarehousesForOrder, finding sufficient stock
            // Order A completes payment first, then calls this method decreaseStock. After stock deduction, inventory becomes 0
            // When order B subsequently pays successfully, attempting to deduct stock reveals insufficient inventory
            // Hence, an additional inventory check is performed here to prevent overselling caused by concurrent orders
            // However, transactions and locking have not been tested; optimisation can be pursued later
            if (stock.getQuantity() < quantityToDecrease) {
                throw new InsufficientStockException("Insufficient stock in warehouse " + warehouseId + " for product " + productId);
            }

            stock.setQuantity(stock.getQuantity() - quantityToDecrease);
            stockRepository.save(stock);
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
            stockRepository.save(stock);
        }
    }

    @Override
    public int getTotalStockByProductId(Long productId) {
        Integer sum = stockRepository.sumQuantityByProductId(productId);
        return sum == null ? 0 : sum;
    }
}
