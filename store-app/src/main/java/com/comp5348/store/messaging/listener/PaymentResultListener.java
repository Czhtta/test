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
import java.util.concurrent.TimeUnit;
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
        Order order = null;

        try {
            order = orderRepository.findById(response.getOrderId())
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

                try {
                    log.info("Waiting for 10 seconds before sending delivery request for Order ID [{}]...", order.getId());
                    TimeUnit.SECONDS.sleep(10);
                    log.info("Wait to process sending delivery request for Order ID [{}]", order.getId());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Wait interrupted while processing payment success for Order ID [{}].", order.getId());
                    // 如果等待被中断，可能需要考虑如何处理，这里简单地重新抛出异常，让重试/DLQ机制接管
                    throw new RuntimeException("Wait interrupted", e);
                }

                Order currentOrder = orderRepository.findById(order.getId())
                        .orElse(null);

                if (currentOrder != null && currentOrder.getOrderStatus() == OrderStatus.PAYMENT_SUCCESS) {
                    DeliveryRequest deliveryRequest = new DeliveryRequest();
                    deliveryRequest.setOrderId(order.getId());
                    deliveryRequest.setCustomerAddress(order.getUser().getAddress());
                    deliveryRequest.setWarehouseAllocations(warehouseAllocation);
                    orderEventPublisher.sendDeliveryRequest(deliveryRequest);
                    log.info("Delivery request sent for Order ID [{}].", order.getId());

                    orderService.updateOrderStatus(order.getId(), OrderStatus.AWAITING_SHIPMENT);
                    log.info("Order ID [{}] status updated to AWAITING_SHIPMENT.", order.getId());
                }else {
                    // 如果状态不是 PAYMENT_SUCCESS (已被取消)
                    log.warn("Order ID [{}] status changed during wait (Current: {}). Skipping delivery request.",
                            order.getId(), (currentOrder != null ? currentOrder.getOrderStatus() : "DELETED"));
                    // 不需要发送 DeliveryRequest，也不需要改状态为 AWAITING_SHIPMENT
                }
            }else{
                log.warn("Payment failed for Order ID [{}]. UReleasing stock...", order.getId());
                orderService.updateOrderStatus(order.getId(), OrderStatus.PAYMENT_FAILED);
                //      stockService.increaseStock(warehouseAllocation,productId);
                EmailRequest email = new EmailRequest();
                email.setTo(order.getUser().getEmail());
                email.setSubject("Order Payment Failed" + order.getId());
                email.setBody("Your payment for order ID " + order.getId() + " has failed. The order has been cancelled.");

                orderEventPublisher.sendEmailRequest(email);
            }
        } catch (Exception e) {
            log.error("Error processing payment result for Order ID [{}]: {}. Message will be retried or sent to DLQ.", (response != null ? response.getOrderId() : "unknown"), e.getMessage(), e);
            // ... (错误处理和消息拒绝/重试，如果是手动ACK模式) ...
            throw new RuntimeException("Processing failed...", e);
        }
    }
}
