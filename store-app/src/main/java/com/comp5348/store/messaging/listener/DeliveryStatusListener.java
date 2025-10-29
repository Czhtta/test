package com.comp5348.store.messaging.listener;

import com.comp5348.dto.DeliveryStatusUpdate;
import com.comp5348.dto.EmailRequest;
import com.comp5348.dto.RefundRequest;
import com.comp5348.store.config.RabbitMQConfig;
import com.comp5348.store.entity.Order;
import com.comp5348.store.entity.OrderStatus;
import com.comp5348.store.messaging.publisher.OrderEventPublisher;
import com.comp5348.store.repository.OrderRepository;
import com.comp5348.store.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 消费者，监听并处理 delivery-co-app 的配送状态更新。
 * 并将 JSON 反序列化为这里定义的dto对象：com.comp5348.dto.DeliveryStatusUpdate
 */
@Component
public class DeliveryStatusListener {
    public static final Logger log = LoggerFactory.getLogger(DeliveryStatusListener.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderEventPublisher orderEventPublisher;

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.QUEUE_DELIVERY_STATUS_UPDATE)
    public void handleDeliveryStatus(DeliveryStatusUpdate statusUpdate) {
        log.info(">>>> [STORE-APP] Received delivery status update for Order ID [{}]: {}",
                statusUpdate.getOrderId(), statusUpdate.getDeliveryStatus());

        Order order = orderRepository.findById(statusUpdate.getOrderId())
                .orElse(null);
        if(order == null){
            log.error("Received delivery status for unknown order ID: {}", statusUpdate.getOrderId());
            return;
        }

        String userEmail = order.getUser().getEmail();
        String status = statusUpdate.getDeliveryStatus();

        switch (status){
            case "PICKED_UP":
                orderService.updateOrderStatus(order.getId(), OrderStatus.SHIPPED);
                sendEmail(userEmail,
                        "Your order has been picked up!",
                        "Your order (ID" + order.getId() + " has been picked up and is on its way.");
                break;
            case "IN_TRANSIT":
                orderService.updateOrderStatus(order.getId(), OrderStatus.IN_TRANSIT);
                sendEmail(userEmail,
                        "Your order is in transit!",
                        "Your order (ID" + order.getId() + ") is currently in transit.");
                break;
            case "DELIVERED":
                orderService.updateOrderStatus(order.getId(), OrderStatus.DELIVERED);
                sendEmail(userEmail,
                        "Your order has been delivered!",
                        "Your order (ID" + order.getId() + ") has been successfully delivered.");
                break;
            case "LOST":
                log.error("Package LOST for Order ID [{}]. Triggering refund.", order.getId());
                // 1. 将订单标记为已取消或专门的状态
                orderService.updateOrderStatus(order.getId(), OrderStatus.CANCELLED);
                sendEmail(userEmail,
                        "Problem with Your Order: " + order.getId(),
                        "We are sorry, but your package for order "
                                + order.getId()
                                + " was lost in transit. The order has been cancelled and a full refund will be processed.");

                RefundRequest refundRequest = new RefundRequest();
                refundRequest.setOrderId(order.getId());
                refundRequest.setAmount(order.getTotalPrice());

                // 从 order 获取 user 再获取 bankAccountNumber
                refundRequest.setCustomerBankAccountNumber(order.getUser().getBankAccountNumber());

                orderEventPublisher.sendRefundRequest(refundRequest);
                break;

            default:
                log.warn("Received unknown delivery status '{}' for Order ID [{}]", status, order.getId());
        }
    }
    private void sendEmail(String to, String subject, String body){
        EmailRequest email = new EmailRequest();
        email.setTo(to);
        email.setSubject(subject);
        email.setBody(body);
        orderEventPublisher.sendEmailRequest(email);
    }
}
