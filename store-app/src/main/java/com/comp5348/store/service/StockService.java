package com.comp5348.store.service;

import java.util.Map;

public interface StockService {
    void decreaseStock(Map<Long, Integer> warehouseAllocation, Long productId);
    void increaseStock(Map<Long, Integer> warehouseAllocation, Long productId);

    /**
     * 不确定这个功能需不要需要
     * (后台管理功能) 更新或创建一个库存记录。
     * 如果记录已存在，则在现有数量上增加指定数量。
     * 如果记录不存在，则创建一条新的库存记录。
     * @param warehouseId 仓库ID
     * @param productId 商品ID
     * @param quantityToUpdate 要增加的数量
     */
    void updateStockQuantity(Long warehouseId, Long productId, int quantityToUpdate);

    /**
     * 获取某商品的总库存（所有仓库之和）
     */
    int getTotalStockByProductId(Long productId);
}
