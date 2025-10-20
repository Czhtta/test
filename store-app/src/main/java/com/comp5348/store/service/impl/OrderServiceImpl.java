package com.comp5348.store.service.impl;

import com.comp5348.store.dto.CreateOrderRequest;
import com.comp5348.store.dto.OrderDTO;
import com.comp5348.store.entity.*;
import com.comp5348.store.exception.ProductNotFoundException;
import com.comp5348.store.repository.OrderRepository;
import com.comp5348.store.repository.ProductRepository;
import com.comp5348.store.repository.UserRepository;
import com.comp5348.store.repository.WarehouseRepository;
import com.comp5348.store.service.OrderService;
import com.comp5348.store.service.StockService;
import com.comp5348.store.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockService stockService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Override
    public OrderDTO createOrder(CreateOrderRequest request) {
        // 1. Check if user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Check if product exists and get product information
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + request.getProductId()));
        
        if (!product.getActive()) {
            throw new RuntimeException("Product is not active: " + product.getName());
        }
        
        // 3. Check stock and allocate warehouse
        Map<Long, Integer> warehouseAllocation = warehouseService.findWarehousesForOrder(product.getId(), request.getQuantity());

        // 4. Create order
        Order order = new Order();
        order.setUser(user);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setWarehouseAllocations(new ArrayList<>());
        order.setOrderItems(new ArrayList<>());

        // 5. Create order items and calculate total price
        BigDecimal totalPrice = BigDecimal.ZERO;
        OrderItem orderItemEntity = new OrderItem();
        orderItemEntity.setOrder(order);
        orderItemEntity.setProduct(product);
        orderItemEntity.setQuantity(request.getQuantity());

        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(orderItemEntity.getQuantity()));
        totalPrice = totalPrice.add(subtotal);

        order.setTotalPrice(totalPrice);
        
        // Add order item to order
        order.getOrderItems().add(orderItemEntity);

        // 6. Create warehouse allocations
        for (Map.Entry<Long, Integer> allocation : warehouseAllocation.entrySet()) {
            OrderWarehouseAllocation warehouseAllocationEntity = new OrderWarehouseAllocation();
            warehouseAllocationEntity.setOrder(order);
            warehouseAllocationEntity.setWarehouse(warehouseRepository.findById(allocation.getKey())
                    .orElseThrow(() -> new RuntimeException("Warehouse not found: " + allocation.getKey())));
            warehouseAllocationEntity.setAllocatedQuantity(allocation.getValue());
            warehouseAllocationEntity.setProduct(product);
            order.getWarehouseAllocations().add(warehouseAllocationEntity);
        }

        // 7. Save order
        Order savedOrder = orderRepository.save(order);

        // 7. Decrease stock - temporarily skipped, simplified for testing
        // TODO: after implementing Bank app, this method will be called to decrease stock in specific warehouses based on warehouseAllocation
        stockService.decreaseStock(warehouseAllocation, product.getId());

        // 8. Convert to DTO and return
        return convertToDTO(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return convertToDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByOrderStatus(status);
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setOrderStatus(newStatus);
        Order savedOrder = orderRepository.save(order);
        
        return convertToDTO(savedOrder);
    }

    @Override
    public OrderDTO cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (order.getOrderStatus() == OrderStatus.DELIVERED || 
            order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order status does not allow cancellation");
        }
        
        order.setOrderStatus(OrderStatus.CANCELLED);
        
        // Restore stock to specific warehouses
        for (OrderWarehouseAllocation allocation : order.getWarehouseAllocations()) {
            Map<Long, Integer> stockToRestore = new HashMap<>();
            stockToRestore.put(allocation.getWarehouse().getId(), allocation.getAllocatedQuantity());
            stockService.increaseStock(stockToRestore, allocation.getProduct().getId());
        }
        
        Order savedOrder = orderRepository.save(order);
        return convertToDTO(savedOrder);
    }
    

    /**
     * Convert Order entity to OrderDTO
     */
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setUsername(order.getUser().getUsername());
        dto.setOrderDate(order.getOrderDate());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setTotalPrice(order.getTotalPrice());

        List<OrderDTO.OrderItemDTO> itemDTOs = new ArrayList<>();
        if (order.getOrderItems() != null) {
            itemDTOs = order.getOrderItems().stream()
                    .map(item -> {
                        OrderDTO.OrderItemDTO itemDTO = new OrderDTO.OrderItemDTO();
                        itemDTO.setId(item.getId());
                        itemDTO.setProductId(item.getProduct().getId());
                        itemDTO.setProductName(item.getProduct().getName());
                        itemDTO.setProductPrice(item.getProduct().getPrice());
                        itemDTO.setQuantity(item.getQuantity());
                        itemDTO.setSubtotal(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                        return itemDTO;
                    })
                    .collect(Collectors.toList());
        }

        dto.setOrderItems(itemDTOs);

        // Convert warehouse allocations
        List<OrderDTO.OrderWarehouseAllocationDTO> warehouseAllocationDTOs = new ArrayList<>();
        if (order.getWarehouseAllocations() != null) {
            warehouseAllocationDTOs = order.getWarehouseAllocations().stream()
                    .map(allocation -> {
                        OrderDTO.OrderWarehouseAllocationDTO allocationDTO = new OrderDTO.OrderWarehouseAllocationDTO();
                        allocationDTO.setId(allocation.getId());
                        allocationDTO.setWarehouseId(allocation.getWarehouse().getId());
                        allocationDTO.setWarehouseName(allocation.getWarehouse().getName());
                        allocationDTO.setAllocatedQuantity(allocation.getAllocatedQuantity());
                        return allocationDTO;
                    })
                    .collect(Collectors.toList());
        }

        dto.setWarehouseAllocations(warehouseAllocationDTOs);
        return dto;
    }
}