package com.comp5348.store.messaging.listener;

import com.comp5348.dto.DeliveryRequest;
import com.comp5348.dto.EmailRequest;
import com.comp5348.dto.PaymentResponse;
import com.comp5348.store.config.RabbitMQConfig;
import com.comp5348.store.entity.Order;
import com.comp5348.store.entity.OrderStatus;
import com.comp5348.store.entity.OrderWarehouseAllocation;
import com.comp5348.store.messaging.publisher.OrderEventPublisher;
import com.comp5348.store.repository.OrderRepository;
import com.comp5348.store.service.OrderService;
import com.comp5348.store.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PaymentResultListener {
    public static final Logger log = LoggerFactory.getLogger(PaymentResultListener.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StockService stockService;

    @Autowired
    private OrderEventPublisher orderEventPublisher;
    @RabbitListener(queues = RabbitMQConfig.QUEUE_PAYMENT_RESPONSE)
    @Transactional
    public void handlePaymentResult(PaymentResponse response) {
        log.info(">>>> [STORE-APP] Received payment result for Order ID [{}]: {}",
                response.getOrderId(), response.getPaymentStatus());

        Order order = orderRepository.findById(response.getOrderId())
                .orElseThrow(()->new RuntimeException("Order not found: " + response.getOrderId()));

        if(order.getOrderStatus() != OrderStatus.PENDING){
            log.warn("Order ID [{}] is already processed with status: {}. Ignoring payment result.",
                    order.getId(), order.getOrderStatus());
            return;
        }

        Map<Long,Integer> warehouseAllocation = order.getWarehouseAllocations().stream()
                .collect(Collectors.toMap(
                        allocation -> allocation.getWarehouse().getId(),
                        OrderWarehouseAllocation::getAllocatedQuantity
                ));
        Long productId = order.getOrderItems().get(0).getProduct().getId();

        if ("SUCCESS".equals(response.getPaymentStatus())){
            log.info("Payment successful for Order ID [{}]. Updating status to CONFIRMED.", order.getId());
            orderService.updateOrderStatus(order.getId(), OrderStatus.PAYMENT_SUCCESS);

            stockService.decreaseStock(warehouseAllocation,productId);

            DeliveryRequest deliveryRequest = new DeliveryRequest();
            deliveryRequest.setOrderId(order.getId());
            deliveryRequest.setCustomerAddress(order.getUser().getAddress());
            deliveryRequest.setWarehouseAllocations(warehouseAllocation);
        }else{
            log.warn("Payment failed for Order ID [{}]. UReleasing stock...", order.getId());
            orderService.updateOrderStatus(order.getId(), OrderStatus.PAYMENT_FAILED);
            stockService.increaseStock(warehouseAllocation,productId);
            EmailRequest email = new EmailRequest();
            email.setTo(order.getUser().getUsername()+"@example.com");
            email.setSubject("Order Payment Failed" + order.getId());
            email.setBody("Your payment for order ID " + order.getId() + " has failed. The order has been cancelled.");

            orderEventPublisher.sendEmailRequest(email);
        }
    }
}
