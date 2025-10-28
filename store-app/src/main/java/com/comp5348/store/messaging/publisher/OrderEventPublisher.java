package com.comp5348.store.messaging.publisher;

import com.comp5348.dto.DeliveryRequest;
import com.comp5348.dto.EmailRequest;
import com.comp5348.dto.PaymentRequest;
import com.comp5348.dto.RefundRequest;
import com.comp5348.store.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
/**
 * 生产者
 * 使用 RabbitTemplate 来简化消息的发送，将 Java 对象（DTO）自动转换为 JSON 格式，
 * 并通过指定的交换机（Exchange）和路由键（Routing Key）进行路由
 */
@Component
public class OrderEventPublisher {
    public static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendEmailRequest(EmailRequest emailRequest) {
        log.info("Sending Email request: {}", emailRequest);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_EMAIL,
                emailRequest
        );
    }
    public void sendDeliveryRequest(DeliveryRequest deliveryRequest) {
        log.info("Sending delivery request for order ID: {}", deliveryRequest.getOrderId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_DELIVERY,
                deliveryRequest
        );
    }
    public void sendPaymentRequest(PaymentRequest paymentRequest){
        log.info("Sending Payment request for order ID: {}",paymentRequest.getOrderId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_PAYMENT_REQUEST,
                paymentRequest
        );
    }
    public void sendRefundRequest(RefundRequest refundRequest){
        log.info("Sending Refund request for order ID: {}",refundRequest.getOrderId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_REFUND_REQUEST,
                refundRequest
        );
    }
}
