package com.comp5348.store.service.impl;

import com.comp5348.dto.DeliveryCancellationRequest;
import com.comp5348.dto.EmailRequest;
import com.comp5348.dto.PaymentRequest;
import com.comp5348.dto.RefundRequest;
import com.comp5348.store.dto.CreateOrderRequest;
import com.comp5348.store.dto.OrderDTO;
import com.comp5348.store.entity.*;
import com.comp5348.store.exception.ProductNotFoundException;
import com.comp5348.store.messaging.publisher.OrderEventPublisher;
import com.comp5348.store.repository.OrderRepository;
import com.comp5348.store.repository.ProductRepository;
import com.comp5348.store.repository.UserRepository;
import com.comp5348.store.repository.WarehouseRepository;
import com.comp5348.store.service.OrderService;
import com.comp5348.store.service.StockService;
import com.comp5348.store.service.WarehouseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

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

    @Autowired
    private OrderEventPublisher orderEventPublisher;

    @Override
    public OrderDTO createOrder(CreateOrderRequest request) {

        //  Check if user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Check if product exists and get product information
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + request.getProductId()));

        if (!product.getActive()) {
            log.warn("Attempted to order inactive Product ID [{}]. Aborting order creation.", product.getId());
            throw new RuntimeException("Product is not active: " + product.getName());
        }

        // Check stock and allocate warehouse
        Map<Long, Integer> warehouseAllocation = warehouseService.findWarehousesForOrder(product.getId(), request.getQuantity());
        log.info("Warehouse allocation found for Product ID [{}]: {}", product.getId(), warehouseAllocation);

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setWarehouseAllocations(new ArrayList<>());
        order.setOrderItems(new ArrayList<>());

        // Create order items and calculate total price
        BigDecimal totalPrice = BigDecimal.ZERO;
        OrderItem orderItemEntity = new OrderItem();
        orderItemEntity.setOrder(order);
        orderItemEntity.setProduct(product);
        order.setDeliveryAddress(request.getDeliveryAddress());
        orderItemEntity.setQuantity(request.getQuantity());

        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(orderItemEntity.getQuantity()));
        totalPrice = totalPrice.add(subtotal);

        order.setTotalPrice(totalPrice);

        // Add order item to order
        order.getOrderItems().add(orderItemEntity);

        // Create warehouse allocations
        for (Map.Entry<Long, Integer> allocation : warehouseAllocation.entrySet()) {
            OrderWarehouseAllocation warehouseAllocationEntity = new OrderWarehouseAllocation();
            warehouseAllocationEntity.setOrder(order);
            warehouseAllocationEntity.setWarehouse(warehouseRepository.findById(allocation.getKey())
                    .orElseThrow(() -> new RuntimeException("Warehouse not found: " + allocation.getKey())));
            warehouseAllocationEntity.setAllocatedQuantity(allocation.getValue());
            warehouseAllocationEntity.setProduct(product);
            order.getWarehouseAllocations().add(warehouseAllocationEntity);
        }

        Order savedOrder = orderRepository.save(order);


        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(savedOrder.getId());
        paymentRequest.setAmount(savedOrder.getTotalPrice());
        paymentRequest.setCustomerBankAccountNumber(user.getBankAccountNumber());

        orderEventPublisher.sendPaymentRequest(paymentRequest);

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

    /**
     * Cancels an order using atomic state transitions to prevent race conditions.
     * This method tries to update the order status directly in the database based on
     * its *current* state, ensuring that only one cancellation or processing flow
     * can succeed.
     */
    @Override
    public OrderDTO cancelOrder(Long orderId) {
        log.info("Attempting to cancel Order ID [{}] using atomic update.", orderId);

        // 1. Try to cancel from PENDING
        // This is an unpaid order. No refund or stock restore needed.
        int rowsUpdated = orderRepository.updateStatusIfCurrentStatusIs(
                orderId, OrderStatus.CANCELLED, OrderStatus.PENDING);

        if (rowsUpdated > 0) {
            log.info("Order ID [{}] successfully cancelled from PENDING status.", orderId);
            Order order = orderRepository.findById(orderId).orElseThrow(); // Re-fetch to get data for email
            sendEmail(order.getUser().getEmail(),
                    "Order Cancelled: " + orderId,
                    "Your order with ID " + orderId + " has been successfully cancelled.");
            return convertToDTO(order);
        }

        // 2. Try to cancel from PAYMENT_SUCCESS
        // This order is paid, but not yet sent to delivery.
        // Requires stock restore and refund.
        rowsUpdated = orderRepository.updateStatusIfCurrentStatusIs(
                orderId, OrderStatus.CANCELLED, OrderStatus.PAYMENT_SUCCESS);

        if (rowsUpdated > 0) {
            log.info("Order ID [{}] successfully cancelled from PAYMENT_SUCCESS. Restoring stock and issuing refund.", orderId);
            Order order = orderRepository.findById(orderId).orElseThrow();

            restoreStock(order);
            sendRefund(order);

            sendEmail(order.getUser().getEmail(),
                    "Order Cancelled: " + orderId,
                    "Your order with ID " + orderId + " has been cancelled. A refund will be processed shortly.");
            return convertToDTO(order);
        }

        // 3. Try to cancel from AWAITING_SHIPMENT
        // This order is paid AND has been processed for delivery.
        // Requires stock restore, refund, AND delivery cancellation.
        rowsUpdated = orderRepository.updateStatusIfCurrentStatusIs(
                orderId, OrderStatus.CANCELLED, OrderStatus.AWAITING_SHIPMENT);

        if (rowsUpdated > 0) {
            log.info("Order ID [{}] successfully cancelled from AWAITING_SHIPMENT. Restoring stock, issuing refund, and sending delivery cancellation.", orderId);
            Order order = orderRepository.findById(orderId).orElseThrow();

            restoreStock(order);
            sendRefund(order);
            sendDeliveryCancellation(order);

            sendEmail(order.getUser().getEmail(),
                    "Order Cancelled: " + orderId,
                    "Your order with ID " + orderId + " has been cancelled. A refund will be processed, and we will attempt to stop the shipment.");
            return convertToDTO(order);
        }

        // 4. If all updates failed, the order is in a non-cancellable state (or already cancelled)
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        log.warn("Failed to cancel Order ID [{}]. It is already in a final (or non-cancellable) state: {}", orderId, order.getOrderStatus());

        // Check if it's already cancelled
        if (order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.REFUNDED) {
            return convertToDTO(order); // Return current state; it's already done.
        }

        // Otherwise, it's SHIPPED, IN_TRANSIT, DELIVERED, etc.
        throw new RuntimeException("Cannot cancel order in status: " + order.getOrderStatus());
    }


    private void restoreStock(Order order) {
        log.info("Restoring stock for cancelled order ID [{}]", order.getId());
        Map<Long, Integer> stockToRestore = order.getWarehouseAllocations().stream()
                .collect(Collectors.toMap(
                        allocation -> allocation.getWarehouse().getId(),
                        OrderWarehouseAllocation::getAllocatedQuantity
                ));
        // Assumes single-item order based on CreateOrderRequest DTO.
        // If orders can have multiple items, this logic must be expanded.
        Long productId = order.getOrderItems().get(0).getProduct().getId();
        stockService.increaseStock(stockToRestore, productId);
    }


    private void sendRefund(Order order) {
        log.info("Sending refund request for cancelled order ID [{}]", order.getId());
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setOrderId(order.getId());
        refundRequest.setAmount(order.getTotalPrice());
        refundRequest.setCustomerBankAccountNumber(order.getUser().getBankAccountNumber());
        orderEventPublisher.sendRefundRequest(refundRequest);
    }


    private void sendDeliveryCancellation(Order order) {
        log.info("Sending delivery cancellation request for cancelled order ID [{}]", order.getId());
        DeliveryCancellationRequest cancellationRequest = new DeliveryCancellationRequest();
        cancellationRequest.setOrderId(order.getId());
        orderEventPublisher.sendDeliveryCancellationRequest(cancellationRequest);
    }


    private void sendEmail(String to, String subject, String body){
        EmailRequest email = new EmailRequest();
        email.setTo(to);
        email.setSubject(subject);
        email.setBody(body);
        orderEventPublisher.sendEmailRequest(email);
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
        dto.setDeliveryAddress(order.getDeliveryAddress());

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