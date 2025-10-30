package com.comp5348.store.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class CreateOrderRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "Quantity is required")
    private Integer quantity;

    @NotNull(message = "Delivery address is required")
    private String deliveryAddress;
}
