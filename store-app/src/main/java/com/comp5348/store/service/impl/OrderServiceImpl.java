package com.comp5348.store.service.impl;

import com.comp5348.store.dto.CreateOrderRequest;
import com.comp5348.store.dto.OrderDTO;
import com.comp5348.store.entity.*;
import com.comp5348.store.exception.ProductNotFoundException;
import com.comp5348.store.repository.OrderRepository;
import com.comp5348.store.repository.ProductRepository;
import com.comp5348.store.repository.UserRepository;
import com.comp5348.store.service.OrderService;
import com.comp5348.store.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public OrderDTO createOrder(CreateOrderRequest request) {
        // 1. Check if user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Check if product exists and get product information
        List<Product> products = new ArrayList<>();
        List<CreateOrderRequest.OrderItemRequest> orderItems = request.getOrderItems();
        
        for (CreateOrderRequest.OrderItemRequest itemRequest : orderItems) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found: " + itemRequest.getProductId()));
            
            if (!product.getActive()) {
                throw new RuntimeException("Product is not active: " + product.getName());
            }
            
            products.add(product);
        }

        // 3. Check stock and allocate warehouse
        // Map<Long, Integer> warehouseAllocation = checkAndAllocateStock(products, orderItems);
        // TODO: Implement complex stock allocation algorithm

        // 4. Create order
        Order order = new Order();
        order.setUser(user);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderItems(new ArrayList<>());

        // 5. Create order items and calculate total price
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (int i = 0; i < orderItems.size(); i++) {
            CreateOrderRequest.OrderItemRequest itemRequest = orderItems.get(i);
            Product product = products.get(i);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalPrice = totalPrice.add(subtotal);

            order.getOrderItems().add(orderItem);
        }

        order.setTotalPrice(totalPrice);

        // 6. Save order
        Order savedOrder = orderRepository.save(order);

        // 7. Decrease stock - temporarily skipped, simplified for testing
        // TODO: In a production environment, this should decrease stock in specific warehouses based on warehouseAllocation

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
        
        // Restore stock
        // TODO: Implement stock restoration algorithm
        
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

        List<OrderDTO.OrderItemDTO> itemDTOs = order.getOrderItems().stream()
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

        dto.setOrderItems(itemDTOs);
        return dto;
    }
}