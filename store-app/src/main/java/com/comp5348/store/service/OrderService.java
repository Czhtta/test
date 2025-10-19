package com.comp5348.store.service;

import com.comp5348.store.dto.CreateOrderRequest;
import com.comp5348.store.dto.OrderDTO;
import com.comp5348.store.entity.OrderStatus;

import java.util.List;

public interface OrderService {
    
    /**
     * Create an order
     */
    OrderDTO createOrder(CreateOrderRequest request);
    
    /**
     * Get an order by ID
     */
    OrderDTO getOrderById(Long orderId);
    
    /**
     * Get all orders for a user
     */
    List<OrderDTO> getOrdersByUserId(Long userId);
    
    /**
     * Get all orders
     */
    List<OrderDTO> getAllOrders();
    
    /**
     * Get orders by status
     */
    List<OrderDTO> getOrdersByStatus(OrderStatus status);
    
    /**
     * Update order status
     */
    OrderDTO updateOrderStatus(Long orderId, OrderStatus newStatus);
    
    /**
     * Cancel an order
     */
    OrderDTO cancelOrder(Long orderId);
}
