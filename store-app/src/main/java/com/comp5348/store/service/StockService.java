package com.comp5348.store.service;

public interface StockService {
    void reserveStock(Long productId, int quantity);
    void releaseStock(Long productId, int quantity);
}
