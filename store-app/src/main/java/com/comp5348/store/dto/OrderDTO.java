package com.comp5348.store.dto;

import com.comp5348.store.entity.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private Long userId;
    private String username;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private BigDecimal totalPrice;
    private String deliveryAddress;
    private List<OrderWarehouseAllocationDTO> warehouseAllocations;
    private List<OrderItemDTO> orderItems;
    
    @Data
    public static class OrderWarehouseAllocationDTO {
        private Long id;
        private Long warehouseId;
        private String warehouseName;
        private Integer allocatedQuantity;
    }
    
    @Data
    public static class OrderItemDTO {
        private Long id;
        private Long productId;
        private String productName;
        private BigDecimal productPrice;
        private Integer quantity;
        private BigDecimal subtotal;
    }
}
