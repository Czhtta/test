package com.comp5348.store.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UpdateStockRequest implements Serializable {
    private Long productId;
    private Long warehouseId;
    private int quantity;
}