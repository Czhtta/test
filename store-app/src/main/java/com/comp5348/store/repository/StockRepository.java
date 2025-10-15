package com.comp5348.store.repository;

import com.comp5348.store.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    /**
     * 根据商品ID查找库存，按库存量降序排列。
     * 可以优先找到库存最duo的仓库。
     * @param productId 商品ID
     * @param quantity 数量，传入0来查找所有有货的仓库
     * @return 符合条件的库存列表
     */
    List<Stock> findByProductIdAndQuantityGreaterThanOrderByQuantityDesc(Long productId, int quantity);
    Optional<Stock> findByWarehouseIdAndProductId(Long warehouseId, Long productId);
}
