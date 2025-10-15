package com.comp5348.store.service;

import java.util.Map;

public interface WarehouseService {
    Map<Long, Integer> findWarehousesForOrder(Long productId, int requiredQuantity);
}
