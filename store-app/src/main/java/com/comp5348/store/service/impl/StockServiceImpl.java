package com.comp5348.store.service.impl;

import com.comp5348.store.entity.Product;
import com.comp5348.store.entity.Stock;
import com.comp5348.store.entity.Warehouse;
import com.comp5348.store.exception.InsufficientStockException;
import com.comp5348.store.repository.ProductRepository;
import com.comp5348.store.repository.StockRepository;
import com.comp5348.store.repository.WarehouseRepository;
import com.comp5348.store.service.ProductService;
import com.comp5348.store.service.StockService;
import com.comp5348.store.service.WarehouseService;
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
        // 查找是否已有对应的库存
        Optional<Stock> existingStockOpt = stockRepository.findByWarehouseIdAndProductId(warehouseId, productId);
        // 如果存在，则更新数量
        if (existingStockOpt.isPresent()) {
            Stock existingStock = existingStockOpt.get();
            existingStock.setQuantity(existingStock.getQuantity() + quantityToUpdate);
            stockRepository.save(existingStock);
        }else{
            // 如果不存在，则创建新的库存记录
            // 代码解释:
            // 1. productRepository.findById(productId) 返回一个 Optional<product>。
            // 2. .orElseThrow(...) :
            //    - 如果 Optional 里面有一个 Product 对象，就把这个对象返回，赋值给 product 变量。
            //    - 如果 Optional 是空的（数据库里没找到），就立刻抛出一个 RuntimeException 异常，并中断后续代码的执行。
            // 3. 类似的逻辑也适用于 warehouseRepository.findById(warehouseId)。
            // 4. 这样可以确保后续代码里使用的 product 和 warehouse 对象一定是有效的，不会是 null。
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


    //    decreaseStock (扣减库存):
    //    当 store-app 收到 bank-app 的“支付成功”消息后，PaymentResultListener 会被触发。
    //    在这个监听器里，调用 stockService.decreaseStock() 来扣减库存。
    @Override
    @Transactional
    public void decreaseStock(Map<Long, Integer> warehouseAllocation, Long productId) {
        for (Map.Entry<Long, Integer> entry : warehouseAllocation.entrySet()) {
            Long warehouseId = entry.getKey();
            Integer quantityToDecrease = entry.getValue();
            Stock stock = stockRepository.findByWarehouseIdAndProductId(warehouseId,productId)
                    .orElseThrow(() -> new RuntimeException(
                            "Stock not found for product ID: " + productId + " in warehouse ID: " + warehouseId));

            // 这里是个悲观检查
            // 实际上能走到这里，说明之前已经分配过库存了，
            // 如果没有足够的库存，warehouseService.findWarehousesForOrder 就会抛异常了
            // 但测试时发现会报错，并发订单导致库存超卖
            // 比如：库存只有5个，订单A分配了5个，订单B也分配了5个
            // 他们先后调用warehouseService.findWarehousesForOrder，发现库存足够
            // 订单A先支付成功，然后调用本方法decreaseStock，扣减库存后，库存变成0
            // 订单B再支付成功，扣减库存时就会发现库存不够了
            // 所以这里再检查一次库存是否足够，防止并发订单导致的库存超卖
            // 但没测试事务与加锁，后面有时间可以优化
            if (stock.getQuantity() < quantityToDecrease) {
                throw new InsufficientStockException("Insufficient stock in warehouse " + warehouseId + " for product " + productId);
            }

            stock.setQuantity(stock.getQuantity() - quantityToDecrease);
            stockRepository.save(stock);
        }
    }


    //    increaseStock (恢复库存):
    //    当用户取消订单时，OrderService 中的 cancelOrder 方法会被调用。_________________待实现_________________
    //    在这个方法里，会调用 stockService.increaseStock() 来把库存加回去。
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
}
